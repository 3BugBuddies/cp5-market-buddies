package br.com.fiap.marketbuddies.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Formulário de cadastro. A confirmação igual à senha é conferida no UserService.
@Data
public class SignUpDto {

    @NotBlank(message = "Informe seu nome completo")
    @Size(max = 120, message = "O nome pode ter até 120 caracteres")
    private String fullName;

    @NotBlank(message = "Informe seu e-mail")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Informe um e-mail válido, como nome@dominio.com")
    @Size(max = 160, message = "O e-mail pode ter até 160 caracteres")
    private String email;

    // 72 é o limite do BCrypt
    @NotBlank(message = "Informe uma senha")
    @Size(min = 6, max = 72, message = "A senha precisa ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Confirme a senha")
    private String confirmPassword;
}
