package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.OrderItemRequestDto;
import br.com.fiap.marketbuddies.api.dtos.PaymentRequestDto;
import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.entities.UserEntity;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.Role;
import br.com.fiap.marketbuddies.api.events.OrderCreatedEvent;
import br.com.fiap.marketbuddies.api.exceptions.BusinessException;
import br.com.fiap.marketbuddies.api.exceptions.NotFoundException;
import br.com.fiap.marketbuddies.api.exceptions.OutOfStockException;
import br.com.fiap.marketbuddies.api.repositories.OrderRepository;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import br.com.fiap.marketbuddies.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    // Cria o pedido AWAITING_PAYMENT e reserva o estoque numa transação só (D17).
    // Se faltar estoque em qualquer item, OutOfStockException desfaz tudo: nenhum item baixa.
    // O evento sai dentro da transação e a mensagem só é publicada depois do commit (PaymentFacade).
    @Transactional
    public OrderDto createOrder(Long customerId, List<OrderItemRequestDto> requestedItems, OrderOrigin origin) {
        UserEntity customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        if (customer.getRole() != Role.CUSTOMER) {
            throw new BusinessException("Conta de administrador não faz compras");
        }

        // Soma itens repetidos e ordena por id: as reservas travam as linhas sempre na mesma ordem,
        // então dois pedidos simultâneos não entram em deadlock
        Map<Long, Integer> quantities = new TreeMap<>();
        requestedItems.forEach(i -> quantities.merge(i.getProductId(), i.getQuantity(), Integer::sum));

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
        eventPublisher.publishEvent(new OrderCreatedEvent(PaymentRequestDto.from(saved)));
        log.info("Pedido {} criado ({}) · {} · R$ {}", saved.getId(), origin, customer.getEmail(), saved.getTotalAmount());
        return OrderDto.from(saved);
    }
}
