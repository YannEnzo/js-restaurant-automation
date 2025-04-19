/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

 
public class MenuCategory {
    private int categoryId;
    private String name;
    private String description;
    private int displayOrder;
    
    // Constructors
    public MenuCategory() {
    }
    
    public MenuCategory(int categoryId, String name, String description, int displayOrder) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }
    
    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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
    
    public int getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    @Override
    public String toString() {
        return "MenuCategory{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}