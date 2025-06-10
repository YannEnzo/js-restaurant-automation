package Model;

/**
 * Represents kitchen performance metrics for a menu item
 */
public class KitchenPerformanceItem {
    private String itemId;
    private String itemName;
    private String categoryName;
    private double avgPrepTimeMinutes;
    private int targetTimeMinutes;
    private int orderCount;
    private String status; // "Good", "Warning", or "Critical"
    
    public KitchenPerformanceItem(String itemId, String itemName, String categoryName,
                                 double avgPrepTimeMinutes, int targetTimeMinutes, 
                                 int orderCount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.categoryName = categoryName;
        this.avgPrepTimeMinutes = avgPrepTimeMinutes;
        this.targetTimeMinutes = targetTimeMinutes;
        this.orderCount = orderCount;
        
        // Calculate status based on performance
        if (avgPrepTimeMinutes <= targetTimeMinutes) {
            this.status = "Good";
        } else if (avgPrepTimeMinutes <= targetTimeMinutes * 1.2) {
            this.status = "Warning"; // Up to 20% over target
        } else {
            this.status = "Critical"; // More than 20% over target
        }
    }
    
    // Getters and setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public double getAvgPrepTimeMinutes() { return avgPrepTimeMinutes; }
    public void setAvgPrepTimeMinutes(double avgPrepTimeMinutes) { this.avgPrepTimeMinutes = avgPrepTimeMinutes; }
    
    public int getTargetTimeMinutes() { return targetTimeMinutes; }
    public void setTargetTimeMinutes(int targetTimeMinutes) { this.targetTimeMinutes = targetTimeMinutes; }
    
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Format the prep time for display (mm:ss)
    public String getFormattedAvgPrepTime() {
        int minutes = (int) avgPrepTimeMinutes;
        int seconds = (int) ((avgPrepTimeMinutes - minutes) * 60);
        return String.format("%d:%02d", minutes, seconds);
    }
    
    // Format the target time for display (mm:ss)
    public String getFormattedTargetTime() {
        return String.format("%d:00", targetTimeMinutes);
    }
}