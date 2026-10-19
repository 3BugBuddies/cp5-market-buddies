package br.com.fiap.marketbuddies.api.services;

import br.com.fiap.marketbuddies.api.dtos.ProductDto;
import br.com.fiap.marketbuddies.api.dtos.ProductFormDto;
import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.exceptions.NotFoundException;
import br.com.fiap.marketbuddies.api.repositories.OrderItemRepository;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    // Vitrine e GET /api/products: sem custo nem markup.
    @Transactional(readOnly = true)
    public List<ProductDto> listCatalog(String search, Category category) {
        String name = search == null ? "" : search.trim();
        List<ProductEntity> products = category == null
                ? productRepository.findByNameContainingIgnoreCaseOrderByIdAsc(name)
                : productRepository.findByNameContainingIgnoreCaseAndCategoryOrderByIdAsc(name, category);
        return products.stream().map(p -> toDto(p, false)).toList();
    }

    // Ofertas da semana na landing.
    @Transactional(readOnly = true)
    public List<ProductDto> listOffers() {
        return productRepository
                .findTop4ByDiscountPercentageGreaterThanAndStockGreaterThanOrderByDiscountPercentageDescIdAsc(
                        BigDecimal.ZERO, 0)
                .stream().map(p -> toDto(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findForCatalog(Long id) {
        return toDto(find(id), false);
    }

    // Tabela da gestão: com custo, markup e versão (só a tela de ADMIN usa).
    @Transactional(readOnly = true)
    public List<ProductDto> listForAdmin() {
        return productRepository.findAll(Sort.by("id")).stream().map(p -> toDto(p, true)).toList();
    }

    // Valores atuais do banco, para reabrir o modal depois de um conflito de versão.
    @Transactional(readOnly = true)
    public ProductFormDto findForm(Long id) {
        ProductFormDto form = new ProductFormDto();
        BeanUtils.copyProperties(find(id), form);
        return form;
    }

    @Transactional
    public ProductDto create(ProductFormDto form) {
        ProductEntity product = new ProductEntity();
        BeanUtils.copyProperties(form, product, "id", "version");
        normalize(product);
        ProductEntity saved = productRepository.save(product);
        log.info("Produto {} criado: {}", saved.getId(), saved.getName());
        return toDto(saved, true);
    }

    // P3: a versão do form precisa ser a do banco. Se uma compra (reserve) ou outro admin mexeu no produto
    // depois que o modal abriu, lança o conflito em vez de gravar o estoque antigo por cima.
    // A corrida entre esta comparação e o flush cai no mesmo ObjectOptimisticLockingFailureException,
    // porque o UPDATE do Hibernate leva "where version = ?".
    @Transactional
    public ProductDto update(Long id, ProductFormDto form) {
        ProductEntity product = find(id);
        if (!Objects.equals(form.getVersion(), product.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(ProductEntity.class, id);
        }
        BeanUtils.copyProperties(form, product, "id", "version");
        normalize(product);
        productRepository.saveAndFlush(product);
        log.info("Produto {} atualizado", id);
        return toDto(product, true);
    }

    // P2: produto já vendido não quebra por FK. Os itens antigos perdem o vínculo (product_id = null)
    // e continuam legíveis pela cópia de nome, categoria, preço e custo. Devolve o nome para o flash.
    @Transactional
    public String delete(Long id) {
        String name = find(id).getName();
        int detached = orderItemRepository.detachProduct(id);
        productRepository.deleteById(id);
        log.info("Produto {} excluído ({} itens de pedido desvinculados)", id, detached);
        return name;
    }

    private ProductEntity find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto " + id + " não encontrado"));
    }

    private static void normalize(ProductEntity product) {
        product.setName(product.getName().trim());
        if (product.getDescription() != null && product.getDescription().isBlank()) {
            product.setDescription(null);
        }
    }

    private static ProductDto toDto(ProductEntity product, boolean withInternalData) {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(product, dto, "cost", "markupPercentage", "version");
        if (withInternalData) {
            dto.setCost(product.getCost());
            dto.setMarkupPercentage(product.getMarkupPercentage());
            dto.setVersion(product.getVersion());
        }
        return dto;
    }
}
