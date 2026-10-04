package com.sa.inventory.services.productservice;

import com.sa.inventory.dto.InventoryDtos.ProductInput;
import com.sa.inventory.exceptions.BusinessException;
import com.sa.inventory.models.Product;
import com.sa.inventory.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    @Transactional
    public Product create(ProductInput r) {
        Product p = new Product();
        p.setName(r.name());
        p.setStock(r.stock());
        p.setPrice(r.price());
        return repository.save(p);
    }

    public Product get(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new BusinessException(404, "Producto no encontrado: " + id));
    }

    public List<Product> all() {
        return repository.findAll();
    }
}
