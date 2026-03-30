package com.example.ecommerce_app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddToCartRequest {

    private Long userId;
    private Long productId;
    private int quantity;

}