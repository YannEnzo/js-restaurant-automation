/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order
 */
public class Order {
    private String orderId;
    private String tableId;
    private String waiterId;
    private LocalDateTime orderDateTime;
    private String status; // NEW, IN_PROGRESS, READY, DELIVERED, PAID, CANCELLED
    private double totalAmount;
    private double taxAmount;
    private double tipAmount;
    private String paymentMethod; // CASH, CREDIT_CARD
    private String paymentStatus; // PENDING, COMPLETED, REFUNDED
    private LocalDateTime paymentDateTime;
    private List<OrderItem> orderItems;
    
    // Constructors
    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDateTime = LocalDateTime.now();
    }
    
    public Order(String orderId, String tableId, String waiterId) {
        this.orderId = orderId;
        this.tableId = tableId;
        this.waiterId = waiterId;
        this.status = "NEW";
        this.orderDateTime = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getTableId() {
        return tableId;
    }
    
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
    
    public String getWaiterId() {
        return waiterId;
    }
    
    public void setWaiterId(String waiterId) {
        this.waiterId = waiterId;
    }
    
    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }
    
    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public double getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public double getTipAmount() {
        return tipAmount;
    }
    
    public void setTipAmount(double tipAmount) {
        this.tipAmount = tipAmount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }
    
    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = paymentDateTime;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }
    
    // Helper methods
    public double calculateSubtotal() {
        double subtotal = 0;
        for (OrderItem item : orderItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }
    
    public double calculateTax() {
        return calculateSubtotal() * 0.10; // Assuming 10% tax
    }
    
    public double calculateTotal() {
        return calculateSubtotal() + calculateTax();
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", waiterId='" + waiterId + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", items=" + orderItems.size() +
                '}';
    }
}
