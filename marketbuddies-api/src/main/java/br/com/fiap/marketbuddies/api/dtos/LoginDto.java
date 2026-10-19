package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "Informe o e-mail")
        String email,

        @NotBlank(message = "Informe a senha")
        String password) {
}
