package com.sa.inventory.services.productservice;

import com.sa.inventory.dto.InventoryDtos.ProductInput;
import com.sa.inventory.models.Product;

import java.util.List;

public interface ProductService {
    Product create(ProductInput request);

    Product get(Long id);

    List<Product> all();
}
