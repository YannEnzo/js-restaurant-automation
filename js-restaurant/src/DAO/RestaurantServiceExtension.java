package DAO;

import Model.MenuItem;
import Model.Order;
import Model.RestaurantTable;
import Model.User;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class extends the RestaurantService with additional reporting methods.
 * In a real implementation, these would be integrated directly into the RestaurantService class.
 * For this demo, we're creating a separate class to avoid modifying the original file.
 */
public class RestaurantServiceExtension {
    private static final Logger logger = Logger.getLogger(RestaurantServiceExtension.class.getName());
    
    /**
     * Get daily revenue for a specific date
     * @param date The date to get revenue for
     * @return The total revenue for the date
     */
    public static double getDailyRevenue(LocalDate date) {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        
        // Monday: $2,510, Tuesday: $2,350, Wednesday: $2,800, Thursday: $3,100, Friday: $4,500
        // Saturday: $5,200, Sunday: $1,800
        
        switch (date.getDayOfWeek()) {
            case MONDAY: return 2510.0;
            case TUESDAY: return 2350.0;
            case WEDNESDAY: return 2800.0;
            case THURSDAY: return 3100.0;
            case FRIDAY: return 4500.0;
            case SATURDAY: return 5200.0;
            case SUNDAY: return 1800.0;
            default: return 2500.0;
        }
    }
    
    /**
     * Get daily customer count for a specific date
     * @param date The date to get customer count for
     * @return The total number of customers for the date
     */
    public static int getDailyCustomerCount(LocalDate date) {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        
        // Monday: 68, Tuesday: 62, Wednesday: 74, Thursday: 85, Friday: 120
        // Saturday: 140, Sunday: 55
        
        switch (date.getDayOfWeek()) {
            case MONDAY: return 68;
            case TUESDAY: return 62;
            case WEDNESDAY: return 74;
            case THURSDAY: return 85;
            case FRIDAY: return 120;
            case SATURDAY: return 140;
            case SUNDAY: return 55;
            default: return 70;
        }
    }
    
    /**
     * Get staff count by role
     * @return Map with role as key and count as value
     */
    public static Map<String, Integer> getStaffCountByRole() {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        Map<String, Integer> roleCount = new HashMap<>();
        roleCount.put("WAITER", 5);
        roleCount.put("COOK", 5);
        roleCount.put("BUSBOY", 2);
        roleCount.put("MANAGER", 1);
        
        return roleCount;
    }
    
    /**
     * Get table status summary
     * @return Map with status as key and count as value
     */
    public static Map<String, Integer> getTableStatusSummary() {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("AVAILABLE", 16);
        statusCount.put("OCCUPIED", 9);
        statusCount.put("DIRTY", 3);
        
        return statusCount;
    }
    
    /**
     * Get sales data for a specific date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of [itemName, category, quantity] arrays
     */
    public static List<Object[]> getSalesDataForDateRange(LocalDate startDate, LocalDate endDate) {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        List<Object[]> salesData = new ArrayList<>();
        
        // Sample data for a 30-day period
        salesData.add(new Object[]{"Grilled Salmon", "Entrees", 152});
        salesData.add(new Object[]{"Chicken Parmesan", "Entrees", 132});
        salesData.add(new Object[]{"Caesar Salad", "Salads", 121});
        salesData.add(new Object[]{"Filet Mignon", "Entrees", 112});
        salesData.add(new Object[]{"Spinach Art Dip", "Appetizers", 98});
        salesData.add(new Object[]{"Chocolate Cake", "Desserts", 85});
        
        // If the date range is 7 days or less, reduce quantities
        if (java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) <= 7) {
            salesData.clear();
            salesData.add(new Object[]{"Grilled Salmon", "Entrees", 20});
            salesData.add(new Object[]{"Chicken Parmesan", "Entrees", 17});
            salesData.add(new Object[]{"Caesar Salad", "Salads", 15});
            salesData.add(new Object[]{"Filet Mignon", "Entrees", 13});
            salesData.add(new Object[]{"Spinach Art Dip", "Appetizers", 9});
            salesData.add(new Object[]{"Chocolate Cake", "Desserts", 6});
        }
        
        return salesData;
    }
    
    /**
     * Get inventory status data
     * @return List of [itemName, category, currentStock, minRequired, status] arrays
     */
    public static List<Object[]> getInventoryStatusData() {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        List<Object[]> inventoryData = new ArrayList<>();
        
        inventoryData.add(new Object[]{"Chicken Breast", "Poultry", "8 lbs", "20 lbs", "Low"});
        inventoryData.add(new Object[]{"Filet Mignon", "Beef", "12 lbs", "15 lbs", "Low"});
        inventoryData.add(new Object[]{"Salmon Filet", "Seafood", "15 lbs", "10 lbs", "OK"});
        inventoryData.add(new Object[]{"Romaine Lettuce", "Produce", "25 heads", "15 heads", "OK"});
        inventoryData.add(new Object[]{"Red Wine", "Beverages", "8 bottles", "12 Bottles", "Low"});
        inventoryData.add(new Object[]{"White Wine", "Beverages", "14 bottles", "12 Bottles", "OK"});
        
        return inventoryData;
    }
    
    /**
     * Get kitchen performance data
     * @return List of [menuItemName, category, avgPrepTime, targetTime, status] arrays
     */
    public static List<Object[]> getKitchenPerformanceData() {
        // In a real implementation, this would query the database
        // For demo purposes, we'll return sample data
        List<Object[]> kitchenData = new ArrayList<>();
        
        kitchenData.add(new Object[]{"Grilled Salmon", "Entrees", "14:35", "15:00", "Good"});
        kitchenData.add(new Object[]{"NY Strip Steak", "Entrees", "17:45", "16:00", "Warning"});
        kitchenData.add(new Object[]{"Bacon Cheeseburger", "Entrees", "11:20", "12:00", "Good"});
        kitchenData.add(new Object[]{"Chicken Nachos", "Appetizers", "8:45", "8:00", "Warning"});
        kitchenData.add(new Object[]{"Sweet Tea Fried Chicken", "Entrees", "18:30", "15:00", "Critical"});
        
        return kitchenData;
    }
    
    /**
     * Calculate date range from string description
     * @param dateRangeStr Date range string (e.g., "Last 7 Days")
     * @return Array with [startDate, endDate]
     */
    public static LocalDate[] calculateDateRange(String dateRangeStr) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;
        
        switch (dateRangeStr) {
            case "Today":
                startDate = today;
                break;
            case "Yesterday":
                startDate = today.minusDays(1);
                endDate = startDate;
                break;
            case "Last 7 Days":
                startDate = today.minusDays(6); // 6 days + today = 7 days
                break;
            case "Last 30 Days":
                startDate = today.minusDays(29); // 29 days + today = 30 days
                break;
            case "This Month":
                startDate = today.withDayOfMonth(1);
                break;
            case "Last Month":
                // First day of previous month
                startDate = today.minusMonths(1).withDayOfMonth(1);
                // Last day of previous month
                endDate = today.withDayOfMonth(1).minusDays(1);
                break;
            case "This Quarter":
                // First day of current quarter
                int currentMonth = today.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1; // 1, 4, 7, or 10
                startDate = today.withMonth(quarterStartMonth).withDayOfMonth(1);
                break;
            default:
                // Default to last 7 days
                startDate = today.minusDays(6);
                break;
        }
        
        return new LocalDate[]{startDate, endDate};
    }
    
    /**
     * Convert LocalDate to formatted string
     * @param date Date to format
     * @param pattern Format pattern
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }
}