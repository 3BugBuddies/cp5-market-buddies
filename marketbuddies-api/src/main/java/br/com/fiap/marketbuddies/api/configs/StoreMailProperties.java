package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

// marketbuddies.mail.from (application.yaml): remetente do e-mail de confirmação.
@ConfigurationProperties("marketbuddies.mail")
public record StoreMailProperties(String from) {
}
