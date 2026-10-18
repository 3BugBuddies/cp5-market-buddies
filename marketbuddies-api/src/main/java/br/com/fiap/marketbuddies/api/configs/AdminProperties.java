package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

// marketbuddies.admin.email / marketbuddies.admin.password (application.yaml).
@ConfigurationProperties("marketbuddies.admin")
public record AdminProperties(String email, String password) {
}
