package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.OrderEntity;
import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Ordem explícita: sem ela o Jackson 3 põe os componentes do record na ordem do construtor. */
@JsonPropertyOrder({"authorizationCode", "createdAt", "customerEmail", "id", "items", "origin", "refusalReason",
        "respondedAt", "status", "statusEmail", "totalAmount"})
public record OrderDto(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        String customerEmail,
        OrderOrigin origin,
        LocalDateTime createdAt,
        LocalDateTime respondedAt,
        String authorizationCode,
        String refusalReason,
        StatusEmail statusEmail,
        List<Item> items) {

    public static OrderDto from(OrderEntity order) {
        return from(order, null);
    }

    public static OrderDto from(OrderEntity order, StatusEmail statusEmail) {
        return new OrderDto(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCustomer().getEmail(),
                order.getOrigin(),
                order.getCreatedAt(),
                order.getRespondedAt(),
                order.getAuthorizationCode(),
                order.getRefusalReason(),
                statusEmail,
                order.getItems().stream().map(Item::from).toList());
    }

    public record Item(
            String productName,
            Integer quantity,
            BigDecimal unitPrice) {

        static Item from(OrderItemEntity entity) {
            return new Item(entity.getProductName(), entity.getQuantity(), entity.getUnitPrice());
        }
    }
}
