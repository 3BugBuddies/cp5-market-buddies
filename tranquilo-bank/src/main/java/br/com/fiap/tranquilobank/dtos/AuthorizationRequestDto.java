package br.com.fiap.tranquilobank.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuthorizationRequestDto {

	@NotNull(message = "O id do pedido é obrigatório.")
	private Long orderId;

	@NotNull(message = "O valor é obrigatório.")
	@Positive(message = "O valor deve ser maior que zero.")
	private BigDecimal amount;

	@NotBlank(message = "O e-mail do cliente é obrigatório.")
	@Email(message = "O e-mail do cliente é inválido.")
	private String customerEmail;
}
