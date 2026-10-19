package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

// Produto para leitura: vitrine, detalhe, landing, tabela da gestão e GET /api/products.
// cost, markupPercentage e version são dados internos: só a gestão os preenche (ProductService.listForAdmin)
// e o @JsonIgnore garante que nunca saiam no JSON da API pública.
// version vai no campo oculto do modal de edição (P3).
// Ordem alfabética explícita: a mesma do JSON da classe com @Data. Sem a lista, o Jackson 3 põe os
// componentes do record primeiro, na ordem do construtor (alphabetic = true não basta).
@JsonPropertyOrder({"category", "description", "discountPercentage", "finalPrice", "id", "imagePath", "name", "onSale", "price",
        "stock"})
public record ProductDto(
        Long id,
        String name,
        Category category,
        String description,
        BigDecimal price,
        BigDecimal finalPrice,
        BigDecimal discountPercentage,
        Integer stock,
        Boolean onSale,
        String imagePath,
        @JsonIgnore BigDecimal cost,
        @JsonIgnore BigDecimal markupPercentage,
        @JsonIgnore Long version) {

    // withInternalData = false deixa custo, markup e versão nulos (vitrine e API pública).
    public static ProductDto from(ProductEntity product, boolean withInternalData) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getPrice(),
                product.getFinalPrice(),
                product.getDiscountPercentage(),
                product.getStock(),
                product.isOnSale(),
                product.getImagePath(),
                withInternalData ? product.getCost() : null,
                withInternalData ? product.getMarkupPercentage() : null,
                withInternalData ? product.getVersion() : null);
    }
}
