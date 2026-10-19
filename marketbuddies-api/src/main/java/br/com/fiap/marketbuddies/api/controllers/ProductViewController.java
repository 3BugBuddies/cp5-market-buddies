package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Vitrine e detalhe, públicos.
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;

    @GetMapping
    public String catalog(@RequestParam(required = false) String search,
                          @RequestParam(required = false) Category category,
                          Model model) {
        model.addAttribute("products", productService.listCatalog(search, category));
        model.addAttribute("search", search == null ? "" : search.trim());
        model.addAttribute("category", category);
        model.addAttribute("categories", Category.values());
        return "products/catalog";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findForCatalog(id));
        return "products/detail";
    }
}
