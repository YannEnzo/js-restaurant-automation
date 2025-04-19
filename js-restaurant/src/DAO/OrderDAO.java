/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;
 
import Model.Order;
import Model.OrderItem;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Order entities
 */
public class OrderDAO {
    private static final Logger logger = Logger.getLogger(OrderDAO.class.getName());
    private static OrderDAO instance;
    private final DatabaseManager dbConnection;
    
    // Private constructor for singleton pattern
    private OrderDAO() {
        this.dbConnection = DatabaseManager.getInstance();
    }
    
    // Get singleton instance
    public static synchronized OrderDAO getInstance() {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }
    
    /**
     * Get all orders from the database
     * @return List of orders
     */
    public List<Order> getAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM `order`";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                // Load order items
                order.setOrderItems(getOrderItemsByOrderId(order.getOrderId(), conn));
                orders.add(order);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving all orders", ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return orders;
    }
    
    /**
     * Get an order by ID
     * @param id Order ID
     * @return Order object or null if not found
     */
    public Order getById(String id) throws SQLException {
        Order order = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM `order` WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                order = extractOrderFromResultSet(rs);
                // Load order items
                order.setOrderItems(getOrderItemsByOrderId(order.getOrderId(), conn));
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving order by ID: " + id, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return order;
    }
    
    /**
     * Get orders for a specific waiter
     * @param waiterId Waiter's user ID
     * @return List of orders assigned to the waiter
     */
    public List<Order> getOrdersByWaiter(String waiterId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM `order` WHERE waiter_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, waiterId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                // Load order items
                order.setOrderItems(getOrderItemsByOrderId(order.getOrderId(), conn));
                orders.add(order);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving orders by waiter: " + waiterId, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return orders;
    }
    
    /**
     * Get active orders (not paid or cancelled)
     * @return List of active orders
     */
    public List<Order> getActiveOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM `order` WHERE status NOT IN ('PAID', 'CANCELLED')";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                // Load order items
                order.setOrderItems(getOrderItemsByOrderId(order.getOrderId(), conn));
                orders.add(order);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving active orders", ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return orders;
    }
    
    /**
     * Add a new order to the database
     * @param order Order to add
     * @return true if successful, false otherwise
     */
    public boolean add(Order order) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Insert order
            String sql = "INSERT INTO `order` (order_id, table_id, waiter_id, order_datetime, status) " +
                         "VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, order.getOrderId());
            stmt.setString(2, order.getTableId());
            stmt.setString(3, order.getWaiterId());
            stmt.setTimestamp(4, Timestamp.valueOf(order.getOrderDateTime()));
            stmt.setString(5, order.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0 && !order.getOrderItems().isEmpty()) {
                // Add order items
                for (OrderItem item : order.getOrderItems()) {
                    addOrderItem(item, conn);
                }
            }
            
            // Commit transaction
            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", e);
                }
            }
            logger.log(Level.SEVERE, "Error adding order: " + order.getOrderId(), ex);
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Update an existing order
     * @param order Order to update
     * @return true if successful, false otherwise
     */
    public boolean update(Order order) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Update order
            String sql = "UPDATE `order` SET table_id = ?, waiter_id = ?, status = ?, " +
                         "total_amount = ?, tax_amount = ? WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, order.getTableId());
            stmt.setString(2, order.getWaiterId());
            stmt.setString(3, order.getStatus());
            stmt.setDouble(4, order.getTotalAmount());
            stmt.setDouble(5, order.getTaxAmount());
            stmt.setString(6, order.getOrderId());
            
            int rowsAffected = stmt.executeUpdate();
            
            // Commit transaction
            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", e);
                }
            }
            logger.log(Level.SEVERE, "Error updating order: " + order.getOrderId(), ex);
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error resetting auto-commit", e);
                }
            }
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Delete an order from the database
     * @param id Order ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Delete order (CASCADE will delete order items)
            String sql = "DELETE FROM `order` WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting order: " + id, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Process payment for an order
     * @param orderId Order ID
     * @param paymentMethod Payment method (CASH, CREDIT_CARD)
     * @param tipAmount Tip amount
     * @return true if successful, false otherwise
     */
    public boolean processPayment(String orderId, String paymentMethod, double tipAmount) throws SQLException {
        Connection conn = null;
        CallableStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "{CALL process_payment(?, ?, ?)}";
            stmt = conn.prepareCall(sql);
            stmt.setString(1, orderId);
            stmt.setString(2, paymentMethod);
            stmt.setDouble(3, tipAmount);
            
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error processing payment for order: " + orderId, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Generate a unique order ID
     * @return Unique order ID
     */
    public String generateOrderId() {
        // Format: ORD + 7 random digits
        Random random = new Random();
        int randomNum = random.nextInt(10000000);
        return "ORD" + String.format("%07d", randomNum);
    }
    
    /**
     * Add an item to an order
     * @param orderItem Order item to add
     * @param orderId Order ID
     * @return true if successful, false otherwise
     */
    public boolean addItemToOrder(OrderItem orderItem, String orderId) throws SQLException {
        Connection conn = null;
        CallableStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "{CALL add_order_item(?, ?, ?, ?, ?)}";
            stmt = conn.prepareCall(sql);
            stmt.setString(1, orderId);
            stmt.setString(2, orderItem.getMenuItemId());
            stmt.setInt(3, orderItem.getQuantity());
            stmt.setInt(4, orderItem.getSeatNumber());
            stmt.setString(5, orderItem.getSpecialInstructions());
            
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error adding item to order: " + orderId, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Update order status
     * @param orderId Order ID
     * @param status New status
     * @return true if successful, false otherwise
     */
    public boolean updateOrderStatus(String orderId, String status) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "UPDATE `order` SET status = ? WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating order status: " + orderId, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Get order items for an order
     * @param orderId Order ID
     * @return List of order items
     */
    private List<OrderItem> getOrderItemsByOrderId(String orderId, Connection existingConn) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean closeConnection = false;
        
        try {
            // Use existing connection if provided, otherwise create a new one
            if (existingConn != null) {
                conn = existingConn;
            } else {
                conn = dbConnection.getConnection();
                closeConnection = true;
            }
            
            String sql = "SELECT oi.*, mi.name as menu_item_name " +
                         "FROM order_item oi " +
                         "JOIN menu_item mi ON oi.menu_item_id = mi.item_id " +
                         "WHERE oi.order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, orderId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                OrderItem item = extractOrderItemFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving order items for order: " + orderId, ex);
            throw ex;
        } finally {
            // Only close connection if we created a new one
            if (closeConnection) {
                closeResources(conn, stmt, rs);
            } else {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
        }
        
        return items;
    }
    
    /**
     * Add an order item to the database
     * @param item OrderItem to add
     * @param conn Existing database connection
     * @return true if successful, false otherwise
     */
    private boolean addOrderItem(OrderItem item, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        
        try {
            String sql = "INSERT INTO order_item (order_id, menu_item_id, quantity, " +
                         "seat_number, price, special_instructions, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getOrderId());
            stmt.setString(2, item.getMenuItemId());
            stmt.setInt(3, item.getQuantity());
            stmt.setInt(4, item.getSeatNumber());
            stmt.setDouble(5, item.getPrice());
            stmt.setString(6, item.getSpecialInstructions());
            stmt.setString(7, item.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error adding order item", ex);
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * Extract an Order object from a ResultSet
     * @param rs ResultSet containing order data
     * @return Order object
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        
        order.setOrderId(rs.getString("order_id"));
        order.setTableId(rs.getString("table_id"));
        order.setWaiterId(rs.getString("waiter_id"));
        
        Timestamp orderDatetime = rs.getTimestamp("order_datetime");
        if (orderDatetime != null) {
            order.setOrderDateTime(orderDatetime.toLocalDateTime());
        } else {
            order.setOrderDateTime(LocalDateTime.now());
        }
        
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setTaxAmount(rs.getDouble("tax_amount"));
        order.setTipAmount(rs.getDouble("tip_amount"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        
        Timestamp paymentDatetime = rs.getTimestamp("payment_datetime");
        if (paymentDatetime != null) {
            order.setPaymentDateTime(paymentDatetime.toLocalDateTime());
        }
        
        return order;
    }
    
    /**
     * Extract an OrderItem object from a ResultSet
     * @param rs ResultSet containing order item data
     * @return OrderItem object
     */
    private OrderItem extractOrderItemFromResultSet(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getString("order_id"));
        item.setMenuItemId(rs.getString("menu_item_id"));
        item.setMenuItemName(rs.getString("menu_item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setSeatNumber(rs.getInt("seat_number"));
        item.setPrice(rs.getDouble("price"));
        item.setSpecialInstructions(rs.getString("special_instructions"));
        item.setStatus(rs.getString("status"));
        
        Timestamp prepStartTime = rs.getTimestamp("preparation_start_time");
        if (prepStartTime != null) {
            item.setPreparationStartTime(prepStartTime.toLocalDateTime());
        }
        
        Timestamp completionTime = rs.getTimestamp("completion_time");
        if (completionTime != null) {
            item.setCompletionTime(completionTime.toLocalDateTime());
        }
        
        return item;
    }
    
    /**
     * Close database resources
     * @param conn Connection to close
     * @param stmt Statement to close
     * @param rs ResultSet to close
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error closing database resources", ex);
        }
    }
}