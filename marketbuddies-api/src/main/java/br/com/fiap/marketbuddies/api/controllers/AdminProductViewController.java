package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.ProductFormDto;
import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Gestão de produtos (ADMIN, regra no SecurityConfig): tabela + modal de criação/edição + modal de exclusão.
// Erro de validação re-renderiza a tela com o modal aberto (modalOpen) e as mensagens nos campos.
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductViewController {

    static final String CONFLICT_MESSAGE =
            "O produto mudou enquanto você editava (uma compra pode ter baixado o estoque). "
                    + "Os valores abaixo são os atuais: confira e salve de novo.";

    private final ProductService productService;

    @GetMapping
    public String list(Model model) {
        return page(model, new ProductFormDto(), false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductFormDto form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        form.setId(null);
        if (result.hasErrors()) {
            return page(model, form, true);
        }
        productService.create(form);
        redirect.addFlashAttribute("success", "Produto salvo");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("productForm") ProductFormDto form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        // O id da rota é o que vale; o do corpo é ignorado
        form.setId(id);
        if (result.hasErrors()) {
            return page(model, form, true);
        }
        try {
            productService.update(id, form);
        } catch (ObjectOptimisticLockingFailureException ex) {
            // P3: em vez de 500, reabre o modal com os valores atuais do banco e a versão nova
            model.addAttribute("conflict", CONFLICT_MESSAGE);
            return page(model, productService.findForm(id), true);
        }
        redirect.addFlashAttribute("success", "Produto salvo");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        String name = productService.delete(id);
        redirect.addFlashAttribute("success", "Produto excluído: " + name);
        return "redirect:/admin/products";
    }

    private String page(Model model, ProductFormDto form, boolean modalOpen) {
        model.addAttribute("products", productService.listForAdmin());
        model.addAttribute("categories", Category.values());
        model.addAttribute("productForm", form);
        model.addAttribute("modalOpen", modalOpen);
        return "admin/products";
    }
}
