package br.com.fiap.marketbuddies.worker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private Long orderId;
    private Boolean approved;
    private String authorizationCode;
    private String reason;
    private LocalDateTime respondedAt;
}
