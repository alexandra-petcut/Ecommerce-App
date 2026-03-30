package com.example.ecommerce_app.controller;

import com.example.ecommerce_app.dto.AddToCartRequest;
import com.example.ecommerce_app.dto.UpdateCartRequest;
import com.example.ecommerce_app.exception.ForbiddenException;
import com.example.ecommerce_app.model.CartItem;
import com.example.ecommerce_app.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public CartItem addToCart(@RequestBody AddToCartRequest request, HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        request.setUserId(authenticatedUserId);
        return cartService.addToCart(request);
    }

    @GetMapping("/{userId}")
    public List<CartItem> getCartByUserId(@PathVariable Long userId, HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!authenticatedUserId.equals(userId)) {
            throw new ForbiddenException("You can only access your own cart");
        }
        return cartService.getCartByUserId(userId);
    }

    @PutMapping("/update")
    public CartItem updateCartItem(@RequestBody UpdateCartRequest request, HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        return cartService.updateCartItem(request, authenticatedUserId);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public Map<String, String> removeCartItem(@PathVariable Long cartItemId, HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        cartService.removeCartItem(cartItemId, authenticatedUserId);
        return Map.of("message", "Cart item removed successfully");
    }
}
