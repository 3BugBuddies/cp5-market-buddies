package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Formulário do modal de criação/edição. Não tem preço: ele é calculado pela entidade (custo x (1 + markup)).
// version vai num campo oculto para detectar edição concorrente (P3).
// discountPercentage: 0 = sem oferta. O checkbox "Em oferta" do modal só mostra ou zera este campo.
public record ProductFormDto(
        Long id,

        Long version,

        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 80, message = "O nome pode ter até 80 caracteres.")
        String name,

        @NotNull(message = "Escolha a categoria.")
        Category category,

        @Size(max = 500, message = "A descrição pode ter até 500 caracteres.")
        String description,

        @NotNull(message = "Informe o custo.")
        @DecimalMin(value = "0.01", message = "O custo deve ser maior que zero.")
        @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais no custo.")
        BigDecimal cost,

        @NotNull(message = "Informe o markup.")
        @DecimalMin(value = "0", message = "Informe um markup de 0 a 500%.")
        @DecimalMax(value = "500", message = "Informe um markup de 0 a 500%.")
        @Digits(integer = 3, fraction = 2, message = "Use no máximo 2 casas decimais no markup.")
        BigDecimal markupPercentage,

        @NotNull(message = "Informe o desconto (0 = sem oferta).")
        @DecimalMin(value = "0", message = "O desconto vai de 0 a 90%.")
        @DecimalMax(value = "90", message = "O desconto vai de 0 a 90%.")
        @Digits(integer = 2, fraction = 2, message = "Use no máximo 2 casas decimais no desconto.")
        BigDecimal discountPercentage,

        @NotNull(message = "Informe o estoque.")
        @Min(value = 0, message = "Informe um estoque inteiro, 0 ou mais.")
        @Max(value = 999999, message = "O estoque pode ser no máximo 999999.")
        Integer stock) {

    // Modal vazio de "Novo produto": começa sem oferta.
    public static ProductFormDto empty() {
        return new ProductFormDto(null, null, null, null, null, null, null, BigDecimal.ZERO, null);
    }

    // Valores atuais do banco, para reabrir o modal depois de um conflito de versão.
    public static ProductFormDto from(ProductEntity product) {
        return new ProductFormDto(
                product.getId(),
                product.getVersion(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getCost(),
                product.getMarkupPercentage(),
                product.getDiscountPercentage(),
                product.getStock());
    }

    public ProductFormDto withId(Long newId) {
        return new ProductFormDto(newId, version, name, category, description, cost, markupPercentage,
                discountPercentage, stock);
    }
}
