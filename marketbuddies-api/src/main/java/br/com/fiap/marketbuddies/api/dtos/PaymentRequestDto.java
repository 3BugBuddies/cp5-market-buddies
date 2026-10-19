package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Mensagem publicada na payment-request-queue. Mesmos campos do PaymentRequestDto do worker,
// que a lê pelo tipo do parâmetro do @RabbitListener (o __TypeId__ com o pacote da api não importa).
public record PaymentRequestDto(
        Long orderId,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        List<PaymentItemDto> items,
        LocalDateTime requestedAt) {

    // Chamar dentro da transação que criou o pedido: lê o cliente e os itens.
    public static PaymentRequestDto from(OrderEntity order) {
        List<PaymentItemDto> items = order.getItems().stream()
                .map(i -> new PaymentItemDto(
                        i.getProduct() == null ? null : i.getProduct().getId(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();
        return new PaymentRequestDto(
                order.getId(),
                order.getCustomer().getEmail(),
                order.getCustomer().getFullName(),
                order.getTotalAmount(),
                items,
                LocalDateTime.now());
    }
}
