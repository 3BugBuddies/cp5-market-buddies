package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.security.AuthUser;
import br.com.fiap.marketbuddies.api.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Carrinho público (visitante e cliente). ADMIN não compra (Q7): é mandado de volta para a vitrine.
// A vitrine e o detalhe do produto (T5) enviam POST /cart/add com productId e quantity.
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {

    static final String ADMIN_CANNOT_BUY = "Conta de administrador não faz compras.";

    private final CartService cartService;

    @GetMapping
    public String cart(@AuthenticationPrincipal AuthUser user, Model model, RedirectAttributes redirect) {
        if (isAdmin(user)) {
            redirect.addFlashAttribute("error", ADMIN_CANNOT_BUY);
            return "redirect:/products";
        }
        model.addAttribute("cart", cartService.view());
        return "cart/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
                      @AuthenticationPrincipal AuthUser user, RedirectAttributes redirect) {
        if (isAdmin(user)) {
            redirect.addFlashAttribute("error", ADMIN_CANNOT_BUY);
            return "redirect:/products";
        }
        flash(redirect, cartService.add(productId, quantity));
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long productId, @RequestParam int quantity, RedirectAttributes redirect) {
        flash(redirect, cartService.update(productId, quantity));
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId, RedirectAttributes redirect) {
        flash(redirect, cartService.remove(productId));
        return "redirect:/cart";
    }

    private static void flash(RedirectAttributes redirect, CartService.Feedback feedback) {
        if (feedback != null) {
            redirect.addFlashAttribute(feedback.type(), feedback.message());
        }
    }

    // Visitante chega como null (o principal anônimo não é AuthUser).
    private static boolean isAdmin(AuthUser user) {
        return user != null && user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
