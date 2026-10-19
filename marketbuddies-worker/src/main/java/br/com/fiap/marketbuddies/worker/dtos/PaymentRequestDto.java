package br.com.fiap.marketbuddies.worker.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Mesmos campos, na mesma ordem, do {@code PaymentRequestDto} que a api publica. */
public record PaymentRequestDto(
        Long orderId,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        List<PaymentItemDto> items,
        LocalDateTime requestedAt) {
}
