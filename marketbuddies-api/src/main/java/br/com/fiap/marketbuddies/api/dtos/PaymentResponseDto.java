package br.com.fiap.marketbuddies.api.dtos;

import java.time.LocalDateTime;

// Resposta do worker nas filas payment-response-success-queue e payment-response-error-queue.
// Mesmos campos do PaymentResponseDto do worker; o conversor JSON lê pelo tipo do parâmetro do @RabbitListener.
public record PaymentResponseDto(
        Long orderId,
        Boolean approved,
        String authorizationCode,
        String reason,
        LocalDateTime respondedAt) {
}
