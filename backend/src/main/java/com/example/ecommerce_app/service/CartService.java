package com.example.ecommerce_app.service;

import com.example.ecommerce_app.dto.AddToCartRequest;
import com.example.ecommerce_app.dto.UpdateCartRequest;
import com.example.ecommerce_app.exception.CartItemNotFoundException;
import com.example.ecommerce_app.exception.ProductNotFoundException;
import com.example.ecommerce_app.model.CartItem;
import com.example.ecommerce_app.model.Product;
import com.example.ecommerce_app.repository.CartRepository;
import com.example.ecommerce_app.repository.ProductRepository;
import com.example.ecommerce_app.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public CartItem addToCart(AddToCartRequest request) {
        validateAddToCartRequest(request);

        Product product = productRepository.findById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException("Product with id " + request.getProductId() + " not found");
        }

        if (request.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        CartItem existingCartItem = cartRepository.findByUserIdAndProductId(
                request.getUserId(),
                request.getProductId()
        );

        if (existingCartItem != null) {
            int newQuantity = existingCartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException("Total quantity in cart exceeds available stock");
            }

            cartRepository.updateQuantity(existingCartItem.getId(), newQuantity);
            existingCartItem.setQuantity(newQuantity);
            return existingCartItem;
        }

        CartItem cartItem = new CartItem();
        cartItem.setUserId(request.getUserId());
        cartItem.setProductId(request.getProductId());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setProduct(product);

        return cartRepository.save(cartItem);
    }

    public List<CartItem> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public CartItem updateCartItem(UpdateCartRequest request, Long authenticatedUserId) {
        if (request.getCartItemId() == null) {
            throw new IllegalArgumentException("Cart item id is required");
        }

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartRepository.findById(request.getCartItemId());
        if (cartItem == null) {
            throw new CartItemNotFoundException("Cart item with id " + request.getCartItemId() + " not found");
        }

        if (!cartItem.getUserId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You can only update your own cart items");
        }

        if (request.getQuantity() > cartItem.getProduct().getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        cartRepository.updateQuantity(cartItem.getId(), request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        return cartItem;
    }

    public void removeCartItem(Long cartItemId,  Long authenticatedUserId) {
        CartItem cartItem = cartRepository.findById(cartItemId);
        if (cartItem == null) {
            throw new CartItemNotFoundException("Cart item with id " + cartItemId + " not found");
        }

        if (!cartItem.getUserId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You can only remove your own cart items");
        }

        cartRepository.deleteById(cartItemId);
    }

    private void validateAddToCartRequest(AddToCartRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User id is required");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required");
        }

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}