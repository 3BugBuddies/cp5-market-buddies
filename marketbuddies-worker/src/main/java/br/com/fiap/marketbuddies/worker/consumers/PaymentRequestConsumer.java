package br.com.fiap.marketbuddies.worker.consumers;

import br.com.fiap.marketbuddies.worker.clients.TranquiloBankClient;
import br.com.fiap.marketbuddies.worker.dtos.AuthorizationRequestDto;
import br.com.fiap.marketbuddies.worker.dtos.AuthorizationResponseDto;
import br.com.fiap.marketbuddies.worker.dtos.PaymentRequestDto;
import br.com.fiap.marketbuddies.worker.dtos.PaymentResponseDto;
import br.com.fiap.marketbuddies.worker.producers.PaymentErrorProducer;
import br.com.fiap.marketbuddies.worker.producers.PaymentSuccessProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final TranquiloBankClient bankClient;
    private final PaymentSuccessProducer successProducer;
    private final PaymentErrorProducer errorProducer;

    @RabbitListener(queues = "payment-request-queue")
    public void receive(PaymentRequestDto request) {
        log.info("Pedido {} recebido · R$ {}", request.getOrderId(), request.getTotalAmount());

        AuthorizationResponseDto authorization = bankClient.authorize(
                new AuthorizationRequestDto(request.getOrderId(), request.getTotalAmount(), request.getCustomerEmail()));

        PaymentResponseDto response = new PaymentResponseDto(
                request.getOrderId(),
                authorization.getApproved(),
                authorization.getAuthorizationCode(),
                authorization.getReason(),
                LocalDateTime.now());

        if (Boolean.TRUE.equals(authorization.getApproved())) {
            successProducer.send(response);
            log.info("Pedido {} APROVADO {}", response.getOrderId(), response.getAuthorizationCode());
        } else {
            errorProducer.send(response);
            log.info("Pedido {} RECUSADO · {}", response.getOrderId(), response.getReason());
        }
    }
}
