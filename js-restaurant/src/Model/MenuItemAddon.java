/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * Represents an add-on option for a menu item
 */
public class MenuItemAddon {
    private int addonId;
    private String itemId;
    private String name;
    private double price;
    
    // Constructors
    public MenuItemAddon() {
    }
    
    public MenuItemAddon(int addonId, String itemId, String name, double price) {
        this.addonId = addonId;
        this.itemId = itemId;
        this.name = name;
        this.price = price;
    }
    
    // Getters and Setters
    public int getAddonId() {
        return addonId;
    }
    
    public void setAddonId(int addonId) {
        this.addonId = addonId;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
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
        return "MenuItemAddon{" +
                "addonId=" + addonId +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}