package br.com.fiap.marketbuddies.api.controllers.handlers;

import br.com.fiap.marketbuddies.api.services.CartSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Badge de quantidade do carrinho na navbar (fragments/layout.html lê ${cartCount}).
// Sem sessão ainda, o carrinho está vazio: não toca no bean de sessão, para não criar uma sessão
// em cada página aberta por visitante nem em cada chamada da API.
@ControllerAdvice
@RequiredArgsConstructor
public class CartModelAdvice {

    private final CartSession cartSession;

    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request) {
        if (request.getSession(false) == null || request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            return 0;
        }
        return cartSession.count();
    }
}
