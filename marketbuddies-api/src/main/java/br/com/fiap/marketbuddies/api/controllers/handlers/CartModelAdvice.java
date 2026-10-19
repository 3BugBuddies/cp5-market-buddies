package br.com.fiap.marketbuddies.api.controllers.handlers;

import br.com.fiap.marketbuddies.api.dtos.CartDto;
import br.com.fiap.marketbuddies.api.services.CartService;
import br.com.fiap.marketbuddies.api.services.CartSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CartModelAdvice {

    private final CartSession cartSession;
    private final CartService cartService;

    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request) {
        if (!hasCart(request)) {
            return 0;
        }
        return cartSession.count();
    }

    /** Resumo do carrinho para o preview da navbar; null quando o carrinho está vazio. */
    @ModelAttribute("cartPreview")
    public CartDto cartPreview(HttpServletRequest request) {
        if (!hasCart(request) || cartSession.isEmpty()) {
            return null;
        }
        return cartService.preview();
    }

    private static boolean hasCart(HttpServletRequest request) {
        return request.getSession(false) != null && !request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }
}
