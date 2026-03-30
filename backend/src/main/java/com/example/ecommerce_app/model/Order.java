package com.example.ecommerce_app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Order {

    private Long id;
    private Long userId;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    public Order() {
    }

    public Order(Long id, Long userId, double totalAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

}