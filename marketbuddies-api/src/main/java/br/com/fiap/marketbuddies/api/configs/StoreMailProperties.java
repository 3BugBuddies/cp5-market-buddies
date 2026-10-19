package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("marketbuddies.mail")
public record StoreMailProperties(String from, String baseUrl) {
}
