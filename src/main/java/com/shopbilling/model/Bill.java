package com.shopbilling.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {
  private int id;
  private int customerId;
  private int createdByUserId;
  private Customer customer;
  private BigDecimal totalAmount;
  private BigDecimal discountAmount;
  private BigDecimal taxAmount;
  private BigDecimal finalAmount;
  private String paymentMethod;
  private LocalDateTime billDate;
  private List<BillItem> billItems;
  private String status; // PENDING or COMPLETED
  
  // Constructors
  public Bill() {
    this.billItems = new ArrayList<>();
    this.totalAmount = BigDecimal.ZERO;
    this.discountAmount = BigDecimal.ZERO;
    this.taxAmount = BigDecimal.ZERO;
    this.finalAmount = BigDecimal.ZERO;
    this.paymentMethod = "CASH";
    this.status = "PENDING";
  }
  
  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  
  public int getCustomerId() { return customerId; }
  public void setCustomerId(int customerId) { this.customerId = customerId; }
  
  public int getCreatedByUserId() { return createdByUserId; }
  public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
  
  public Customer getCustomer() { return customer; }
  public void setCustomer(Customer customer) { this.customer = customer; }
  
  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
  
  public BigDecimal getDiscountAmount() { return discountAmount; }
  public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
  
  public BigDecimal getTaxAmount() { return taxAmount; }
  public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
  
  public BigDecimal getFinalAmount() { return finalAmount; }
  public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
  
  public String getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
  
  public LocalDateTime getBillDate() { return billDate; }
  public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
  
  public List<BillItem> getBillItems() { return billItems; }
  public void setBillItems(List<BillItem> billItems) { this.billItems = billItems; }
  
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  
  public void addBillItem(BillItem item) {
    this.billItems.add(item);
    calculateTotals();
  }
  
  public void removeBillItem(BillItem item) {
    this.billItems.remove(item);
    calculateTotals();
  }
  
  public void calculateTotals() {
    totalAmount = billItems.stream()
        .map(BillItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Calculate tax (12% GST)
    taxAmount = totalAmount.multiply(new BigDecimal("0.12"));
    
    // Calculate final amount
    finalAmount = totalAmount.add(taxAmount).subtract(discountAmount);
  }
}
