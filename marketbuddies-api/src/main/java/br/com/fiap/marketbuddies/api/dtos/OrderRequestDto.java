package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

// Corpo do POST /api/orders. O comprador é o dono do token, por isso não há e-mail aqui.
@Data
public class OrderRequestDto {

    @NotEmpty(message = "Informe ao menos um item")
    private List<@Valid OrderItemRequestDto> items;
}
