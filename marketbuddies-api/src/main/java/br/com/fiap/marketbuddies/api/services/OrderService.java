package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.OrderItemRequestDto;
import br.com.fiap.marketbuddies.api.dtos.PaymentRequestDto;
import br.com.fiap.marketbuddies.api.dtos.RetryItemDto;
import br.com.fiap.marketbuddies.api.entities.EmailEntity;
import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.entities.UserEntity;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.Role;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import br.com.fiap.marketbuddies.api.exceptions.BusinessException;
import br.com.fiap.marketbuddies.api.exceptions.NotFoundException;
import br.com.fiap.marketbuddies.api.exceptions.OutOfStockException;
import br.com.fiap.marketbuddies.api.repositories.EmailRepository;
import br.com.fiap.marketbuddies.api.repositories.OrderRepository;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import br.com.fiap.marketbuddies.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;

    public record CreatedOrder(OrderDto order, PaymentRequestDto paymentRequest) {
    }

    // Cria o pedido AWAITING_PAYMENT e reserva o estoque numa transação só (D17).
    // Se faltar estoque em qualquer item, OutOfStockException desfaz tudo: nenhum item baixa.
    @Transactional
    public CreatedOrder createOrder(Long customerId, List<OrderItemRequestDto> requestedItems, OrderOrigin origin) {
        UserEntity customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        if (customer.getRole() != Role.CUSTOMER) {
            throw new BusinessException("Conta de administrador não faz compras");
        }

        // Soma itens repetidos e ordena por id: as reservas travam as linhas sempre na mesma ordem,
        // então dois pedidos simultâneos não entram em deadlock
        Map<Long, Integer> quantities = new TreeMap<>();
        requestedItems.forEach(i -> quantities.merge(i.productId(), i.quantity(), Integer::sum));

        Map<Long, ProductEntity> products = new TreeMap<>();
        productRepository.findAllById(quantities.keySet()).forEach(p -> products.put(p.getId(), p));
        quantities.keySet().stream()
                .filter(id -> !products.containsKey(id))
                .findFirst()
                .ifPresent(id -> {
                    throw new NotFoundException("Produto " + id + " não encontrado");
                });

        OrderEntity order = new OrderEntity();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setOrigin(origin);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            ProductEntity product = products.get(entry.getKey());
            int quantity = entry.getValue();

            // update … where stock >= :qty: devolve 0 quando falta, sem ler-e-gravar (sem corrida)
            if (productRepository.reserve(product.getId(), quantity) == 0) {
                int available = productRepository.findById(product.getId()).map(ProductEntity::getStock).orElse(0);
                throw new OutOfStockException("Sem estoque para " + product.getName()
                        + " (pedido " + quantity + ", disponível " + available + ")");
            }

            // Foto do momento da compra (D8): nome, categoria, preço final e custo do dia
            OrderItemEntity item = new OrderItemEntity();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setCategory(product.getCategory());
            item.setQuantity(quantity);
            item.setUnitPrice(product.getFinalPrice());
            item.setUnitCost(product.getCost());
            order.getItems().add(item);

            total = total.add(product.getFinalPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        OrderEntity saved = orderRepository.save(order);
        log.info("Pedido {} criado ({}) · {} · R$ {}", saved.getId(), origin, customer.getEmail(), saved.getTotalAmount());
        return new CreatedOrder(OrderDto.from(saved), PaymentRequestDto.from(saved));
    }

    // Pagamento aprovado. Update condicional (só sai de AWAITING_PAYMENT): devolve true só para a primeira
    // resposta; repetida, atrasada (pedido já expirado) ou de um pedido que não existe neste banco devolve false.
    @Transactional
    public boolean markApproved(Long orderId, String authorizationCode) {
        int changed = orderRepository.respond(orderId, OrderStatus.APPROVED, authorizationCode, null, LocalDateTime.now());
        if (changed == 0) {
            logIgnored(orderId, "aprovação");
            return false;
        }
        log.info("Pedido {} APROVADO {}", orderId, authorizationCode);
        return true;
    }

    // Pagamento recusado ou tempo esgotado: o mesmo update condicional e, só para quem mudou o status,
    // a devolução do estoque reservado na mesma transação. Item com produto excluído (P2) não tem para onde voltar.
    @Transactional
    public boolean markRefused(Long orderId, String reason) {
        int changed = orderRepository.respond(orderId, OrderStatus.REFUSED, null, reason, LocalDateTime.now());
        if (changed == 0) {
            logIgnored(orderId, "recusa (" + reason + ")");
            return false;
        }
        OrderEntity order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " não encontrado"));
        for (OrderItemEntity item : order.getItems()) {
            if (item.getProduct() == null) {
                log.info("Pedido {} · {} foi excluído do catálogo; estoque não devolvido", orderId, item.getProductName());
                continue;
            }
            productRepository.release(item.getProduct().getId(), item.getQuantity());
        }
        log.info("Pedido {} RECUSADO · {} · estoque devolvido", orderId, reason);
        return true;
    }

    // Ids dos pedidos que aguardam o banco desde antes de `limit` (job de expiração, P1).
    @Transactional(readOnly = true)
    public List<Long> findAwaitingSince(LocalDateTime limit) {
        return orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.AWAITING_PAYMENT, limit).stream()
                .map(OrderEntity::getId)
                .toList();
    }

    // Pedido com o status do último e-mail. Só o dono vê; o ADMIN vê todos (API).
    // Pedido de outro cliente responde como inexistente (404), para não revelar quais ids existem.
    @Transactional(readOnly = true)
    public OrderDto getOrder(Long orderId, Long userId, boolean admin) {
        OrderEntity order = orderRepository.findDetailedById(orderId)
                .filter(o -> admin || o.getCustomer().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " não encontrado"));
        StatusEmail statusEmail = emailRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .map(EmailEntity::getStatusEmail)
                .orElse(null);
        return OrderDto.from(order, statusEmail);
    }

    // Itens de um pedido RECUSADO do próprio cliente, para "Tentar de novo" remontar o carrinho (D20).
    @Transactional(readOnly = true)
    public List<RetryItemDto> retryItems(Long orderId, Long customerId) {
        OrderEntity order = orderRepository.findDetailedById(orderId)
                .filter(o -> o.getCustomer().getId().equals(customerId))
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " não encontrado"));
        if (order.getStatus() != OrderStatus.REFUSED) {
            throw new BusinessException("Só um pedido recusado pode ser refeito");
        }
        return order.getItems().stream().map(RetryItemDto::from).toList();
    }

    // Resposta que não mudou nada: só registra o motivo, sem e-mail e sem devolução de estoque.
    private void logIgnored(Long orderId, String what) {
        String current = orderRepository.findById(orderId)
                .map(o -> "já está " + o.getStatus())
                .orElse("não existe neste banco");
        log.info("Pedido {} {}: {} ignorada (resposta ignorada)", orderId, current, what);
    }
}
