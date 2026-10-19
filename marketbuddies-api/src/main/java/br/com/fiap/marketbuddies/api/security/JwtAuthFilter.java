package br.com.fiap.marketbuddies.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "MB_TOKEN";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SecurityContextRepository contextRepository = new RequestAttributeSecurityContextRepository();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean fromCookie = false;
        String token;

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length());
        } else {
            token = readCookie(request);
            fromCookie = token != null;
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean authenticated = authenticate(token, request, response);
            if (!authenticated && fromCookie) {
                response.addHeader(HttpHeaders.SET_COOKIE, clearCookie(request.isSecure()).toString());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticate(String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtUtil.isValid(token, userDetails)) {
                return false;
            }
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);
            return true;
        } catch (Exception ex) {
            log.debug("JWT rejeitado: {}", ex.getMessage());
            return false;
        }
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }

    public static ResponseCookie tokenCookie(String token, long expirationMs, boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
    }

    public static ResponseCookie clearCookie(boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
