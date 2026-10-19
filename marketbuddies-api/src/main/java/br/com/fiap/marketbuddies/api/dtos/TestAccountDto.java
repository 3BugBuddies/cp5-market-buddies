package br.com.fiap.marketbuddies.api.dtos;

// Conta de teste mostrada no quadro da tela de login (cliente e ADMIN do seed).
public record TestAccountDto(
        String label,
        String email,
        String password) {
}
