package br.com.fiap.marketbuddies.api.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

// marketbuddies.mail (application.yaml): remetente do e-mail de confirmação e a URL pública da api,
// usada para montar o link "Ver meu pedido" (o e-mail não roda dentro de uma requisição HTTP).
@ConfigurationProperties("marketbuddies.mail")
public record StoreMailProperties(String from, String baseUrl) {
}
