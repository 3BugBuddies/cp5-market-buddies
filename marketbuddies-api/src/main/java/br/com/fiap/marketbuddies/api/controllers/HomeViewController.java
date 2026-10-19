package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final ProductService productService;

    // Landing: "Ofertas da semana" vem dos produtos em oferta do banco.
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("offers", productService.listOffers());
        return "index";
    }

    // Destino do forward do accessDeniedHandler (SecurityConfig), que já define o status 403.
    @RequestMapping(value = "/403", method = {RequestMethod.GET, RequestMethod.POST})
    public String forbidden() {
        return "error/403";
    }
}
