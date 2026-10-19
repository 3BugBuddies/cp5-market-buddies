package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.configs.OrderProperties;
import br.com.fiap.marketbuddies.api.facades.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationJob {

    private final OrderService orderService;
    private final PaymentFacade paymentFacade;
    private final OrderProperties orderProperties;

    @Scheduled(fixedDelayString = "${marketbuddies.orders.expiration-check-ms:60000}",
            initialDelayString = "${marketbuddies.orders.expiration-check-ms:60000}")
    public void expireAwaitingOrders() {
        LocalDateTime limit = LocalDateTime.now().minus(orderProperties.expiration());
        List<Long> expired = orderService.findAwaitingSince(limit);
        if (expired.isEmpty()) {
            return;
        }
        long refused = expired.stream().filter(paymentFacade::expire).count();
        log.info("Job de expiração: {} pedido(s) aguardando desde antes de {}; {} recusado(s) por tempo esgotado",
                expired.size(), limit, refused);
    }
}
