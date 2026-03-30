package com.example.ecommerce_app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateCartRequest {

    private Long cartItemId;
    private int quantity;
}
