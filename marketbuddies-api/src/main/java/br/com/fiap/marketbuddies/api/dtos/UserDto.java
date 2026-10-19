package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.UserEntity;
import br.com.fiap.marketbuddies.api.enums.Role;

public record UserDto(
        Long id,
        String fullName,
        String email,
        Role role) {

    public static UserDto from(UserEntity user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
