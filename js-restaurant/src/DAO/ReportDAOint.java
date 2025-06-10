package DAO;

import Model.KitchenPerformanceItem;
import Model.TopSellerItem;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for report-related data access operations
 */
public interface ReportDAOint {
    
    /**
     * Get top selling menu items for a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param limit Maximum number of items to return
     * @return List of top selling items with quantities
     */
    List<TopSellerItem> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) throws SQLException;
    
    /**
     * Get kitchen performance metrics for a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param categoryFilter Optional category filter (null for all)
     * @param staffFilter Optional staff filter (null for all)
     * @return List of kitchen performance metrics by menu item
     */
    List<KitchenPerformanceItem> getKitchenPerformance(
            LocalDate startDate, LocalDate endDate, 
            String categoryFilter, String staffFilter) throws SQLException;
    
    /**
     * Get daily revenue data for a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Map of dates to revenue amounts
     */
    java.util.Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) throws SQLException;
    
    /**
     * Get daily customer count for a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Map of dates to customer counts
     */
    java.util.Map<LocalDate, Integer> getDailyCustomerCount(LocalDate startDate, LocalDate endDate) throws SQLException;
}