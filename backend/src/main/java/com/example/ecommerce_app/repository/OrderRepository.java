package com.example.ecommerce_app.repository;

import com.example.ecommerce_app.model.Order;
import com.example.ecommerce_app.model.OrderItem;
import com.example.ecommerce_app.model.Product;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private final DataSource dataSource;

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Order saveOrder(Order order) {
        String sql = """
                INSERT INTO orders (user_id, total_amount, status)
                VALUES (?, ?, ?)
                RETURNING id, created_at
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, order.getUserId());
            statement.setDouble(2, order.getTotalAmount());
            statement.setString(3, order.getStatus());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    order.setId(rs.getLong("id"));
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        order.setCreatedAt(timestamp.toLocalDateTime());
                    }
                }
            }

            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving order", e);
        }
    }

    public void saveOrderItem(OrderItem orderItem) {
        String sql = """
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderItem.getOrderId());
            statement.setLong(2, orderItem.getProductId());
            statement.setInt(3, orderItem.getQuantity());
            statement.setDouble(4, orderItem.getPrice());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving order item", e);
        }
    }

    public List<Order> findByUserId(Long userId) {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, user_id, total_amount, status, created_at
                FROM orders
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );

                    order.setItems(findItemsByOrderId(order.getId()));
                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching orders by user id", e);
        }

        return orders;
    }

    public List<OrderItem> findItemsByOrderId(Long orderId) {
        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price,
                       p.name, p.description, p.stock
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                ORDER BY oi.id
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getLong("product_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock")
                    );

                    OrderItem item = new OrderItem(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            product
                    );

                    items.add(item);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching order items", e);
        }

        return items;
    }
}
