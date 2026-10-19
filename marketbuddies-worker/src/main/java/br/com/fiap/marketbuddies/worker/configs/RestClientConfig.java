package br.com.fiap.marketbuddies.worker.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    /** HTTP/1.1 explícito: o cliente padrão do JDK tenta upgrade h2c, que servidores simples não aceitam. */
    @Bean
    public RestClient tranquiloBankRestClient(@Value("${tranquilo-bank.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
