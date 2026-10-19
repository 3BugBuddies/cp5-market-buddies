package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("marketbuddies.admin")
public record AdminProperties(String email, String password) {
}
