/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.ArrayList;
import java.util.List;
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
}