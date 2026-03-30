package com.example.ecommerce_app.repository;

import com.example.ecommerce_app.model.CartItem;
import com.example.ecommerce_app.model.Product;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartRepository {

    private final DataSource dataSource;

    public CartRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting cart items by user id", e);
        }
    }

    public CartItem findByUserIdAndProductId(Long userId, Long productId) {
        String sql = """
                SELECT c.id AS cart_id, c.user_id, c.product_id, c.quantity,
                       p.id AS p_id, p.name, p.description, p.price, p.stock
                FROM cart_items c
                JOIN products p ON c.product_id = p.id
                WHERE c.user_id = ? AND c.product_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, productId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCartItem(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding cart item by user and product", e);
        }

        return null;
    }

    public CartItem save(CartItem cartItem) {
        String sql = """
                INSERT INTO cart_items (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItem.getUserId());
            statement.setLong(2, cartItem.getProductId());
            statement.setInt(3, cartItem.getQuantity());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    cartItem.setId(rs.getLong("id"));
                }
            }

            return cartItem;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving cart item", e);
        }
    }

    public void updateQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, cartItemId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating cart item quantity", e);
        }
    }

    public List<CartItem> findByUserId(Long userId) {
        List<CartItem> cartItems = new ArrayList<>();

        String sql = """
                SELECT c.id AS cart_id, c.user_id, c.product_id, c.quantity,
                       p.id AS p_id, p.name, p.description, p.price, p.stock
                FROM cart_items c
                JOIN products p ON c.product_id = p.id
                WHERE c.user_id = ?
                ORDER BY c.id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(mapRowToCartItem(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching cart items by user id", e);
        }

        return cartItems;
    }

    public CartItem findById(Long cartItemId) {
        String sql = """
                SELECT c.id AS cart_id, c.user_id, c.product_id, c.quantity,
                       p.id AS p_id, p.name, p.description, p.price, p.stock
                FROM cart_items c
                JOIN products p ON c.product_id = p.id
                WHERE c.id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCartItem(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding cart item by id", e);
        }

        return null;
    }

    public void deleteById(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cartItemId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting cart item", e);
        }
    }

    private CartItem mapRowToCartItem(ResultSet rs) throws SQLException {
        Product product = new Product(
                rs.getLong("p_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock")
        );

        return new CartItem(
                rs.getLong("cart_id"),
                rs.getLong("user_id"),
                rs.getLong("product_id"),
                rs.getInt("quantity"),
                product
        );
    }
}