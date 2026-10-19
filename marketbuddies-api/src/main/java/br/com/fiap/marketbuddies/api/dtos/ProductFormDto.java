package br.com.fiap.marketbuddies.api.dtos;

import br.com.fiap.marketbuddies.api.enums.Category;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

// Formulário do modal de criação/edição. Não tem preço: ele é calculado pela entidade (custo x (1 + markup)).
// version vai num campo oculto para detectar edição concorrente (P3).
@Data
public class ProductFormDto {

    private Long id;

    private Long version;

    @NotBlank(message = "Informe o nome do produto.")
    @Size(max = 80, message = "O nome pode ter até 80 caracteres.")
    private String name;

    @NotNull(message = "Escolha a categoria.")
    private Category category;

    @Size(max = 500, message = "A descrição pode ter até 500 caracteres.")
    private String description;

    @NotNull(message = "Informe o custo.")
    @DecimalMin(value = "0.01", message = "O custo deve ser maior que zero.")
    @Digits(integer = 13, fraction = 2, message = "Use no máximo 2 casas decimais no custo.")
    private BigDecimal cost;

    @NotNull(message = "Informe o markup.")
    @DecimalMin(value = "0", message = "Informe um markup de 0 a 500%.")
    @DecimalMax(value = "500", message = "Informe um markup de 0 a 500%.")
    @Digits(integer = 3, fraction = 2, message = "Use no máximo 2 casas decimais no markup.")
    private BigDecimal markupPercentage;

    // 0 = sem oferta. O checkbox "Em oferta" do modal só mostra ou zera este campo.
    @NotNull(message = "Informe o desconto (0 = sem oferta).")
    @DecimalMin(value = "0", message = "O desconto vai de 0 a 90%.")
    @DecimalMax(value = "90", message = "O desconto vai de 0 a 90%.")
    @Digits(integer = 2, fraction = 2, message = "Use no máximo 2 casas decimais no desconto.")
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @NotNull(message = "Informe o estoque.")
    @Min(value = 0, message = "Informe um estoque inteiro, 0 ou mais.")
    @Max(value = 999999, message = "O estoque pode ser no máximo 999999.")
    private Integer stock;
}
