package br.com.fiap.marketbuddies.worker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
