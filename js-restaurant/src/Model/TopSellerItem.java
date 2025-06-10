package Model;

/**
 * Represents a top seller item in reports
 */
public class TopSellerItem {
    private String itemId;
    private String itemName;
    private String categoryName;
    private int orderCount;
    private int quantity;
    private double revenue;
    
    public TopSellerItem(String itemId, String itemName, String categoryName, 
                        int orderCount, int quantity, double revenue) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.categoryName = categoryName;
        this.orderCount = orderCount;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    // Getters and setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
}