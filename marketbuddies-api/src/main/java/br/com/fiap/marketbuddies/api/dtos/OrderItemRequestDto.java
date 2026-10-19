package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto(
        @NotNull(message = "Informe o produto")
        Long productId,

        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade mínima é 1")
        Integer quantity) {
}
