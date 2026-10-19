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

    @Transactional(readOnly = true)
    public List<ProductDto> listCatalog(String search, Category category) {
        String name = search == null ? "" : search.trim();
        List<ProductEntity> products = category == null
                ? productRepository.findByNameContainingIgnoreCaseOrderByIdAsc(name)
                : productRepository.findByNameContainingIgnoreCaseAndCategoryOrderByIdAsc(name, category);
        return products.stream().map(p -> ProductDto.from(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> listOffers() {
        return productRepository
                .findTop4ByDiscountPercentageGreaterThanAndStockGreaterThanOrderByDiscountPercentageDescIdAsc(
                        BigDecimal.ZERO, 0)
                .stream().map(p -> ProductDto.from(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findForCatalog(Long id) {
        return ProductDto.from(find(id), false);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> listForAdmin() {
        return productRepository.findAll(Sort.by("id")).stream().map(p -> ProductDto.from(p, true)).toList();
    }

    @Transactional(readOnly = true)
    public ProductFormDto findForm(Long id) {
        return ProductFormDto.from(find(id));
    }

    @Transactional
    public ProductDto create(ProductFormDto form) {
        ProductEntity product = new ProductEntity();
        apply(form, product);
        normalize(product);
        ProductEntity saved = productRepository.save(product);
        log.info("Produto {} criado: {}", saved.getId(), saved.getName());
        return ProductDto.from(saved, true);
    }

    /** Lança conflito de versão se o produto mudou depois que o modal abriu. */
    @Transactional
    public ProductDto update(Long id, ProductFormDto form) {
        ProductEntity product = find(id);
        if (!Objects.equals(form.version(), product.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(ProductEntity.class, id);
        }
        apply(form, product);
        normalize(product);
        productRepository.saveAndFlush(product);
        log.info("Produto {} atualizado", id);
        return ProductDto.from(product, true);
    }

    /** Solta o vínculo dos itens já vendidos antes de excluir; devolve o nome do produto. */
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

    private static void apply(ProductFormDto form, ProductEntity product) {
        product.setName(form.name());
        product.setCategory(form.category());
        product.setDescription(form.description());
        product.setCost(form.cost());
        product.setMarkupPercentage(form.markupPercentage());
        product.setDiscountPercentage(form.discountPercentage());
        product.setStock(form.stock());
    }
}
