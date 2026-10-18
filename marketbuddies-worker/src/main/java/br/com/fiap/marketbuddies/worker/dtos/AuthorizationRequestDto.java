package br.com.fiap.marketbuddies.worker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequestDto {
    private Long orderId;
    private BigDecimal amount;
    private String customerEmail;
}
