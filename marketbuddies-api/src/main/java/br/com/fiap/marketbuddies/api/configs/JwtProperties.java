package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

// app.jwt.secret / app.jwt.expiration-ms (application.yaml).
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
