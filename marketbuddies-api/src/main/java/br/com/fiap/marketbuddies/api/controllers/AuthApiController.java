package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.LoginDto;
import br.com.fiap.marketbuddies.api.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// Igual ao AuthController da Aula 13. Credencial errada lança AuthenticationException,
// que o ApiExceptionHandler devolve como 401 "Usuário ou senha inválidos".
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword()));

        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", token);
        body.put("tokenType", "Bearer");
        body.put("expiresIn", jwtUtil.getExpirationMs());
        return ResponseEntity.ok(body);
    }
}
