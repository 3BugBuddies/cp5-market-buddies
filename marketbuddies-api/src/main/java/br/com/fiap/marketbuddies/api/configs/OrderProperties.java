package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

// marketbuddies.orders.* (application.yaml).
// expiration: quanto tempo um pedido pode ficar aguardando o banco antes do job recusá-lo (P1).
// O intervalo do job (expiration-check-ms) é lido direto no @Scheduled do OrderExpirationJob.
@ConfigurationProperties("marketbuddies.orders")
public record OrderProperties(@DefaultValue("5m") Duration expiration) {
}
