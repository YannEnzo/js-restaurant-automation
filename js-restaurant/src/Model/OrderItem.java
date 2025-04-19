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
 * Represents an item within an order
 */
public class OrderItem {
    private int orderItemId;
    private String orderId;
    private String menuItemId;
    private String menuItemName; // Not stored in DB, for display only
    private int quantity;
    private int seatNumber;
    private double price;
    private String specialInstructions;
    private String status; // ORDERED, IN_PREPARATION, READY, DELIVERED, CANCELLED
    private LocalDateTime preparationStartTime;
    private LocalDateTime completionTime;
    private List<OrderItemAddon> addons;
    
    // Constructors
    public OrderItem() {
        this.addons = new ArrayList<>();
    }
    
    public OrderItem(String orderId, String menuItemId, int quantity, 
                    int seatNumber, double price) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = "ORDERED";
        this.addons = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getMenuItemId() {
        return menuItemId;
    }
    
    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }
    
    public String getMenuItemName() {
        return menuItemName;
    }
    
    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public int getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getPreparationStartTime() {
        return preparationStartTime;
    }
    
    public void setPreparationStartTime(LocalDateTime preparationStartTime) {
        this.preparationStartTime = preparationStartTime;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }
    
    public List<OrderItemAddon> getAddons() {
        return addons;
    }
    
    public void setAddons(List<OrderItemAddon> addons) {
        this.addons = addons;
    }
    
    public void addAddon(OrderItemAddon addon) {
        this.addons.add(addon);
    }
    
    // Helper methods
    public double calculateItemTotal() {
        double addonTotal = 0;
        for (OrderItemAddon addon : addons) {
            addonTotal += addon.getPrice();
        }
        return (price + addonTotal) * quantity;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "menuItemId='" + menuItemId + '\'' +
                ", quantity=" + quantity +
                ", seat=" + seatNumber +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}