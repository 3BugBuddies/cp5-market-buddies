package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Um item do POST /api/orders (e do carrinho, quando o checkout cria o pedido).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {

    @NotNull(message = "Informe o produto")
    private Long productId;

    @NotNull(message = "Informe a quantidade")
    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantity;
}
