package br.com.fiap.marketbuddies.api.configs;

import br.com.fiap.marketbuddies.api.entities.ProductEntity;
import br.com.fiap.marketbuddies.api.entities.UserEntity;
import br.com.fiap.marketbuddies.api.enums.Category;
import br.com.fiap.marketbuddies.api.enums.Role;
import br.com.fiap.marketbuddies.api.repositories.ProductRepository;
import br.com.fiap.marketbuddies.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Dados iniciais: só insere quando a tabela respectiva está vazia.
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    // Conta de cliente de teste, à mostra na tela de login (AuthViewController) para a correção.
    public static final String TEST_CUSTOMER_EMAIL = "cliente@marketbuddies.com";
    public static final String TEST_CUSTOMER_PASSWORD = "cliente123";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(List.of(
                user("Gerência", adminProperties.email(), adminProperties.password(), Role.ADMIN),
                user("Ana Souza", TEST_CUSTOMER_EMAIL, TEST_CUSTOMER_PASSWORD, Role.CUSTOMER)
        ));
        log.info("Seed: ADMIN {} e cliente de teste criados", adminProperties.email());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(List.of(
                product("Banana prata (kg)", Category.PRODUCE, "4.20", "40", "0", 32,
                        "Banana prata selecionada, vendida por quilo. Madura em 1 a 2 dias."),
                product("Maçã gala (kg)", Category.PRODUCE, "7.90", "38", "10", 21, null),
                product("Detergente neutro 500 ml", Category.CLEANING, "2.10", "60", "0", 48, null),
                product("Sabão em pó 1 kg", Category.CLEANING, "11.00", "50", "0", 0, null),
                product("Água mineral sem gás 1,5 L", Category.BEVERAGES, "1.40", "80", "15", 120, null),
                product("Pão de forma integral", Category.BAKERY, "6.50", "45", "10", 9, null),
                product("Café torrado e moído 500 g", Category.GROCERY, "14.90", "35", "7", 18, null),
                product("Leite integral 1 L", Category.GROCERY, "3.80", "30", "0", 4, null)
        ));
        log.info("Seed: 8 produtos criados");
    }

    private UserEntity user(String fullName, String email, String rawPassword, Role role) {
        UserEntity user = new UserEntity();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }

    private ProductEntity product(String name, Category category, String cost, String markup,
                                  String discount, int stock, String description) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setCategory(category);
        product.setCost(new BigDecimal(cost));
        product.setMarkupPercentage(new BigDecimal(markup));
        product.setDiscountPercentage(new BigDecimal(discount));
        product.setStock(stock);
        product.setDescription(description);
        return product;
    }
}
