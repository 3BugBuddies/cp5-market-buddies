package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Corpo do POST /api/auth/login.
@Data
public class LoginDto {

    @NotBlank(message = "Informe o e-mail")
    private String email;

    @NotBlank(message = "Informe a senha")
    private String password;
}
