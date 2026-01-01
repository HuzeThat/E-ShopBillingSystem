package com.shopbilling.dao;

import com.shopbilling.database.DatabaseManager;
import com.shopbilling.model.Bill;
import com.shopbilling.model.BillItem;
import com.shopbilling.model.Customer;
import com.shopbilling.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
  private final Connection connection;
  private final CustomerDAO customerDAO;
  private final ProductDAO productDAO;
  
  public BillDAO() {
    this.connection = DatabaseManager.getInstance().getConnection();
    this.customerDAO = new CustomerDAO();
    this.productDAO = new ProductDAO();
  }
  
  public BigDecimal getTotalRevenue() {
    String query = "SELECT SUM(final_amount) FROM bills WHERE status = 'COMPLETED'";
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getBigDecimal(1);
      }
    } catch (SQLException e) {
      System.err.println("Error getting total revenue: " + e.getMessage());
    }
    return BigDecimal.ZERO;
  }

  public BigDecimal getHighestBillAmount() {
    String query = "SELECT MAX(final_amount) FROM bills WHERE status = 'COMPLETED'";
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getBigDecimal(1);
      }
    } catch (SQLException e) {
      System.err.println("Error getting highest bill amount: " + e.getMessage());
    }
    return BigDecimal.ZERO;
  }

  public BigDecimal getLowestBillAmount() {
    String query = "SELECT MIN(final_amount) FROM bills WHERE status = 'COMPLETED'";
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return rs.getBigDecimal(1);
      }
    } catch (SQLException e) {
      System.err.println("Error getting lowest bill amount: " + e.getMessage());
    }
    return BigDecimal.ZERO;
  }

  public boolean saveBill(Bill bill) {
    // Check if created_by_user_id column exists
    boolean hasCreatedByColumn = checkColumnExists("bills", "created_by_user_id");
    
    String billQuery;
    if (hasCreatedByColumn) {
        billQuery = "INSERT INTO bills (customer_id, created_by_user_id, total_amount, discount_amount, tax_amount, final_amount, payment_method, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    } else {
        billQuery = "INSERT INTO bills (customer_id, total_amount, discount_amount, tax_amount, final_amount, payment_method, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }
    
    String itemQuery = "INSERT INTO bill_items (bill_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
    
    try {
      connection.setAutoCommit(false);
      
      // Insert bill
      try (PreparedStatement billStmt = connection.prepareStatement(billQuery, Statement.RETURN_GENERATED_KEYS)) {
        int paramIndex = 1;
        billStmt.setInt(paramIndex++, bill.getCustomerId());
        
        if (hasCreatedByColumn) {
            // Handle created_by_user_id (can be 0 or null if not set)
            if (bill.getCreatedByUserId() > 0) {
                billStmt.setInt(paramIndex++, bill.getCreatedByUserId());
            } else {
                billStmt.setNull(paramIndex++, Types.INTEGER);
            }
        }
        
        billStmt.setBigDecimal(paramIndex++, bill.getTotalAmount());
        billStmt.setBigDecimal(paramIndex++, bill.getDiscountAmount());
        billStmt.setBigDecimal(paramIndex++, bill.getTaxAmount());
        billStmt.setBigDecimal(paramIndex++, bill.getFinalAmount());
        billStmt.setString(paramIndex++, bill.getPaymentMethod());
        billStmt.setString(paramIndex++, bill.getStatus());
        
        int affectedRows = billStmt.executeUpdate();
        
        if (affectedRows > 0) {
          try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
              int billId = generatedKeys.getInt(1);
bill.setId(billId);
              
              // Insert bill items
              try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                for (BillItem item : bill.getBillItems()) {
                  itemStmt.setInt(1, billId);
                  itemStmt.setInt(2, item.getProductId());
                  itemStmt.setInt(3, item.getQuantity());
                  itemStmt.setBigDecimal(4, item.getUnitPrice());
                  itemStmt.setBigDecimal(5, item.getTotalPrice());
                  itemStmt.addBatch();
                  
                  // NOTE: Stock is NOT deducted here for PENDING bills.
                  // It will be deducted when the admin confirms the bill.
                }
                itemStmt.executeBatch();
              }
              
              connection.commit();
              return true;
            }
          }
        }
      }
      
      connection.rollback();
      return false;
      
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException rollbackEx) {
        System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
      }
      System.err.println("Error saving bill: " + e.getMessage());
      return false;
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException e) {
        System.err.println("Error resetting auto-commit: " + e.getMessage());
      }
    }
  }
  
  private boolean checkColumnExists(String tableName, String columnName) {
      try {
          DatabaseMetaData meta = connection.getMetaData();
          try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
              return rs.next();
          }
      } catch (SQLException e) {
          return false;
      }
  }
  
  public boolean confirmBill(Bill bill) {
      String updateQuery = "UPDATE bills SET status = 'COMPLETED' WHERE id = ?";
      
      try {
          connection.setAutoCommit(false);
          
          // Update status
          try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
              stmt.setInt(1, bill.getId());
              int rows = stmt.executeUpdate();
              
              if (rows > 0) {
                  // Deduct stock
                  for (BillItem item : bill.getBillItems()) {
                      Product product = item.getProduct();
                      // Fetch current stock to be safe
                      Product currentProduct = productDAO.getProductById(product.getId());
                      if (currentProduct != null) {
                          int newStock = currentProduct.getStockQuantity() - item.getQuantity();
                          productDAO.updateStock(product.getId(), newStock);
                      }
                  }
                  
                  connection.commit();
                  return true;
              }
          }
          
          connection.rollback();
          return false;
          
      } catch (SQLException e) {
          try {
              connection.rollback();
          } catch (SQLException rollbackEx) {
              System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
          }
          System.err.println("Error confirming bill: " + e.getMessage());
          return false;
      } finally {
          try {
              connection.setAutoCommit(true);
          } catch (SQLException e) {
              System.err.println("Error resetting auto-commit: " + e.getMessage());
          }
      }
  }
  
  public boolean deleteBill(int id) {
      String query = "DELETE FROM bills WHERE id = ?";
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
          stmt.setInt(1, id);
          return stmt.executeUpdate() > 0;
      } catch (SQLException e) {
          System.err.println("Error deleting bill: " + e.getMessage());
          return false;
      }
  }
  
  public List<Bill> getAllBills() {
    List<Bill> bills = new ArrayList<>();
    String query = """
SELECT b.*, c.name as customer_name, c.email, c.phone, c.address
      FROM bills b
      LEFT JOIN customers c ON b.customer_id = c.id
      ORDER BY b.bill_date DESC
      """;
    
    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
      
      while (rs.next()) {
        Bill bill = mapResultSetToBill(rs);
        bill.setBillItems(getBillItems(bill.getId()));
        bills.add(bill);
      }
    } catch (SQLException e) {
      System.err.println("Error fetching bills: " + e.getMessage());
    }
    
    return bills;
  }
  
  public List<Bill> getBillsByStatus(String status) {
      List<Bill> bills = new ArrayList<>();
      String query = """
        SELECT b.*, c.name as customer_name, c.email, c.phone, c.address
        FROM bills b
        LEFT JOIN customers c ON b.customer_id = c.id
        WHERE b.status = ?
        ORDER BY b.bill_date DESC
        """;
      
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, status);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
              Bill bill = mapResultSetToBill(rs);
              bill.setBillItems(getBillItems(bill.getId()));
              bills.add(bill);
            }
        }
      } catch (SQLException e) {
        System.err.println("Error fetching bills by status: " + e.getMessage());
      }
      
      return bills;
    }

    public List<Bill> getBillsByCustomerId(int customerId) {
        List<Bill> bills = new ArrayList<>();
        String query = """
            SELECT b.*, c.name as customer_name, c.email, c.phone, c.address
            FROM bills b
            LEFT JOIN customers c ON b.customer_id = c.id
            WHERE b.customer_id = ?
            ORDER BY b.bill_date DESC
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Bill bill = mapResultSetToBill(rs);
                    bill.setBillItems(getBillItems(bill.getId()));
                    bills.add(bill);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bills by customer ID: " + e.getMessage());
        }
return bills;
    }
  
  public Bill getBillById(int id) {
    String query = """
      SELECT b.*, c.name as customer_name, c.email, c.phone, c.address
      FROM bills b
      LEFT JOIN customers c ON b.customer_id = c.id
      WHERE b.id = ?
      """;
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, id);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Bill bill = mapResultSetToBill(rs);
          bill.setBillItems(getBillItems(bill.getId()));
          return bill;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching bill by ID: " + e.getMessage());
    }
    
    return null;
  }
  
  private List<BillItem> getBillItems(int billId) {
    List<BillItem> items = new ArrayList<>();
    String query = """
      SELECT bi.*, p.name, p.description, p.category, p.barcode
      FROM bill_items bi
      JOIN products p ON bi.product_id = p.id
      WHERE bi.bill_id = ?
      """;
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, billId);
      
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          BillItem item = new BillItem();
          item.setId(rs.getInt("id"));
          item.setBillId(rs.getInt("bill_id"));
          item.setProductId(rs.getInt("product_id"));
          item.setQuantity(rs.getInt("quantity"));
          item.setUnitPrice(rs.getBigDecimal("unit_price"));
          item.setTotalPrice(rs.getBigDecimal("total_price"));
          
          // Create product object
          Product product = new Product();
          product.setId(rs.getInt("product_id"));
          product.setName(rs.getString("name"));
          product.setDescription(rs.getString("description"));
          product.setCategory(rs.getString("category"));
          product.setBarcode(rs.getString("barcode"));
          product.setPrice(rs.getBigDecimal("unit_price"));
          
          item.setProduct(product);
          items.add(item);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching bill items: " + e.getMessage());
    }
    
    return items;
  }
  
  private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
    Bill bill = new Bill();
    bill.setId(rs.getInt("id"));
    bill.setCustomerId(rs.getInt("customer_id"));
    
    // Handle created_by_user_id if column exists
    try {
        bill.setCreatedByUserId(rs.getInt("created_by_user_id"));
    } catch (SQLException e) {
        // Ignore if column doesn't exist yet
    }

    bill.setTotalAmount(rs.getBigDecimal("total_amount"));
    bill.setDiscountAmount(rs.getBigDecimal("discount_amount"));
    bill.setTaxAmount(rs.getBigDecimal("tax_amount"));
    bill.setFinalAmount(rs.getBigDecimal("final_amount"));
    bill.setPaymentMethod(rs.getString("payment_method"));
    
    // Check if status column exists in result set before accessing it
    try {
        bill.setStatus(rs.getString("status"));
    } catch (SQLException e) {
        // If status column is missing, default to COMPLETED for backward compatibility
        // or handle as needed. This catch block handles the "Column 'status' not found" error
        // if the database schema hasn't been updated yet.
        bill.setStatus("COMPLETED"); 
    }
    
    Timestamp billDate = rs.getTimestamp("bill_date");
    if (billDate != null) {
      bill.setBillDate(billDate.toLocalDateTime());
    }
    
    // Set customer if exists
    String customerName = rs.getString("customer_name");
    if (customerName != null) {
      Customer customer = new Customer();
      customer.setId(rs.getInt("customer_id"));
      customer.setName(customerName);
      customer.setEmail(rs.getString("email"));
      customer.setPhone(rs.getString("phone"));
      customer.setAddress(rs.getString("address"));
      bill.setCustomer(customer);
    }
    
    return bill;
  }
}