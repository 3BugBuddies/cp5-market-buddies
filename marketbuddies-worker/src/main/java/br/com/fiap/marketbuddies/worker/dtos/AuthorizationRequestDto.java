package br.com.fiap.marketbuddies.worker.dtos;

import java.math.BigDecimal;

public record AuthorizationRequestDto(
        Long orderId,
        BigDecimal amount,
        String customerEmail) {
}
