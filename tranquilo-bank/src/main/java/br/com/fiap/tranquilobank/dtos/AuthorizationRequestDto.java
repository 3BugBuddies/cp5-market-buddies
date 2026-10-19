package br.com.fiap.tranquilobank.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AuthorizationRequestDto(

		@NotNull(message = "O id do pedido é obrigatório.")
		Long orderId,

		@NotNull(message = "O valor é obrigatório.")
		@Positive(message = "O valor deve ser maior que zero.")
		BigDecimal amount,

		@NotBlank(message = "O e-mail do cliente é obrigatório.")
		@Email(message = "O e-mail do cliente é inválido.")
		String customerEmail) {
}
