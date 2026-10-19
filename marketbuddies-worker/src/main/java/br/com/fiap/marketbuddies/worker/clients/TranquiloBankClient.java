package br.com.fiap.marketbuddies.worker.clients;

import br.com.fiap.marketbuddies.worker.dtos.AuthorizationRequestDto;
import br.com.fiap.marketbuddies.worker.dtos.AuthorizationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TranquiloBankClient {

    static final String BANK_UNAVAILABLE = "Banco indisponível";

    private final RestClient restClient;
    private final int attempts;
    private final long waitMs;

    public TranquiloBankClient(RestClient tranquiloBankRestClient,
                               @Value("${tranquilo-bank.attempts}") int attempts,
                               @Value("${tranquilo-bank.wait-ms}") long waitMs) {
        this.restClient = tranquiloBankRestClient;
        this.attempts = attempts;
        this.waitMs = waitMs;
    }

    public AuthorizationResponseDto authorize(AuthorizationRequestDto request) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                log.info("Pedido {} · tentativa {}/{} no tranquilo-bank", request.orderId(), attempt, attempts);
                AuthorizationResponseDto response = restClient.post()
                        .uri("/api/payments/authorize")
                        .body(request)
                        .retrieve()
                        .body(AuthorizationResponseDto.class);
                if (response != null) {
                    return response;
                }
                log.warn("Pedido {} · tentativa {}/{} sem corpo na resposta", request.orderId(), attempt, attempts);
            } catch (Exception e) {
                log.warn("Pedido {} · tentativa {}/{} falhou: {}", request.orderId(), attempt, attempts, e.getMessage());
            }
            if (attempt < attempts) {
                pause();
            }
        }
        log.error("Pedido {} · tranquilo-bank indisponível após {} tentativas", request.orderId(), attempts);
        return new AuthorizationResponseDto(request.orderId(), false, null, BANK_UNAVAILABLE, LocalDateTime.now());
    }

    private void pause() {
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
