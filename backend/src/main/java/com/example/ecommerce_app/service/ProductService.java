// applies rules, makes decisions: What should we do if the product doesn't exist?

package com.example.ecommerce_app.service;

import com.example.ecommerce_app.exception.ProductNotFoundException;
import com.example.ecommerce_app.repository.ProductRepository;
import com.example.ecommerce_app.model.Product;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        return product;
    }

    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id);
        if (existing == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        product.setId(id);
        validateProduct(product);
        return productRepository.update(product);
    }

    public void saveImage(Long id, byte[] imageData, String imageType) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        productRepository.saveImage(id, imageData, imageType);
    }

    public byte[] getImageData(Long id) {
        return productRepository.findImageData(id);
    }

    public String getImageType(Long id) {
        return productRepository.findImageType(id);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }
}