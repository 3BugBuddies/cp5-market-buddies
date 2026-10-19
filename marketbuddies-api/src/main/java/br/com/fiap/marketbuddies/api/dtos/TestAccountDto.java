package br.com.fiap.marketbuddies.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Conta de teste mostrada no quadro da tela de login (cliente e ADMIN do seed).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAccountDto {

    private String label;
    private String email;
    private String password;
}
