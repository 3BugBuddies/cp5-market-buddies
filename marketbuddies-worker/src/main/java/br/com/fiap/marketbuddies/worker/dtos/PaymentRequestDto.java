package br.com.fiap.marketbuddies.worker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private Long orderId;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private List<PaymentItemDto> items;
    private LocalDateTime requestedAt;
}
