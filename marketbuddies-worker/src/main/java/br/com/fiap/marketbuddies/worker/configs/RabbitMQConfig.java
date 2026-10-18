package br.com.fiap.marketbuddies.worker.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REQUEST_EXCHANGE = "payment-request-exchange";
    public static final String REQUEST_ROUTING_KEY = "payment-request-routing-key";
    public static final String REQUEST_QUEUE = "payment-request-queue";

    public static final String SUCCESS_EXCHANGE = "payment-response-success-exchange";
    public static final String SUCCESS_ROUTING_KEY = "payment-response-success-routing-key";
    public static final String SUCCESS_QUEUE = "payment-response-success-queue";

    public static final String ERROR_EXCHANGE = "payment-response-error-exchange";
    public static final String ERROR_ROUTING_KEY = "payment-response-error-routing-key";
    public static final String ERROR_QUEUE = "payment-response-error-queue";

    @Bean
    public DirectExchange paymentRequestExchange() {
        return ExchangeBuilder.directExchange(REQUEST_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue paymentRequestQueue() {
        return QueueBuilder.durable(REQUEST_QUEUE).build();
    }

    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder.bind(paymentRequestQueue()).to(paymentRequestExchange()).with(REQUEST_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentSuccessExchange() {
        return ExchangeBuilder.directExchange(SUCCESS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(SUCCESS_QUEUE).build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(paymentSuccessExchange()).with(SUCCESS_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentErrorExchange() {
        return ExchangeBuilder.directExchange(ERROR_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue paymentErrorQueue() {
        return QueueBuilder.durable(ERROR_QUEUE).build();
    }

    @Bean
    public Binding paymentErrorBinding() {
        return BindingBuilder.bind(paymentErrorQueue()).to(paymentErrorExchange()).with(ERROR_ROUTING_KEY);
    }

    // Spring AMQP 4.x usa Jackson 3; o Jackson2JsonMessageConverter (Jackson 2) está descontinuado.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
