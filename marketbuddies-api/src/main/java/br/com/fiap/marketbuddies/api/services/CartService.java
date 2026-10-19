package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.CartDto;
import br.com.fiap.marketbuddies.api.dtos.OrderItemRequestDto;
import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Regras do carrinho da sessão: limita ao estoque ao adicionar e alterar, e monta a visão com preço e
// estoque atuais. A reserva de verdade só acontece no pagamento (OrderService.createOrder).
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartSession cart;
    private final ProductRepository productRepository;

    // Resultado de uma ação no carrinho, para o flash da tela: type = success | warning | error.
    public record Feedback(String type, String message) {
        static Feedback success(String message) { return new Feedback("success", message); }
        static Feedback warning(String message) { return new Feedback("warning", message); }
        static Feedback error(String message) { return new Feedback("error", message); }
    }

    @Transactional(readOnly = true)
    public Feedback add(Long productId, int quantity) {
        int wanted = cart.quantityOf(productId) + Math.max(1, quantity);
        return productRepository.findById(productId)
                .map(product -> store(product, wanted, product.getName() + " adicionado ao carrinho."))
                .orElseGet(() -> Feedback.error("Esse produto não está mais no catálogo."));
    }

    @Transactional(readOnly = true)
    // Devolve null quando a quantidade foi aceita como pedida (o stepper não precisa de aviso).
    public Feedback update(Long productId, int quantity) {
        if (quantity < 1) {
            return remove(productId);
        }
        return productRepository.findById(productId)
                .map(product -> store(product, quantity, null))
                .orElseGet(() -> {
                    cart.remove(productId);
                    return Feedback.warning("Esse produto saiu do catálogo e foi retirado do carrinho.");
                });
    }

    public Feedback remove(Long productId) {
        String name = cart.entries().containsKey(productId) ? cart.entries().get(productId).productName() : "Item";
        cart.remove(productId);
        return Feedback.success(name + " removido do carrinho.");
    }

    // Visão do carrinho com preço e estoque de agora. Produto excluído sai do carrinho com aviso (P4).
    // Quantidade acima do estoque NÃO é cortada aqui: o item aparece marcado e a reserva no pagamento decide.
    @Transactional(readOnly = true)
    public CartDto view() {
        Map<Long, ProductEntity> products = productRepository.findAllById(cart.entries().keySet()).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        List<String> notices = new ArrayList<>();
        List<Long> gone = new ArrayList<>();
        List<CartDto.Item> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (Map.Entry<Long, CartSession.Entry> entry : cart.entries().entrySet()) {
            ProductEntity product = products.get(entry.getKey());
            if (product == null) {
                gone.add(entry.getKey());
                notices.add(entry.getValue().productName() + " saiu do catálogo e foi retirado do carrinho.");
                continue;
            }
            int quantity = entry.getValue().quantity();
            BigDecimal subtotal = product.getFinalPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartDto.Item(product.getId(), product.getName(), product.getCategory().getLabel(),
                    product.getFinalPrice(), product.getPrice(), product.isOnSale(), quantity, product.getStock(),
                    subtotal));
            total = total.add(subtotal);
            totalQuantity += quantity;
        }
        gone.forEach(cart::remove);

        return new CartDto(items, totalQuantity, total, notices);
    }

    // Itens do carrinho no formato do pedido, para o checkout.
    public List<OrderItemRequestDto> toOrderItems(CartDto view) {
        return view.getItems().stream()
                .map(i -> new OrderItemRequestDto(i.getProductId(), i.getQuantity()))
                .toList();
    }

    public void clear() {
        cart.clear();
    }

    private Feedback store(ProductEntity product, int wanted, String successMessage) {
        if (product.getStock() <= 0) {
            cart.remove(product.getId());
            return Feedback.error(product.getName() + " está sem estoque no momento.");
        }
        if (wanted > product.getStock()) {
            cart.put(product.getId(), product.getName(), product.getStock());
            return Feedback.warning("Só temos " + product.getStock() + " unidades de " + product.getName()
                    + ". Ajustamos a quantidade no carrinho.");
        }
        cart.put(product.getId(), product.getName(), wanted);
        return successMessage == null ? null : Feedback.success(successMessage);
    }
}
