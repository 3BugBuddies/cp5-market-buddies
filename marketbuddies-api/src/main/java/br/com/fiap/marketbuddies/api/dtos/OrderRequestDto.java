package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Corpo do POST /api/orders. O comprador é o dono do token, por isso não há e-mail aqui.
public record OrderRequestDto(
        @NotEmpty(message = "Informe ao menos um item")
        List<@Valid OrderItemRequestDto> items) {
}
