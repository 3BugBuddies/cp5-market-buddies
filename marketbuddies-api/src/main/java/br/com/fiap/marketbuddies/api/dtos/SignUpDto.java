package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpDto(
        @NotBlank(message = "Informe seu nome completo")
        @Size(max = 120, message = "O nome pode ter até 120 caracteres")
        String fullName,

        @NotBlank(message = "Informe seu e-mail")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Informe um e-mail válido, como nome@dominio.com")
        @Size(max = 160, message = "O e-mail pode ter até 160 caracteres")
        String email,

        @NotBlank(message = "Informe uma senha")
        @Size(min = 6, max = 72, message = "A senha precisa ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "Confirme a senha")
        String confirmPassword) {

    public static SignUpDto empty() {
        return new SignUpDto(null, null, null, null);
    }
}
