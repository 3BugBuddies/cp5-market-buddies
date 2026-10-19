package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
