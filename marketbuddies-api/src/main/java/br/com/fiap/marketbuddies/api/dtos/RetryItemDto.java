package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;

public record RetryItemDto(Long productId, String productName, Integer quantity) {

    public static RetryItemDto from(OrderItemEntity item) {
        return new RetryItemDto(
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity());
    }
}
