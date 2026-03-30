// talks to the database only

package com.example.ecommerce_app.repository;

import com.example.ecommerce_app.model.Product;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepository {
    private final DataSource dataSource;

    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // iterate through rows and convert each row into a product object
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, description, price, stock FROM products ORDER BY id";

        // try-with-resources for to auto-close conn
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapRowToProduct(resultSet));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching products", e);
            }
        return products;
    }

    public Product findById(Long id) {
        String sql = "SELECT id, name, description, price, stock FROM products WHERE id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id); // put id into ? parameter

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToProduct(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product by id", e);
        }
        return null;
    }

    public Product save(Product product) {
        String sql = """
                INSERT INTO products (name, description, price, stock)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getStock());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    product.setId(resultSet.getLong("id"));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getDouble("price"),
                resultSet.getInt("stock")
        );
    }

    public Product update(Product product) {
        String sql = """
                UPDATE products SET name = ?, description = ?, price = ?, stock = ?
                WHERE id = ?
                RETURNING id, name, description, price, stock
                """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getStock());
            statement.setLong(5, product.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToProduct(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
        return null;
    }

    public void saveImage(Long productId, byte[] imageData, String imageType) {
        String sql = "UPDATE products SET image_data = ?, image_type = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, imageData);
            statement.setString(2, imageType);
            statement.setLong(3, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product image", e);
        }
    }

    public byte[] findImageData(Long productId) {
        String sql = "SELECT image_data FROM products WHERE id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBytes("image_data");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product image", e);
        }
        return null;
    }

    public String findImageType(Long productId) {
        String sql = "SELECT image_type FROM products WHERE id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("image_type");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product image type", e);
        }
        return null;
    }

    public void updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newStock);
            statement.setLong(2, productId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating product stock", e);
        }
    }
}
