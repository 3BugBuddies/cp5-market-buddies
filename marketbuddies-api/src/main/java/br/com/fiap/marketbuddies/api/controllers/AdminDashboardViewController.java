package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.enums.StatusEmail;
import br.com.fiap.marketbuddies.api.exceptions.BusinessException;
import br.com.fiap.marketbuddies.api.exceptions.NotFoundException;
import br.com.fiap.marketbuddies.api.facades.PaymentFacade;
import br.com.fiap.marketbuddies.api.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminDashboardViewController {

    private final DashboardService dashboardService;
    private final PaymentFacade paymentFacade;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.load());
        return "admin/dashboard";
    }

    @PostMapping("/admin/orders/{id}/resend-email")
    public String resendEmail(@PathVariable Long id, @RequestParam(required = false) String back,
                              RedirectAttributes redirect) {
        try {
            StatusEmail status = paymentFacade.resendEmail(id);
            if (status == StatusEmail.SENT) {
                redirect.addFlashAttribute("success", "E-mail do pedido #" + id + " reenviado.");
            } else {
                redirect.addFlashAttribute("error", "O reenvio do e-mail do pedido #" + id
                        + " falhou de novo. A tentativa ficou registrada no controle de e-mails.");
            }
        } catch (BusinessException | NotFoundException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "orders".equals(back) ? "redirect:/admin/orders" : "redirect:/admin/dashboard";
    }
}
