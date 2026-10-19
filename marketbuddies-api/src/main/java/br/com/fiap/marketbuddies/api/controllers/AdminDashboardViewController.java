package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Dashboard da gerência. O acesso só de ADMIN vem da regra /admin/** do SecurityConfig.
@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardViewController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.load());
        return "admin/dashboard";
    }
}
