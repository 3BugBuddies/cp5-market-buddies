package br.com.fiap.marketbuddies.api.security;

import br.com.fiap.marketbuddies.api.configs.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Mesmo papel do JwtUtil da Aula 13, com a API da jjwt 0.12 (a 0.11 do professor está descontinuada).
// Token HS256: sub = e-mail, claim role = CUSTOMER | ADMIN.
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(JwtProperties properties) {
        // HS256 exige chave de 32+ bytes; Keys.hmacShaKeyFor falha na subida se for menor
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse(null);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                // Fixo em HS256: sem o algoritmo, a jjwt escolhe pelo tamanho da chave (HS384 para 48+ bytes)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // Lança JwtException se a assinatura não bate ou o token expirou.
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parse(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
