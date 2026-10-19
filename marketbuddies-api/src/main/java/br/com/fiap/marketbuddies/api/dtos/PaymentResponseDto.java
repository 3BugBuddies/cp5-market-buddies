package br.com.fiap.marketbuddies.api.dtos;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long orderId,
        Boolean approved,
        String authorizationCode,
        String reason,
        LocalDateTime respondedAt) {
}
