package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Resposta única do pedido (POST /api/orders e, na T7, GET /api/orders/{id}).
// Campos da resposta do banco ficam nulos enquanto o pedido aguarda pagamento.
@Data
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String customerEmail;
    private OrderOrigin origin;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private String authorizationCode;
    private String refusalReason;
    // Status do último e-mail do pedido; preenchido pela T7.
    private StatusEmail statusEmail;
    private List<Item> items;

    // Chamar dentro da transação: lê o cliente e os itens do pedido.
    public static OrderDto from(OrderEntity order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCustomerEmail(order.getCustomer().getEmail());
        dto.setOrigin(order.getOrigin());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setRespondedAt(order.getRespondedAt());
        dto.setAuthorizationCode(order.getAuthorizationCode());
        dto.setRefusalReason(order.getRefusalReason());
        dto.setItems(order.getItems().stream().map(Item::from).toList());
        return dto;
    }

    @Data
    @NoArgsConstructor
    public static class Item {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        static Item from(OrderItemEntity entity) {
            Item item = new Item();
            item.setProductName(entity.getProductName());
            item.setQuantity(entity.getQuantity());
            item.setUnitPrice(entity.getUnitPrice());
            return item;
        }
    }
}
