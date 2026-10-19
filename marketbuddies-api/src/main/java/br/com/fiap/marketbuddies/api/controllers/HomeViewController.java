package br.com.fiap.marketbuddies.api.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Destino do forward do accessDeniedHandler (SecurityConfig), que já define o status 403.
    @RequestMapping(value = "/403", method = {RequestMethod.GET, RequestMethod.POST})
    public String forbidden() {
        return "error/403";
    }
}
