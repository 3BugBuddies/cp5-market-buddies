package br.com.fiap.marketbuddies.api.facades;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.OrderItemRequestDto;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.events.OrderCreatedEvent;
import br.com.fiap.marketbuddies.api.producers.PaymentRequestProducer;
import br.com.fiap.marketbuddies.api.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

// Mesmo papel da PagamentoFacade da demo do professor (RabbitmqSpringBoot): concentra o fluxo do pagamento.
// Os controllers (tela e API) chamam a facade; o OrderService cuida só do pedido e do estoque.
// A T7 acrescenta aqui paymentApproved, paymentRefused, expire e resendEmail.
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentRequestProducer paymentRequestProducer;

    // Cria o pedido com a reserva de estoque. A mensagem para o worker sai em onOrderCreated, depois do commit.
    public OrderDto requestPayment(Long customerId, List<OrderItemRequestDto> items, OrderOrigin origin) {
        return orderService.createOrder(customerId, items, origin);
    }

    // AFTER_COMMIT: o worker só recebe pedido que já está gravado; se a transação desfez, nada é publicado.
    // Se o RabbitMQ estiver fora do ar, o pedido já foi gravado: registrar o erro em vez de devolver 500
    // para uma compra que existe. O pedido fica aguardando e o job de expiração (P1, T7) o recusa e devolve o estoque.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Long orderId = event.paymentRequest().getOrderId();
        try {
            paymentRequestProducer.send(event.paymentRequest());
            log.info("Pedido {} publicado na payment-request-queue", orderId);
        } catch (AmqpException ex) {
            log.error("Pedido {} gravado, mas a publicação no RabbitMQ falhou; o job de expiração vai recusá-lo",
                    orderId, ex);
        }
    }
}
