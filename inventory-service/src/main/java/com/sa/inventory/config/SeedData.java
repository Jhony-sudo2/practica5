package com.sa.inventory.config;

import com.sa.inventory.models.Product;
import com.sa.inventory.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class SeedData {
    private final ProductRepository products;

    @Bean
    ApplicationRunner seed() {
        return args -> {
            if (products.count() == 0) {
                create("Teclado", 100, "50.00");
                create("Mouse", 100, "25.00");
                create("Monitor sin stock", 0, "200.00");
            }
        };
    }

    private void create(String name, int stock, String price) {
        Product p = new Product();
        p.setName(name);
        p.setStock(stock);
        p.setPrice(new BigDecimal(price));
        products.save(p);
    }
}
