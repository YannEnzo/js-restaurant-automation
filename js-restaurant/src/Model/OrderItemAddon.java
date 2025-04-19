/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

 
/**
 * Represents an add-on selected for a specific order item
 */
public class OrderItemAddon {
    private int orderItemAddonId;
    private int orderItemId;
    private int addonId;
    private String name; // Not stored in DB, for display only
    private double price;
    
    // Constructors
    public OrderItemAddon() {
    }
    
    public OrderItemAddon(int orderItemId, int addonId, double price) {
        this.orderItemId = orderItemId;
        this.addonId = addonId;
        this.price = price;
    }
    
    // Getters and Setters
    public int getOrderItemAddonId() {
        return orderItemAddonId;
    }
    
    public void setOrderItemAddonId(int orderItemAddonId) {
        this.orderItemAddonId = orderItemAddonId;
    }
    
    public int getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public int getAddonId() {
        return addonId;
    }
    
    public void setAddonId(int addonId) {
        this.addonId = addonId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return "OrderItemAddon{" +
                "addonId=" + addonId +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}