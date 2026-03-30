package com.example.ecommerce_app.controller;

import com.example.ecommerce_app.exception.ForbiddenException;
import com.example.ecommerce_app.exception.ProductNotFoundException;
import com.example.ecommerce_app.service.ProductService;
import com.example.ecommerce_app.model.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService productService; // DI

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product, HttpServletRequest request) {
        requireAdmin(request);
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        requireAdmin(request);
        return productService.updateProduct(id, product);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Void> uploadImage(@PathVariable Long id,
                                            @RequestParam("file") MultipartFile file,
                                            HttpServletRequest request) throws IOException {
        requireAdmin(request);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        productService.saveImage(id, file.getBytes(), contentType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] imageData = productService.getImageData(id);
        String imageType = productService.getImageType(id);
        if (imageData == null || imageType == null) {
            throw new ProductNotFoundException("No image for product " + id);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imageType))
                .body(imageData);
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("authenticatedUserRole");
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only admins can manage products");
        }
    }
}

// The flow: request -> controller -> service -> repository -> turn back
// Exceptions handled by @RestControllerAdvice from GlobalExceptionHandler