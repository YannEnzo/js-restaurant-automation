package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a menu item in the restaurant
 */
public class MenuItem {
    private String itemId;
    private String name;
    private String description;
    private int categoryId;
    private String categoryName; // Not stored in DB, for display only
    private double price;
    private boolean isAvailable;
    private List<MenuItemAddon> addons;
    
    // Constructors
    public MenuItem() {
        this.addons = new ArrayList<>();
    }
    
    public MenuItem(String itemId, String name, String description, 
                  int categoryId, double price, boolean isAvailable) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.price = price;
        this.isAvailable = isAvailable;
        this.addons = new ArrayList<>();
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    public List<MenuItemAddon> getAddons() {
        return addons;
    }
    
    public void setAddons(List<MenuItemAddon> addons) {
        this.addons = addons;
    }
    
    public void addAddon(MenuItemAddon addon) {
        this.addons.add(addon);
    }
    
    @Override
    public String toString() {
        return "MenuItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                ", isAvailable=" + isAvailable +
                '}';
    }
private int preparationTime; 
    public int getPreparationTime() {
        return preparationTime; //To change body of generated methods, choose Tools | Templates.
    }
}