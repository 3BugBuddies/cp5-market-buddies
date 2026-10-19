package br.com.fiap.marketbuddies.api.controllers;

import br.com.fiap.marketbuddies.api.dtos.ProductDto;
import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> list(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) Category category) {
        return productService.listCatalog(search, category);
    }
}
