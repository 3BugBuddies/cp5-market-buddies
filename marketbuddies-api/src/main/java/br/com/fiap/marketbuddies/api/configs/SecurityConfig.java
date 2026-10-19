package br.com.fiap.marketbuddies.api.configs;

import br.com.fiap.marketbuddies.api.controllers.handlers.ApiExceptionHandler;
import br.com.fiap.marketbuddies.api.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Segurança no padrão da Aula 13: JWT, sem sessão de login, filtro antes do UsernamePasswordAuthenticationFilter.
// Diferenças para a aula:
//  - o token vem do header Bearer (API) ou do cookie MB_TOKEN (telas), ver JwtAuthFilter;
//  - não há DaoAuthenticationProvider declarado. Com exatamente um UserDetailsService
//    (UserDetailsServiceImpl) e um PasswordEncoder (UserConfig) no contexto, o Spring Security
//    monta esse provider sozinho e o AuthenticationManager abaixo já o usa;
//  - CSRF ligado nas telas, porque o cookie de autenticação vai junto em toda requisição do navegador.
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JsonMapper jsonMapper) throws Exception {
        http
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation(fixation -> fixation.none()))
                // Telas: token no cookie XSRF-TOKEN e no campo _csrf que o Thymeleaf põe nos forms.
                // API: autenticada por Bearer, que o navegador não envia sozinho.
                // A estratégia nula evita que o Spring trate cada requisição autenticada pelo cookie
                // (até o GET do CSS) como "login novo" e troque o token CSRF: o form já aberto daria 403
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers("/api/**"))
                // POST /logout é do AuthViewController (apaga o cookie); o filtro padrão o interceptaria
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // API
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/emails/**").hasRole("ADMIN")
                        // Telas
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/checkout/**", "/orders/**").hasRole("CUSTOMER")
                        .requestMatchers("/", "/login", "/logout", "/signup", "/products/**", "/cart/**",
                                "/css/**", "/img/**", "/js/**", "/favicon.svg", "/error", "/403").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint(jsonMapper))
                        .accessDeniedHandler(accessDeniedHandler(jsonMapper)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Sem token (ou token inválido): API → 401 JSON; tela → login, voltando depois para onde estava.
    private AuthenticationEntryPoint authenticationEntryPoint(JsonMapper jsonMapper) {
        return (request, response, authException) -> {
            if (isApi(request)) {
                writeJson(response, jsonMapper, HttpStatus.UNAUTHORIZED,
                        "Autenticação necessária: envie o token no header Authorization: Bearer", request);
                return;
            }
            String target = UrlUtils.buildRequestUrl(request);
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + UriUtils.encodeQueryParam(target, StandardCharsets.UTF_8));
        };
    }

    // Logado sem o papel certo: API → 403 JSON; tela → página error/403 com status 403 (forward, não redirect).
    private AccessDeniedHandler accessDeniedHandler(JsonMapper jsonMapper) {
        AccessDeniedHandlerImpl page = new AccessDeniedHandlerImpl();
        page.setErrorPage("/403");
        return (request, response, accessDeniedException) -> {
            if (isApi(request)) {
                writeJson(response, jsonMapper, HttpStatus.FORBIDDEN,
                        "Acesso negado para o seu perfil", request);
                return;
            }
            page.handle(request, response, accessDeniedException);
        };
    }

    private static boolean isApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

    private static void writeJson(HttpServletResponse response, JsonMapper jsonMapper, HttpStatus status,
                                  String message, HttpServletRequest request) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(),
                ApiExceptionHandler.body(status, message, request.getRequestURI(), null));
    }
}
