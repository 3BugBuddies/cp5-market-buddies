package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("marketbuddies.orders")
public record OrderProperties(@DefaultValue("5m") Duration expiration) {
}
