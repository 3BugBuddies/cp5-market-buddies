package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.enums.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

// Produto para leitura: vitrine, detalhe, landing, tabela da gestão e GET /api/products.
// cost, markupPercentage e version são dados internos: só a gestão os preenche (ProductService.listForAdmin)
// e o @JsonIgnore garante que nunca saiam no JSON da API pública.
@Data
public class ProductDto {

    private Long id;
    private String name;
    private Category category;
    private String description;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private BigDecimal discountPercentage;
    private Integer stock;
    private boolean onSale;

    @JsonIgnore
    private BigDecimal cost;

    @JsonIgnore
    private BigDecimal markupPercentage;

    // Vai no campo oculto do modal de edição (P3).
    @JsonIgnore
    private Long version;
}
