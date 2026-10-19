package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.configs.AdminProperties;
import br.com.fiap.marketbuddies.api.configs.DataSeeder;
import br.com.fiap.marketbuddies.api.dtos.SignUpDto;
import br.com.fiap.marketbuddies.api.dtos.TestAccountDto;
import br.com.fiap.marketbuddies.api.exceptions.FieldValidationException;
import br.com.fiap.marketbuddies.api.security.JwtAuthFilter;
import br.com.fiap.marketbuddies.api.security.JwtUtil;
import br.com.fiap.marketbuddies.api.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

// Login próprio das telas: o POST /login gera o JWT e grava no cookie MB_TOKEN (no lugar do formLogin).
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AdminProperties adminProperties;

    // O quadro "Contas de teste" mostra as duas contas do seed (pedido do Gabriel para a correção).
    // As credenciais vêm da configuração e do DataSeeder, nunca escritas no template.
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("testAccounts", List.of(
                new TestAccountDto("Cliente", DataSeeder.TEST_CUSTOMER_EMAIL, DataSeeder.TEST_CUSTOMER_PASSWORD),
                new TestAccountDto("ADMIN", adminProperties.email(), adminProperties.password())));
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String email,
                        @RequestParam(defaultValue = "") String senha,
                        @RequestParam(required = false) String redirect,
                        HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim(), senha));
        } catch (AuthenticationException ex) {
            // Aqui o catch é a regra da tela: credencial errada volta para o login, preservando o destino
            if (isInternalPath(redirect)) {
                return "redirect:/login?error&redirect=" + UriUtils.encodeQueryParam(redirect, StandardCharsets.UTF_8);
            }
            return "redirect:/login?error";
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        response.addHeader(HttpHeaders.SET_COOKIE,
                JwtAuthFilter.tokenCookie(token, jwtUtil.getExpirationMs(), request.isSecure()).toString());

        if (isInternalPath(redirect)) {
            return "redirect:" + redirect;
        }
        boolean admin = user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return admin ? "redirect:/admin/dashboard" : "redirect:/products";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, JwtAuthFilter.clearCookie(request.isSecure()).toString());
        return "redirect:/login?logout";
    }

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("signUpDto", new SignUpDto());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("signUpDto") SignUpDto signUpDto, BindingResult result) {
        if (result.hasErrors()) {
            return "auth/signup";
        }
        try {
            userService.register(signUpDto);
        } catch (FieldValidationException ex) {
            result.rejectValue(ex.getField(), "signup." + ex.getField(), ex.getMessage());
            return "auth/signup";
        }
        return "redirect:/login?registered";
    }

    // Só aceita rota interna ("/checkout"); "//site" e "/\site" levariam o navegador para outro domínio.
    private static boolean isInternalPath(String redirect) {
        return redirect != null
                && redirect.startsWith("/")
                && !redirect.startsWith("//")
                && !redirect.startsWith("/\\");
    }
}
