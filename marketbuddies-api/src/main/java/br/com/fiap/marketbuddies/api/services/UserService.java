package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.SignUpDto;
import br.com.fiap.marketbuddies.api.entities.UserEntity;
import br.com.fiap.marketbuddies.api.enums.Role;
import br.com.fiap.marketbuddies.api.exceptions.FieldValidationException;
import br.com.fiap.marketbuddies.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final String DUPLICATE_EMAIL = "Já existe uma conta com esse e-mail";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Sem @Transactional de propósito: o saveAndFlush roda na transação do próprio repositório,
    // então a violação do unique (dois cadastros ao mesmo tempo) chega aqui e vira o mesmo erro no campo.
    public UserEntity register(SignUpDto dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new FieldValidationException("confirmPassword", "As senhas não conferem");
        }
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new FieldValidationException("email", DUPLICATE_EMAIL);
        }

        UserEntity user = new UserEntity();
        user.setFullName(dto.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.password()));
        // Cadastro público sempre cria cliente; ADMIN só pelo seed
        user.setRole(Role.CUSTOMER);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new FieldValidationException("email", DUPLICATE_EMAIL);
        }
    }
}
