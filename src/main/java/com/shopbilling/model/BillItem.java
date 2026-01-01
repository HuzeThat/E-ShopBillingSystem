package com.shopbilling.model;

import java.math.BigDecimal;

public class BillItem {
  private int id;
  private int billId;
  private int productId;
  private Product product;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  
  // Constructors
  public BillItem() {}
  
  public BillItem(Product product, int quantity) {
    this.product = product;
    this.productId = product.getId();
    this.quantity = quantity;
    this.unitPrice = product.getPrice();
    this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
  }
  
  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  
  public int getBillId() { return billId; }
  public void setBillId(int billId) { this.billId = billId; }
  
  public int getProductId() { return productId; }
  public void setProductId(int productId) { this.productId = productId; }
  
  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }
  
  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { 
    this.quantity = quantity;
    if (unitPrice != null) {
      this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
    }
  }
  
  public BigDecimal getUnitPrice() { return unitPrice; }
  public void setUnitPrice(BigDecimal unitPrice) { 
    this.unitPrice = unitPrice;
    if (quantity > 0) {
      this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
    }
  }
  
  public BigDecimal getTotalPrice() { return totalPrice; }
  public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
