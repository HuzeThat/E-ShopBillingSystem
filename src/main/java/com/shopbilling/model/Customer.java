package com.shopbilling.model;

import java.time.LocalDateTime;

public class Customer {
  private int id;
  private String name;
  private String username;
  private String email;
  private String phone;
  private String address;
  private String password;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  // Constructors
  public Customer() {}
  
  public Customer(String name, String username, String email, String phone, String address) {
    this.name = name;
    this.username = username;
    this.email = email;
    this.phone = phone;
    this.address = address;
  }
  
  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  
  @Override
  public String toString() {
    return name + (phone != null && !phone.isEmpty() ? " (" + phone + ")" : "");
  }
}
