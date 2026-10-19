package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.OrderItemEntity;

// Item de um pedido recusado para remontar o carrinho ("Tentar de novo").
// productId fica nulo quando o produto foi excluído (P2); quem remonta ignora esse item.
public record RetryItemDto(Long productId, String productName, Integer quantity) {

    // Chamar com o produto do item já carregado (entity graph ou dentro da transação).
    public static RetryItemDto from(OrderItemEntity item) {
        return new RetryItemDto(
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity());
    }
}
