package com.example.ecommerce_app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItem {

    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private Product product;

    public CartItem() {
    }

    public CartItem(Long id, Long userId, Long productId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
