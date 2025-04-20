package DAO;

import DAO.MenuCache;
import DAO.OrderDAO;
import DAO.TableDAO;
import DAO.UserDAO;
import Model.MenuItem;
import Model.Order;
import Model.RestaurantTable;
import Model.User;
import DAO.TableStatusObserver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for restaurant operations
 * This provides a simple API for controllers to interact with data
 */
public class RestaurantService {
    private static final Logger logger = Logger.getLogger(RestaurantService.class.getName());
    private static RestaurantService instance;
    
    private final UserDAO userDAO;
    private final TableDAO tableDAO;
    private final OrderDAO orderDAO;
    
    private RestaurantService() {
        userDAO = UserDAO.getInstance();
        tableDAO = TableDAO.getInstance();
        orderDAO = OrderDAO.getInstance();
        
        // Initialize menu cache
        MenuCache.initialize();
    }
    
    public static synchronized RestaurantService getInstance() {
        if (instance == null) {
            instance = new RestaurantService();
        }
        return instance;
    }
    
    // User-related methods
    public boolean authenticateUser(String username, String password) {
        try {
            return userDAO.authenticateUser(username, password);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Authentication failed", e);
            return false;
        }
    }
    
    public User getUserByUsername(String username) {
        try {
            return userDAO.getByUsername(username);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by username", e);
            return null;
        }
    }
    
    public List<User> getAllUsers() {
        try {
            return userDAO.getAll();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all users", e);
            return new ArrayList<>(); // Return empty list instead of null
        }
    }
    
    public boolean clockInEmployee(String userId) {
        try {
            return userDAO.clockInEmployee(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clocking in employee", e);
            return false;
        }
    }
    
    public boolean clockOutEmployee(String userId) {
        try {
            return userDAO.clockOutEmployee(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clocking out employee", e);
            return false;
        }
    }
    
    // Table-related methods
    public List<RestaurantTable> getAllTables() {
        try {
            return tableDAO.getAll();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all tables", e);
            return new ArrayList<>();
        }
    }
    
    public List<RestaurantTable> getTablesByWaiter(String waiterId) {
        try {
            return tableDAO.getTablesByWaiter(waiterId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting tables by waiter", e);
            return new ArrayList<>();
        }
    }
    
    public List<RestaurantTable> getTablesByStatus(String status) {
        try {
            return tableDAO.getTablesByStatus(status);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting tables by status", e);
            return new ArrayList<>();
        }
    }
    
    public boolean updateTableStatus(String tableId, String status, String userId) {
        try {
            return tableDAO.updateTableStatus(tableId, status, userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating table status", e);
            return false;
        }
    }
    
    public void addTableStatusObserver(TableStatusObserver observer) {
        tableDAO.addObserver(observer);
    }
    
    public void removeTableStatusObserver(TableStatusObserver observer) {
        tableDAO.removeObserver(observer);
    }
    
    // Menu-related methods
    public List<MenuItem> getAllMenuItems() {
        return MenuCache.getMenuItems();
    }
    
    public MenuItem getMenuItemById(String itemId) {
        return MenuCache.getMenuItemById(itemId);
    }
    
    public List<MenuItem> getMenuItemsByCategory(int categoryId) {
        return MenuCache.getMenuItemsByCategory(categoryId);
    }
    
    public void refreshMenuCache() {
        MenuCache.refreshCache();
    }
    
    // Order-related methods
    public Order createOrder(String tableId, String waiterId) {
        try {
            String orderId = orderDAO.generateOrderId();
            Order order = new Order(orderId, tableId, waiterId);
            if (orderDAO.add(order)) {
                // Update table status to occupied
                tableDAO.updateTableStatus(tableId, "OCCUPIED", waiterId);
                return order;
            }
            return null;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating order", e);
            return null;
        }
    }
    
    public boolean processPayment(String orderId, String paymentMethod, double tipAmount) {
        try {
            return orderDAO.processPayment(orderId, paymentMethod, tipAmount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error processing payment", e);
            return false;
        }
    }
    
    public List<Order> getActiveOrders() {
        try {
            return orderDAO.getActiveOrders();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting active orders", e);
            return new ArrayList<>();
        }
    }
    
    public List<Order> getOrdersByWaiter(String waiterId) {
        try {
            return orderDAO.getOrdersByWaiter(waiterId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting orders by waiter", e);
            return new ArrayList<>();
        }
    }
    
    // Dashboard-related methods
    
    /**
     * Get daily revenue for a specific date
     * @param date The date to get revenue for
     * @return Total revenue for the date
     */
    public double getDailyRevenue(LocalDate date) {
        try {
            // This would normally query the database through OrderDAO
            // For now we'll generate realistic sample data
            // In a real implementation, replace this with actual query results
            
            Random random = new Random(date.toEpochDay()); // Use date as seed for consistent results
            double baseRevenue = 2000 + random.nextDouble() * 1000; // Base between $2000-$3000
            
            // Weekends have higher revenue
            if (date.getDayOfWeek().getValue() >= 5) { // Friday, Saturday, Sunday
                baseRevenue *= 1.25;
            }
            
            return Math.round(baseRevenue * 100) / 100.0; // Round to 2 decimal places
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting daily revenue", e);
            return 0.0;
        }
    }
    
    /**
     * Get the percentage change in revenue compared to previous day
     * @param date The current date
     * @return Percentage change in revenue
     */
    public double getRevenuePercentChange(LocalDate date) {
        try {
            double todayRevenue = getDailyRevenue(date);
            double yesterdayRevenue = getDailyRevenue(date.minusDays(1));
            
            if (yesterdayRevenue == 0) {
                return 0.0;
            }
            
            return ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating revenue change", e);
            return 0.0;
        }
    }
    
    /**
     * Get number of customers for a specific date
     * @param date The date to get customer count for
     * @return Total customers for the date
     */
    public int getDailyCustomerCount(LocalDate date) {
        try {
            // This would normally query the database through OrderDAO
            // For now we'll generate realistic sample data based on revenue
            // In a real implementation, replace this with actual query results
            
            double revenue = getDailyRevenue(date);
            // Assume average spend of $35 per customer with some randomness
            Random random = new Random(date.toEpochDay() + 1); // Different seed than revenue
            double avgSpend = 30 + random.nextDouble() * 10; // $30-$40 per customer
            
            return (int) Math.round(revenue / avgSpend);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting daily customer count", e);
            return 0;
        }
    }
    
    /**
     * Get the percentage change in customer count compared to previous day
     * @param date The current date
     * @return Percentage change in customer count
     */
    public double getCustomerCountPercentChange(LocalDate date) {
        try {
            int todayCount = getDailyCustomerCount(date);
            int yesterdayCount = getDailyCustomerCount(date.minusDays(1));
            
            if (yesterdayCount == 0) {
                return 0.0;
            }
            
            return ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating customer count change", e);
            return 0.0;
        }
    }
    
    /**
     * Get count of staff by role
     * @return Map with role as key and count as value
     */
    public Map<String, Integer> getStaffCountByRole() {
        try {
            List<User> allUsers = getAllUsers();
            Map<String, Integer> staffCounts = new HashMap<>();
            
            for (User user : allUsers) {
                String role = user.getRole();
                staffCounts.put(role, staffCounts.getOrDefault(role, 0) + 1);
            }
            
            // If no real data is available, provide sample data
            if (staffCounts.isEmpty()) {
                staffCounts.put("WAITER", 5);
                staffCounts.put("BUSBOY", 2);
                staffCounts.put("COOK", 5);
                staffCounts.put("MANAGER", 1);
            }
            
            return staffCounts;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting staff counts", e);
            
            // Return sample data on error
            Map<String, Integer> sampleData = new HashMap<>();
            sampleData.put("WAITER", 5);
            sampleData.put("BUSBOY", 2);
            sampleData.put("COOK", 5);
            sampleData.put("MANAGER", 1);
            return sampleData;
        }
    }
    
    /**
     * Get summary of table status counts
     * @return Map with status as key and count as value
     */
    public Map<String, Integer> getTableStatusSummary() {
        try {
            List<RestaurantTable> allTables = getAllTables();
            Map<String, Integer> statusCounts = new HashMap<>();
            
            for (RestaurantTable table : allTables) {
                String status = table.getStatus();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
            
            // If no real data is available, provide sample data
            if (statusCounts.isEmpty()) {
                statusCounts.put("AVAILABLE", 16);
                statusCounts.put("OCCUPIED", 9);
                statusCounts.put("DIRTY", 3);
            }
            
            return statusCounts;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting table status summary", e);
            
            // Return sample data on error
            Map<String, Integer> sampleData = new HashMap<>();
            sampleData.put("AVAILABLE", 16);
            sampleData.put("OCCUPIED", 9);
            sampleData.put("DIRTY", 3);
            return sampleData;
        }
    }
    
    /**
     * Get table occupancy percentage
     * @return Percentage of tables that are occupied
     */
    public double getTableOccupancyPercentage() {
        try {
            Map<String, Integer> statusCounts = getTableStatusSummary();
            int occupiedTables = statusCounts.getOrDefault("OCCUPIED", 0);
            
            int totalTables = 0;
            for (Integer count : statusCounts.values()) {
                totalTables += count;
            }
            
            if (totalTables == 0) {
                return 0.0;
            }
            
            return ((double) occupiedTables / totalTables) * 100;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating table occupancy percentage", e);
            return 0.0;
        }
    }
}