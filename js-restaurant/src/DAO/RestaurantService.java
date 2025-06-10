package DAO;

import Controllers.Manager.ReportController;
import Model.MenuItem;
import Model.Order;
import Model.OrderItem;
import Model.RestaurantTable;
import Model.TimeRecord;
import Model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * Consolidated service layer for restaurant operations
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
    
    public User getUserById(String userId) {
        try {
            return userDAO.getById(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by ID", e);
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
    
    public RestaurantTable getTableById(String tableId) {
        try {
            return tableDAO.getById(tableId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting table by ID: " + tableId, e);
            return null;
        }
    }
    
    public RestaurantTable getTableByNumber(String tableNumber) {
        try {
            return tableDAO.getByTableNumber(tableNumber);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting table by number: " + tableNumber, e);
            return null;
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
            // Ensure the status is not null
            if (status == null || status.isEmpty()) {
                logger.warning("Invalid status provided for table: " + tableId);
                return false;
            }
            
            // Update table status
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
    
    public Order getOrderById(String orderId) {
        try {
            return orderDAO.getById(orderId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting order by ID: " + orderId, e);
            return null;
        }
    }
    
    public List<Order> getActiveOrdersByTable(String tableId) {
        try {
            List<Order> allActiveOrders = orderDAO.getActiveOrders();
            List<Order> tableOrders = new ArrayList<>();
            
            for (Order order : allActiveOrders) {
                if (order.getTableId().equals(tableId)) {
                    tableOrders.add(order);
                }
            }
            
            return tableOrders;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting active orders for table: " + tableId, e);
            return new ArrayList<>(); // Return empty list on error
        }
    }
    
    public boolean addItemToOrder(OrderItem newItem, String orderId) {
        try {
            // Ensure the item has the order ID set
            newItem.setOrderId(orderId);
            
            // If the item doesn't have a price set, get it from the menu item
            if (newItem.getPrice() <= 0) {
                MenuItem menuItem = MenuCache.getMenuItemById(newItem.getMenuItemId());
                if (menuItem != null) {
                    newItem.setPrice(menuItem.getPrice());
                    
                    // Set the menu item name for easier display in the kitchen
                    if (newItem.getMenuItemName() == null || newItem.getMenuItemName().isEmpty()) {
                        newItem.setMenuItemName(menuItem.getName());
                    }
                } else {
                    logger.log(Level.WARNING, "Could not find menu item: " + newItem.getMenuItemId());
                    return false;
                }
            }
            
            // Add the item to the order
            boolean success = orderDAO.addItemToOrder(newItem, orderId);
            
            if (success) {
                // After adding the item, update the order total
                Order order = orderDAO.getById(orderId);
                if (order != null) {
                    // Recalculate totals
                    double subtotal = order.calculateSubtotal();
                    double taxAmount = order.calculateTax();
                    
                    // Update the order
                    order.setTotalAmount(subtotal);
                    order.setTaxAmount(taxAmount);
                    orderDAO.update(order);
                }
            }
            
            return success;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding item to order", e);
            return false;
        }
    }
    
    public boolean removeItemFromOrder(OrderItem itemToRemove) {
        try {
            // Ensure the item exists in the current order
            if (itemToRemove == null || itemToRemove.getOrderId() == null || itemToRemove.getMenuItemId() == null) {
                logger.warning("Item to remove is invalid.");
                return false;
            }
            
            // Call the DAO to remove the item from the database
            boolean success = orderDAO.removeItemFromOrder(itemToRemove);
            if (!success) {
                logger.warning("Failed to remove item from the order.");
                return false;
            }
            
            // Recalculate the order totals after removal
            Order order = orderDAO.getById(itemToRemove.getOrderId());
            if (order != null) {
                double subtotal = order.calculateSubtotal();
                double taxAmount = order.calculateTax();
                
                // Update the order with new totals
                order.setTotalAmount(subtotal);
                order.setTaxAmount(taxAmount);
                orderDAO.update(order);
            }
            
            logger.info("Item removed successfully from the order.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error removing item from order", e);
            return false;
        }
    }
    
    public boolean updateOrder(Order order) {
        try {
            // Ensure the order is valid and has an order ID
            if (order == null || order.getOrderId() == null || order.getOrderId().isEmpty()) {
                logger.warning("Cannot update order with invalid order ID");
                return false;
            }
            
            // Update the order in the OrderDAO
            boolean success = orderDAO.update(order);
            
            if (success) {
                // If the order status changed to PAID, update the table status
                if ("PAID".equals(order.getStatus())) {
                    tableDAO.updateTableStatus(order.getTableId(), "DIRTY", order.getWaiterId());
                }
                
                // If the table is now empty (no active orders), it could be marked as available
                checkTableStatus(order.getTableId());
            }
            
            return success;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating order", e);
            return false;
        }
    }
    
    public boolean updateOrderItem(OrderItem item) {
        try {
            // Ensure the item has the order item ID set
            if (item.getOrderItemId() <= 0) {
                logger.warning("Invalid order item ID");
                return false;
            }
            
            // Update the order item in the OrderDAO
            return orderDAO.updateOrderItem(item);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating order item", e);
            return false;
        }
    }
    
    /**
     * Optional helper method to check if a table has no active orders and can be marked available
     * @param tableId Table ID to check
     */
    private void checkTableStatus(String tableId) {
        try {
            // Get all active orders for this table
            List<Order> activeOrders = new ArrayList<>();
            for (Order order : orderDAO.getActiveOrders()) {
                if (order.getTableId().equals(tableId)) {
                    activeOrders.add(order);
                }
            }
            
            // If no active orders, the table can be marked as dirty
            if (activeOrders.isEmpty()) {
                // Find who last updated this table
                RestaurantTable table = tableDAO.getById(tableId);
                if (table != null && table.getAssignedWaiterId() != null) {
                    tableDAO.updateTableStatus(tableId, "DIRTY", table.getAssignedWaiterId());
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking table status", e);
        }
    }
    
    // Time record-related methods
    public List<TimeRecord> getTimeRecordsForEmployee(String userId) {
        try {
            return TimeRecordDAO.getInstance().getRecordsForEmployee(userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting time records for employee: " + userId, e);
            return new ArrayList<>(); // Return empty list on error
        }
    }
    
    // Dashboard-related methods
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
    
    public Map<String, Integer> getStaffCountByRole() {
        try {
            return userDAO.getStaffCountByRole();
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
    
    public Map<String, Integer> getTableStatusSummary() {
        try {
            return tableDAO.getTableStatusSummary();
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
    
    // Report-related methods
    public List<ReportController.TopSellerData> getTopSellers(LocalDate startDate, LocalDate endDate, int limit) {
        List<ReportController.TopSellerData> topSellers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getInstance().getConnection();
            
            // Query to get top sellers for the date range
            String sql = 
                "SELECT mi.item_id, mi.name, mc.name as category_name, " +
                "COUNT(oi.order_item_id) as order_count, " +
                "SUM(oi.price * oi.quantity) as total_revenue " +
                "FROM order_item oi " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.item_id " +
                "JOIN menu_category mc ON mi.category_id = mc.category_id " +
                "JOIN `order` o ON oi.order_id = o.order_id " +
                "WHERE o.order_datetime BETWEEN ? AND ? " +
                "GROUP BY mi.item_id " +
                "ORDER BY order_count DESC " +
                "LIMIT ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            stmt.setInt(3, limit);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                String name = rs.getString("name");
                String category = rs.getString("category_name");
                int orderCount = rs.getInt("order_count");
                double revenue = rs.getDouble("total_revenue");
                
                topSellers.add(new ReportController.TopSellerData(itemId, name, category, orderCount, revenue));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting top sellers", e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return topSellers;
    }
    
    public List<ReportController.InventoryItem> getInventoryItems(String category, String status) {
        List<ReportController.InventoryItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getInstance().getConnection();
            
            StringBuilder sql = new StringBuilder(
                "SELECT inventory_id, item_name, category, quantity, unit, " +
                "reorder_level, is_low " +
                "FROM inventory WHERE 1=1");
            
            if (category != null) {
                sql.append(" AND category = ?");
            }
            
            if (status != null) {
                if (status.equals("Low")) {
                    sql.append(" AND is_low = 1");
                } else if (status.equals("OK")) {
                    sql.append(" AND is_low = 0");
                }
            }
            
            stmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (category != null) {
                stmt.setString(paramIndex++, category);
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String itemId = rs.getString("inventory_id");
                String name = rs.getString("item_name");
                String cat = rs.getString("category");
                double quantity = rs.getDouble("quantity");
                double reorderLevel = rs.getDouble("reorder_level");
                boolean isLow = rs.getBoolean("is_low");
                
                items.add(new ReportController.InventoryItem(
                    itemId, name, cat, quantity, reorderLevel, isLow));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting inventory items", e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return items;
    }
    
    public List<ReportController.KitchenPerformanceItem> getKitchenPerformanceData(String category, String staff) {
        List<ReportController.KitchenPerformanceItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getInstance().getConnection();
            
            StringBuilder sql = new StringBuilder(
                "SELECT mi.item_id, mi.name, mc.name as category_name, " +
                "mi.preparation_time as target_time, " +
                "AVG(TIMESTAMPDIFF(MINUTE, oi.preparation_start_time, oi.completion_time)) as avg_prep_time " +
                "FROM menu_item mi " +
                "JOIN menu_category mc ON mi.category_id = mc.category_id " +
                "LEFT JOIN order_item oi ON mi.item_id = oi.menu_item_id " +
                "AND oi.preparation_start_time IS NOT NULL " +
                "AND oi.completion_time IS NOT NULL ");
            
            if (category != null) {
                sql.append(" WHERE mc.name = ?");
            }
            
            sql.append(" GROUP BY mi.item_id");
            
            stmt = conn.prepareStatement(sql.toString());
            
            if (category != null) {
                stmt.setString(1, category);
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                String name = rs.getString("name");
                String cat = rs.getString("category_name");
                int targetTime = rs.getInt("target_time");
                Double avgPrepTime = rs.getObject("avg_prep_time") != null ? 
                                    rs.getDouble("avg_prep_time") : null;
                
                String avgPrepTimeStr = avgPrepTime != null ? 
                                        String.format("%.1f mins", avgPrepTime) : "N/A";
                String targetTimeStr = targetTime + " mins";
                
                String status = "N/A";
                if (avgPrepTime != null) {
                    if (avgPrepTime <= targetTime * 0.8) {
                        status = "Good";
                    } else if (avgPrepTime <= targetTime) {
                        status = "OK";
                    } else {
                        status = "Slow";
                    }
                }
                
                items.add(new ReportController.KitchenPerformanceItem(
                    itemId, name, cat, avgPrepTimeStr, targetTimeStr, status));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting kitchen performance data", e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return items;
    }
}