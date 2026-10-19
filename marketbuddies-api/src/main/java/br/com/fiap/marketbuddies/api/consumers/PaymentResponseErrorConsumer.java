package br.com.fiap.marketbuddies.api.consumers;

import br.com.fiap.marketbuddies.api.configs.RabbitMQConfig;
import br.com.fiap.marketbuddies.api.dtos.PaymentResponseDto;
import br.com.fiap.marketbuddies.api.facades.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseErrorConsumer {

    private final PaymentFacade paymentFacade;

    @RabbitListener(queues = RabbitMQConfig.ERROR_QUEUE)
    public void receive(@Payload PaymentResponseDto response) {
        log.info("Resposta de erro recebida: pedido {} · {}", response.orderId(), response.reason());
        if (response.orderId() == null) {
            log.warn("Resposta de erro sem orderId descartada: {}", response);
            return;
        }
        paymentFacade.paymentRefused(response);
    }
}
