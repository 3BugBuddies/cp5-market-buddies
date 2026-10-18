package br.com.fiap.marketbuddies.worker.producers;

import br.com.fiap.marketbuddies.worker.configs.RabbitMQConfig;
import br.com.fiap.marketbuddies.worker.dtos.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentErrorProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(PaymentResponseDto response) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ERROR_EXCHANGE, RabbitMQConfig.ERROR_ROUTING_KEY, response);
    }
}
