package com.shopbilling.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
  private int id;
  private String name;
  private String description;
  private BigDecimal price;
  private int stockQuantity;
  private String category;
  private String barcode;
  private String imagePath; // New field for image path
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  // Constructors
  public Product() {}
  
  public Product(String name, String description, BigDecimal price, int stockQuantity, String category, String barcode) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.category = category;
    this.barcode = barcode;
  }
  
  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }
  
  public int getStockQuantity() { return stockQuantity; }
  public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
  
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  
  public String getBarcode() { return barcode; }
  public void setBarcode(String barcode) { this.barcode = barcode; }
  
  public String getImagePath() { return imagePath; }
  public void setImagePath(String imagePath) { this.imagePath = imagePath; }
  
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  
  @Override
  public String toString() {
    return name + "ETB" + price;
  }
}
