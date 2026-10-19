package br.com.fiap.marketbuddies.worker.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Mesmos campos, na mesma ordem, do PaymentRequestDto que a api publica. O worker lê a mensagem
// pelo tipo do parâmetro do @RabbitListener; o __TypeId__ com o pacote da api não é usado.
public record PaymentRequestDto(
        Long orderId,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        List<PaymentItemDto> items,
        LocalDateTime requestedAt) {
}
