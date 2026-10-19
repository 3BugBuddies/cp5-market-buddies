package br.com.fiap.marketbuddies.api.events;

import br.com.fiap.marketbuddies.api.dtos.PaymentRequestDto;

// Publicado pelo OrderService dentro da transação do pedido. Leva a mensagem já montada,
// porque depois do commit (open-in-view desligado) o cliente e os itens não podem mais ser carregados.
public record OrderCreatedEvent(PaymentRequestDto paymentRequest) {
}
