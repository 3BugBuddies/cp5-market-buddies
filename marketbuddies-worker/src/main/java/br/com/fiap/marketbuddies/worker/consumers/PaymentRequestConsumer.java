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
        log.info("Pedido {} recebido · R$ {}", request.orderId(), request.totalAmount());

        AuthorizationResponseDto authorization = bankClient.authorize(
                new AuthorizationRequestDto(request.orderId(), request.totalAmount(), request.customerEmail()));

        PaymentResponseDto response = new PaymentResponseDto(
                request.orderId(),
                authorization.approved(),
                authorization.authorizationCode(),
                authorization.reason(),
                LocalDateTime.now());

        if (Boolean.TRUE.equals(authorization.approved())) {
            successProducer.send(response);
            log.info("Pedido {} APROVADO {}", response.orderId(), response.authorizationCode());
        } else {
            errorProducer.send(response);
            log.info("Pedido {} RECUSADO · {}", response.orderId(), response.reason());
        }
    }
}
