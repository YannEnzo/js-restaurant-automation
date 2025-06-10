package DAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * This class serves as an adapter to integrate the reporting extension with the main RestaurantService.
 * In a real implementation, these methods would be directly integrated into the RestaurantService class.
 */
public class RestaurantServiceReportAdapter {
    private final RestaurantService service;
    
    /**
     * Constructor
     * @param service The RestaurantService instance to extend
     */
    public RestaurantServiceReportAdapter(RestaurantService service) {
        this.service = service;
    }
    
    /**
     * Get daily revenue for a specific date
     * @param date The date to get revenue for
     * @return The total revenue for the date
     */
    public double getDailyRevenue(LocalDate date) {
        return RestaurantServiceExtension.getDailyRevenue(date);
    }
    
    /**
     * Get daily customer count for a specific date
     * @param date The date to get customer count for
     * @return The total number of customers for the date
     */
    public int getDailyCustomerCount(LocalDate date) {
        return RestaurantServiceExtension.getDailyCustomerCount(date);
    }
    
    /**
     * Get staff count by role
     * @return Map with role as key and count as value
     */
    public Map<String, Integer> getStaffCountByRole() {
        return RestaurantServiceExtension.getStaffCountByRole();
    }
    
    /**
     * Get table status summary
     * @return Map with status as key and count as value
     */
    public Map<String, Integer> getTableStatusSummary() {
        return RestaurantServiceExtension.getTableStatusSummary();
    }
    
    /**
     * Get sales data for a specific date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of [itemName, category, quantity] arrays
     */
    public List<Object[]> getSalesDataForDateRange(LocalDate startDate, LocalDate endDate) {
        return RestaurantServiceExtension.getSalesDataForDateRange(startDate, endDate);
    }
    
    /**
     * Get inventory status data
     * @return List of [itemName, category, currentStock, minRequired, status] arrays
     */
    public List<Object[]> getInventoryStatusData() {
        return RestaurantServiceExtension.getInventoryStatusData();
    }
    
    /**
     * Get kitchen performance data
     * @return List of [menuItemName, category, avgPrepTime, targetTime, status] arrays
     */
    public List<Object[]> getKitchenPerformanceData() {
        return RestaurantServiceExtension.getKitchenPerformanceData();
    }
    
    /**
     * Calculate date range from string description
     * @param dateRangeStr Date range string (e.g., "Last 7 Days")
     * @return Array with [startDate, endDate]
     */
    public LocalDate[] calculateDateRange(String dateRangeStr) {
        return RestaurantServiceExtension.calculateDateRange(dateRangeStr);
    }
    
    /**
     * Convert LocalDate to formatted string
     * @param date Date to format
     * @param pattern Format pattern
     * @return Formatted date string
     */
    public String formatDate(LocalDate date, String pattern) {
        return RestaurantServiceExtension.formatDate(date, pattern);
    }
}