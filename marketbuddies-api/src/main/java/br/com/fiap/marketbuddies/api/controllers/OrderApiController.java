package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.OrderRequestDto;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.facades.PaymentFacade;
import br.com.fiap.marketbuddies.api.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// Compra pela API (Postman). Bearer de CUSTOMER (SecurityConfig); o comprador é o dono do token.
// 202 Accepted: o pedido foi criado e aguarda o banco. 400 corpo inválido e 409 sem estoque vêm do ApiExceptionHandler.
// GET /api/orders/{id} entra na T7.
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderRequestDto request,
                                           @AuthenticationPrincipal AuthUser user) {
        OrderDto order = paymentFacade.requestPayment(user.getId(), request.getItems(), OrderOrigin.API);
        return ResponseEntity.accepted()
                .location(URI.create("/api/orders/" + order.getId()))
                .body(order);
    }
}
