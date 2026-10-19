package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.OrderDto;
import br.com.fiap.marketbuddies.api.dtos.RetryItemDto;
import br.com.fiap.marketbuddies.api.security.AuthUser;
import br.com.fiap.marketbuddies.api.services.CartService;
import br.com.fiap.marketbuddies.api.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

// Tela do pedido (aguardando · aprovado · recusado). Só CUSTOMER (SecurityConfig) e só o dono:
// o pedido de outro cliente responde 404, como se não existisse (OrderService.getOrder).
@Controller
@RequestMapping("/orders/{id}")
@RequiredArgsConstructor
public class OrderViewController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public String order(@PathVariable Long id, @AuthenticationPrincipal AuthUser user, Model model) {
        OrderDto order = orderService.getOrder(id, user.getId(), false);
        model.addAttribute("order", order);
        return "orders/order";
    }

    // Consultado a cada 2 s pela tela "aguardando" (marketbuddies.js); quando muda, a tela recarrega.
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> status(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return Map.of("status", orderService.getOrder(id, user.getId(), false).status());
    }

    // "Tentar de novo": remonta o carrinho com os itens do pedido recusado e leva ao checkout.
    @PostMapping("/retry")
    public String retry(@PathVariable Long id, @AuthenticationPrincipal AuthUser user, RedirectAttributes redirect) {
        List<RetryItemDto> items = orderService.retryItems(id, user.getId());
        List<String> warnings = cartService.refill(items);
        if (!warnings.isEmpty()) {
            redirect.addFlashAttribute("notices", warnings);
        }
        return "redirect:/checkout";
    }
}
