package br.com.fiap.marketbuddies.api.producers;

import br.com.fiap.marketbuddies.api.configs.RabbitMQConfig;
import br.com.fiap.marketbuddies.api.dtos.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// Como o PagamentoRequestProducer do professor, mas envia o objeto e deixa o conversor JSON
// (JacksonJsonMessageConverter, Jackson 3) serializar, igual aos producers do worker.
@Component
@RequiredArgsConstructor
public class PaymentRequestProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(PaymentRequestDto request) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.REQUEST_EXCHANGE, RabbitMQConfig.REQUEST_ROUTING_KEY, request);
    }
}
