package br.com.fiap.marketbuddies.api.dtos;

import java.math.BigDecimal;

public record PaymentItemDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice) {
}
