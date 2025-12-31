package com.shopbilling.dao;

import com.shopbilling.database.DatabaseManager;
import com.shopbilling.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
  private final Connection connection;
  
  public ProductDAO() {
    this.connection = DatabaseManager.getInstance().getConnection();
  }
  
  public int getTotalProducts() {
    String query = "SELECT COUNT(*) FROM products";
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      System.err.println("Error getting total products: " + e.getMessage());
    }
    return 0;
  }

  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    String query = "SELECT * FROM products ORDER BY name";
    
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      
      while (rs.next()) {
        products.add(mapResultSetToProduct(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching products: " + e.getMessage());
    }
    
    return products;
  }
  
  public List<Product> getAllProductsSorted(String sortOption) {
    List<Product> products = new ArrayList<>();
    String orderByClause = "ORDER BY name"; // Default
    
    switch (sortOption) {
        case "Latest":
            orderByClause = "ORDER BY created_at DESC";
            break;
        case "Price: Low to High":
            orderByClause = "ORDER BY price ASC";
            break;
        case "Popularity":
            // Assuming popularity is based on stock quantity for now, or we could join with bill_items
            // For simplicity, let's just order by stock quantity descending as a placeholder
            // A better implementation would be: 
            // SELECT p.*, SUM(bi.quantity) as total_sold FROM products p LEFT JOIN bill_items bi ON p.id = bi.product_id GROUP BY p.id ORDER BY total_sold DESC
             orderByClause = "ORDER BY stock_quantity DESC"; 
            break;
        default:
            orderByClause = "ORDER BY name";
    }
    
    String query = "SELECT * FROM products " + orderByClause;
    
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      
      while (rs.next()) {
        products.add(mapResultSetToProduct(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching sorted products: " + e.getMessage());
    }
    
    return products;
  }
  
  public Product getProductById(int id) {
    String query = "SELECT * FROM products WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, id);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToProduct(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching product by ID: " + e.getMessage());
    }
    
    return null;
  }
  
  public Product getProductByBarcode(String barcode) {
    String query = "SELECT * FROM products WHERE barcode = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setString(1, barcode);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToProduct(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching product by barcode: " + e.getMessage());
    }
    
    return null;
  }
  
  public boolean isBarcodeExists(String barcode) {
    String query = "SELECT COUNT(*) FROM products WHERE barcode = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setString(1, barcode);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error checking if barcode exists: " + e.getMessage());
    }
    return false;
  }

  public List<Product> searchProducts(String searchTerm) {
    List<Product> products = new ArrayList<>();
    String query = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ? OR category LIKE ? ORDER BY name";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      String searchPattern = "%" + searchTerm + "%";
      stmt.setString(1, searchPattern);
      stmt.setString(2, searchPattern);
      stmt.setString(3, searchPattern);
      
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          products.add(mapResultSetToProduct(rs));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error searching products: " + e.getMessage());
    }
    
    return products;
  }

  public List<Product> searchProductsStartsWith(String searchTerm) {
    List<Product> products = new ArrayList<>();
    String query = "SELECT * FROM products WHERE name LIKE ? ORDER BY name";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      String searchPattern = searchTerm + "%";
      stmt.setString(1, searchPattern);
      
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          products.add(mapResultSetToProduct(rs));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error searching products (starts with): " + e.getMessage());
    }
    
    return products;
  }
  
  public boolean addProduct(Product product) {
    String query = "INSERT INTO products (name, description, price, stock_quantity, category, barcode, image_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    
    try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, product.getName());
      stmt.setString(2, product.getDescription());
      stmt.setBigDecimal(3, product.getPrice());
      stmt.setInt(4, product.getStockQuantity());
      stmt.setString(5, product.getCategory());
      stmt.setString(6, product.getBarcode());
      
      if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
          stmt.setString(7, product.getImagePath());
      } else {
          stmt.setNull(7, Types.VARCHAR);
      }
      
      int affectedRows = stmt.executeUpdate();
      
      if (affectedRows > 0) {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            product.setId(generatedKeys.getInt(1));
          }
        }
        return true;
      }
    } catch (SQLException e) {
      System.err.println("Error adding product: " + e.getMessage());
    }
    
    return false;
  }
  
  public boolean updateProduct(Product product) {
    String query = "UPDATE products SET name = ?, description = ?, price = ?, stock_quantity = ?, category = ?, barcode = ?, image_path = ? WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setString(1, product.getName());
      stmt.setString(2, product.getDescription());
      stmt.setBigDecimal(3, product.getPrice());
      stmt.setInt(4, product.getStockQuantity());
      stmt.setString(5, product.getCategory());
      stmt.setString(6, product.getBarcode());
      
      if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
          stmt.setString(7, product.getImagePath());
      } else {
          stmt.setNull(7, Types.VARCHAR);
      }

      stmt.setInt(8, product.getId());
      
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error updating product: " + e.getMessage());
    }
    
    return false;
  }
  
  public boolean updateStock(int productId, int newQuantity) {
    String query = "UPDATE products SET stock_quantity = ? WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, newQuantity);
      stmt.setInt(2, productId);
      
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error updating stock: " + e.getMessage());
    }
    
    return false;
  }
  
  public boolean deleteProduct(int id) {
    String query = "DELETE FROM products WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, id);
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error deleting product: " + e.getMessage());
    }
    
    return false;
  }
  
  private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
    Product product = new Product();
    product.setId(rs.getInt("id"));
    product.setName(rs.getString("name"));
    product.setDescription(rs.getString("description"));
    product.setPrice(rs.getBigDecimal("price"));
    product.setStockQuantity(rs.getInt("stock_quantity"));
    product.setCategory(rs.getString("category"));
    product.setBarcode(rs.getString("barcode"));
    
    // Check if image_path column exists in result set to avoid errors if schema isn't updated yet
    try {
        product.setImagePath(rs.getString("image_path"));
    } catch (SQLException e) {
        // Column might not exist yet, ignore
    }
    
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      product.setCreatedAt(createdAt.toLocalDateTime());
    }
    
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      product.setUpdatedAt(updatedAt.toLocalDateTime());
    }
    
    return product;
  }
}
