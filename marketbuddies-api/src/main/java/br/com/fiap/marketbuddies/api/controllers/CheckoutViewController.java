package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.CartDto;
import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.enums.OrderOrigin;
import br.com.fiap.marketbuddies.api.exceptions.OutOfStockException;
import br.com.fiap.marketbuddies.api.facades.PaymentFacade;
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

import java.math.BigDecimal;

// Revisar e pagar. Só CUSTOMER (SecurityConfig); o visitante é mandado para /login?redirect=/checkout.
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutViewController {

    private final CartService cartService;
    private final PaymentFacade paymentFacade;

    @GetMapping
    public String checkout(@AuthenticationPrincipal AuthUser user, Model model, RedirectAttributes redirect) {
        CartDto cart = cartService.view();
        if (cart.isEmpty()) {
            // Avisos de produto que saiu do catálogo: a visão já tirou o item da sessão, então vão no flash
            redirect.addFlashAttribute("notices", cart.notices());
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("customerEmail", user.getUsername());
        return "checkout/checkout";
    }

    // P4: o form leva o total que o cliente viu. Se o preço mudou desde então, volta sem criar pedido.
    @PostMapping
    public String pay(@AuthenticationPrincipal AuthUser user,
                      @RequestParam(required = false) BigDecimal expectedTotal,
                      RedirectAttributes redirect) {
        CartDto cart = cartService.view();
        // Carrinho vazio também barra o segundo POST de um duplo clique (o primeiro já esvaziou)
        if (cart.isEmpty()) {
            redirect.addFlashAttribute("notices", cart.notices());
            return "redirect:/cart";
        }
        if (!cart.notices().isEmpty()) {
            redirect.addFlashAttribute("notices", cart.notices());
            return "redirect:/checkout";
        }
        if (expectedTotal == null || expectedTotal.compareTo(cart.total()) != 0) {
            redirect.addFlashAttribute("error", "Os preços mudaram, confira o total antes de pagar.");
            return "redirect:/checkout";
        }

        OrderDto order;
        try {
            order = paymentFacade.requestPayment(user.getId(), cartService.toOrderItems(cart), OrderOrigin.WEB);
        } catch (OutOfStockException ex) {
            // Regra da tela: sem estoque volta ao carrinho com o motivo; o rollback não baixou nada
            // e o carrinho continua como estava para o cliente ajustar
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }

        cartService.clear();
        return "redirect:/orders/" + order.id();
    }
}
