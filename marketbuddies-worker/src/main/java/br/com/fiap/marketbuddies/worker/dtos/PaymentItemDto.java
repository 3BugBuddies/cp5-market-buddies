package br.com.fiap.marketbuddies.worker.dtos;

import java.math.BigDecimal;

public record PaymentItemDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice) {
}
