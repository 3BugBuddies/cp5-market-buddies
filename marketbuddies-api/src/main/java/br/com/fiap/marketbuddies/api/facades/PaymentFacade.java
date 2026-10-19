package br.com.fiap.marketbuddies.api.facades;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.OrderItemRequestDto;
import br.com.fiap.marketbuddies.api.dtos.PaymentResponseDto;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.enums.OrderStatus;
import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import br.com.fiap.marketbuddies.api.events.OrderCreatedEvent;
import br.com.fiap.marketbuddies.api.exceptions.BusinessException;
import br.com.fiap.marketbuddies.api.producers.PaymentRequestProducer;
import br.com.fiap.marketbuddies.api.services.EmailService;
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
// A volta (T7): os consumidores das respostas e o job de expiração também passam por aqui.
// A facade não é transacional: cada chamada ao OrderService é uma transação que já terminou
// quando o e-mail sai, então o SMTP nunca segura conexão do banco.
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentRequestProducer paymentRequestProducer;
    private final EmailService emailService;

    // Fallback do motivo quando o worker manda a recusa sem texto.
    private static final String DEFAULT_REFUSAL = "Pagamento recusado pelo banco";
    // P1: motivo do pedido que ficou tempo demais aguardando o banco.
    public static final String EXPIRED_REASON = "Tempo esgotado";

    // Cria o pedido com a reserva de estoque. A mensagem para o worker sai em onOrderCreated, depois do commit.
    public OrderDto requestPayment(Long customerId, List<OrderItemRequestDto> items, OrderOrigin origin) {
        return orderService.createOrder(customerId, items, origin);
    }

    // AFTER_COMMIT: o worker só recebe pedido que já está gravado; se a transação desfez, nada é publicado.
    // Se o RabbitMQ estiver fora do ar, o pedido já foi gravado: registrar o erro em vez de devolver 500
    // para uma compra que existe. O pedido fica aguardando e o job de expiração (P1, T7) o recusa e devolve o estoque.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Long orderId = event.paymentRequest().orderId();
        try {
            paymentRequestProducer.send(event.paymentRequest());
            log.info("Pedido {} publicado na payment-request-queue", orderId);
        } catch (AmqpException ex) {
            log.error("Pedido {} gravado, mas a publicação no RabbitMQ falhou; o job de expiração vai recusá-lo",
                    orderId, ex);
        }
    }

    // Fila de sucesso: grava APPROVED e, só se o status mudou agora, envia o e-mail (depois do commit).
    // Resposta repetida ou atrasada não manda segundo e-mail.
    public void paymentApproved(PaymentResponseDto response) {
        if (orderService.markApproved(response.orderId(), response.authorizationCode())) {
            emailService.sendConfirmation(response.orderId());
        }
    }

    // Fila de erro: grava REFUSED com o motivo do banco e devolve o estoque (só na primeira resposta).
    public void paymentRefused(PaymentResponseDto response) {
        String reason = response.reason() == null || response.reason().isBlank() ? DEFAULT_REFUSAL : response.reason();
        orderService.markRefused(response.orderId(), reason);
    }

    // Job de expiração (P1): o mesmo caminho idempotente da recusa. Se o banco responder depois,
    // a resposta encontra o pedido já REFUSED e é ignorada.
    public boolean expire(Long orderId) {
        return orderService.markRefused(orderId, EXPIRED_REASON);
    }

    // P5: reenvio pelo dashboard (botão da T8). Só pedido aprovado; grava uma nova linha em TB_EMAILS.
    public StatusEmail resendEmail(Long orderId) {
        OrderDto order = orderService.getOrder(orderId, null, true);
        if (order.status() != OrderStatus.APPROVED) {
            throw new BusinessException("Só pedido aprovado recebe e-mail de confirmação");
        }
        return emailService.sendConfirmation(orderId);
    }
}
