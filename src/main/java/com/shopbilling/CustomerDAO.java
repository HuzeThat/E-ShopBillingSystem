package com.shopbilling.dao;

import com.shopbilling.database.DatabaseManager;
import com.shopbilling.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
  private final Connection connection;
  
  public CustomerDAO() {
    this.connection = DatabaseManager.getInstance().getConnection();
  }
  
  public int getTotalCustomers() {
    String query = "SELECT COUNT(*) FROM customers";
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      System.err.println("Error getting total customers: " + e.getMessage());
    }
    return 0;
  }

  public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<>();
    String query = "SELECT * FROM customers ORDER BY name";
    
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      
      while (rs.next()) {
        customers.add(mapResultSetToCustomer(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching customers: " + e.getMessage());
    }
    
    return customers;
  }
  
  public Customer getCustomerById(int id) {
    String query = "SELECT * FROM customers WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, id);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToCustomer(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching customer by ID: " + e.getMessage());
    }
    
    return null;
  }

  public Customer login(String username, String password) {
    String query = "SELECT * FROM customers WHERE username = ? AND password = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setString(1, username);
      stmt.setString(2, password);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToCustomer(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching customer by username and password: " + e.getMessage());
    }
    
    return null;
  }
  
  public List<Customer> searchCustomers(String searchTerm) {
    List<Customer> customers = new ArrayList<>();
    String query = "SELECT * FROM customers WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR username LIKE ? ORDER BY name";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      String searchPattern = "%" + searchTerm + "%";
      stmt.setString(1, searchPattern);
      stmt.setString(2, searchPattern);
      stmt.setString(3, searchPattern);
      stmt.setString(4, searchPattern);
      
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          customers.add(mapResultSetToCustomer(rs));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error searching customers: " + e.getMessage());
    }
    
    return customers;
  }
  
  public boolean addCustomer(Customer customer) {
    String query = "INSERT INTO customers (name, username, email, phone, address, password) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, customer.getName());
      stmt.setString(2, customer.getUsername());
      stmt.setString(3, customer.getEmail());
      stmt.setString(4, customer.getPhone());
      stmt.setString(5, customer.getAddress());
      stmt.setString(6, customer.getPassword());
      
      int affectedRows = stmt.executeUpdate();
      
      if (affectedRows > 0) {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            customer.setId(generatedKeys.getInt(1));
          }
        }
        return true;
      }
    } catch (SQLException e) {
System.err.println("Error adding customer: " + e.getMessage());
    }
    
    return false;
  }
  
  public boolean updateCustomer(Customer customer) {
    String query = "UPDATE customers SET name = ?, username = ?, email = ?, phone = ?, address = ?, password = ? WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setString(1, customer.getName());
      stmt.setString(2, customer.getUsername());
      stmt.setString(3, customer.getEmail());
      stmt.setString(4, customer.getPhone());
      stmt.setString(5, customer.getAddress());
      stmt.setString(6, customer.getPassword());
      stmt.setInt(7, customer.getId());
      
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error updating customer: " + e.getMessage());
    }
    
    return false;
  }
  
  public boolean deleteCustomer(int id) {
    String query = "DELETE FROM customers WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, id);
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("Error deleting customer: " + e.getMessage());
    }
    
    return false;
  }
  
  private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
    Customer customer = new Customer();
    customer.setId(rs.getInt("id"));
    customer.setName(rs.getString("name"));
    customer.setUsername(rs.getString("username"));
    customer.setEmail(rs.getString("email"));
    customer.setPhone(rs.getString("phone"));
    customer.setAddress(rs.getString("address"));
    customer.setPassword(rs.getString("password"));
    
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      customer.setCreatedAt(createdAt.toLocalDateTime());
    }
    
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      customer.setUpdatedAt(updatedAt.toLocalDateTime());
    }
    
    return customer;
  }
}
