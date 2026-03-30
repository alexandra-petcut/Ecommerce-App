package com.example.ecommerce_app.service;

import com.example.ecommerce_app.exception.EmptyCartException;
import com.example.ecommerce_app.model.CartItem;
import com.example.ecommerce_app.model.Order;
import com.example.ecommerce_app.model.OrderItem;
import com.example.ecommerce_app.model.Product;
import com.example.ecommerce_app.repository.CartRepository;
import com.example.ecommerce_app.repository.OrderRepository;
import com.example.ecommerce_app.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Order checkout(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cannot checkout because cart is empty");
        }

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId());

            if (product == null) {
                throw new IllegalArgumentException("Product with id " + cartItem.getProductId() + " not found");
            }

            if (cartItem.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product: " + product.getName()
                );
            }

            double itemTotal = product.getPrice() * cartItem.getQuantity();
            totalAmount += itemTotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setProduct(product);

            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.saveOrder(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(savedOrder.getId());
            orderRepository.saveOrderItem(orderItem);


            //update stock
            Product product = productRepository.findById(orderItem.getProductId());
            int newStock = product.getStock() - orderItem.getQuantity();
            productRepository.updateStock(product.getId(), newStock);
        }

        cartRepository.deleteByUserId(userId);

        savedOrder.setItems(orderRepository.findItemsByOrderId(savedOrder.getId()));
        return savedOrder;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}