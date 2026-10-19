package br.com.fiap.marketbuddies.api.dtos;

import java.math.BigDecimal;

// Item da mensagem de pedido de pagamento. Mesmos campos do PaymentItemDto do worker.
public record PaymentItemDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice) {
}
