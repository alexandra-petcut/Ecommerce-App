package com.example.ecommerce_app.controller;

import com.example.ecommerce_app.exception.ForbiddenException;
import com.example.ecommerce_app.model.Order;
import com.example.ecommerce_app.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout/{userId}")
    public Order checkout(@PathVariable Long userId, HttpServletRequest request) {
        Long authenticatedUserId = (Long) request.getAttribute("authenticatedUserId");
        if (!authenticatedUserId.equals(userId)) {
            throw new ForbiddenException("You can only checkout for your own account");
        }
        return orderService.checkout(userId);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId, HttpServletRequest request) {
        Long authenticatedUserId = (Long) request.getAttribute("authenticatedUserId");
        if (!authenticatedUserId.equals(userId)) {
            throw new ForbiddenException("You can only view your own orders");
        }
        return orderService.getOrdersByUserId(userId);
    }
}