package br.com.fiap.marketbuddies.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Item da mensagem de pedido de pagamento. Mesmos campos do PaymentItemDto do worker.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
