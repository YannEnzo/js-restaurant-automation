package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an inventory item in the restaurant
 */
public class InventoryItem {
    private int inventoryId;
    private String itemName;
    private double quantity;
    private String unit;
    private LocalDateTime lastUpdated;
    private double reorderLevel;
    private boolean isLow;
    private String category; // Optional category for grouping
    
    /**
     * Default constructor
     */
    public InventoryItem() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential fields
     * @param inventoryId Inventory ID
     * @param itemName Item name
     * @param quantity Quantity
     * @param unit Unit of measurement
     * @param reorderLevel Reorder level
     */
    public InventoryItem(int inventoryId, String itemName, double quantity, 
                       String unit, double reorderLevel) {
        this.inventoryId = inventoryId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.lastUpdated = LocalDateTime.now();
        this.isLow = quantity < reorderLevel;
    }
    
    /**
     * Constructor with all fields
     * @param inventoryId Inventory ID
     * @param itemName Item name
     * @param quantity Quantity
     * @param unit Unit of measurement
     * @param lastUpdated Last updated datetime
     * @param reorderLevel Reorder level
     * @param isLow Is low flag
     * @param category Category
     */
    public InventoryItem(int inventoryId, String itemName, double quantity, 
                       String unit, LocalDateTime lastUpdated, double reorderLevel, 
                       boolean isLow, String category) {
        this.inventoryId = inventoryId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.lastUpdated = lastUpdated;
        this.reorderLevel = reorderLevel;
        this.isLow = isLow;
        this.category = category;
    }
    public InventoryItem(int inventoryId, String itemName, double quantity, 
                         String unit, double reorderLevel, boolean isLow, 
                         String category) {
        this.inventoryId = inventoryId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.isLow = isLow;
        this.category = category;
    }
    /**
     * Get the inventory ID
     * @return the inventoryId
     */
    public int getInventoryId() {
        return inventoryId;
    }

    /**
     * Set the inventory ID
     * @param inventoryId the inventoryId to set
     */
    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    /**
     * Get the item name
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Set the item name
     * @param itemName the itemName to set
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Get the quantity
     * @return the quantity
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity
     * @param quantity the quantity to set
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.isLow = quantity < reorderLevel;
    }

    /**
     * Get the unit
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Set the unit
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get the last updated datetime
     * @return the lastUpdated
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set the last updated datetime
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Get the reorder level
     * @return the reorderLevel
     */
    public double getReorderLevel() {
        return reorderLevel;
    }

    /**
     * Set the reorder level
     * @param reorderLevel the reorderLevel to set
     */
    public void setReorderLevel(double reorderLevel) {
        this.reorderLevel = reorderLevel;
        this.isLow = quantity < reorderLevel;
    }

    /**
     * Check if item is low on stock
     * @return true if low, false otherwise
     */
    public boolean isLow() {
        return isLow;
    }

    /**
     * Set the low flag
     * @param isLow the isLow to set
     */
    public void setLow(boolean isLow) {
        this.isLow = isLow;
    }

    /**
     * Get the category
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the category
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get formatted quantity string (with unit)
     * @return Formatted quantity string
     */
    public String getFormattedQuantity() {
        if (quantity == (int) quantity) {
            return (int) quantity + " " + unit;
        } else {
            return String.format("%.1f", quantity) + " " + unit;
        }
    }
    
    /**
     * Get formatted last updated datetime
     * @return Formatted last updated datetime
     */
    public String getFormattedLastUpdated() {
        if (lastUpdated == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        return lastUpdated.format(formatter);
    }
    
    /**
     * Get status text
     * @return Status text
     */
    public String getStatusText() {
        return isLow ? "Low" : "OK";
    }
    
    /**
     * Check if item needs restocking
     * @return true if needs restocking, false otherwise
     */
    public boolean needsRestocking() {
        return isLow;
    }
    
    @Override
    public String toString() {
        return "InventoryItem{" +
                "inventoryId=" + inventoryId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", reorderLevel=" + reorderLevel +
                ", isLow=" + isLow +
                '}';
    }
}