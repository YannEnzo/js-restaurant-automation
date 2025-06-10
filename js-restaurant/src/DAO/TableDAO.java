/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;
 

import Model.RestaurantTable;
import DAO.TableStatusObserver;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Data Access Object for RestaurantTable entities
 * Includes observer pattern implementation for table status changes
 */
public class TableDAO extends BaseDAO<RestaurantTable> {
    
    private static TableDAO instance;
    private final List<TableStatusObserver> observers = new CopyOnWriteArrayList<>();
    
    // Private constructor for singleton pattern
    private TableDAO() {
        super();
    }
    
    // Get singleton instance
    public static synchronized TableDAO getInstance() {
        if (instance == null) {
            instance = new TableDAO();
        }
        return instance;
    }
    
    // Observer pattern methods
    public void addObserver(TableStatusObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(TableStatusObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyTableStatusChanged(String tableId, String newStatus) {
        for (TableStatusObserver observer : observers) {
            observer.onTableStatusChanged(tableId, newStatus);
        }
    }
    
    @Override
    public List<RestaurantTable> getAll() throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM restaurant_table";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                tables.add(extractTableFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all tables", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return tables;
    }
    
    @Override
    public RestaurantTable getById(String id) throws SQLException {
        RestaurantTable table = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM restaurant_table WHERE table_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                table = extractTableFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving table by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return table;
    }
    /**
 * Get a table by its table number
 * @param tableNumber The table number (e.g., "A1")
 * @return The table, or null if not found
 * @throws SQLException If a database error occurs
 */
public RestaurantTable getByTableNumber(String tableNumber) throws SQLException {
    RestaurantTable table = null;
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = dbManager.getConnection();
        String sql = "SELECT * FROM restaurant_table WHERE table_number = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, tableNumber);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            table = extractTableFromResultSet(rs);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error retrieving table by number: " + tableNumber, e);
        throw e;
    } finally {
        closeResources(conn, stmt, rs);
    }
    
    return table;
}

    @Override
    public boolean add(RestaurantTable table) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "INSERT INTO restaurant_table (table_id, table_number, status, " +
                         "capacity, location_x, location_y, assigned_waiter_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, table.getTableId());
            stmt.setString(2, table.getTableNumber());
            stmt.setString(3, table.getStatus());
            stmt.setInt(4, table.getCapacity());
            stmt.setInt(5, table.getLocationX());
            stmt.setInt(6, table.getLocationY());
            stmt.setString(7, table.getAssignedWaiterId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding table: " + table.getTableId(), e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public boolean update(RestaurantTable table) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "UPDATE restaurant_table SET table_number = ?, status = ?, " +
                         "capacity = ?, location_x = ?, location_y = ?, assigned_waiter_id = ? " +
                         "WHERE table_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, table.getTableNumber());
            stmt.setString(2, table.getStatus());
            stmt.setInt(3, table.getCapacity());
            stmt.setInt(4, table.getLocationX());
            stmt.setInt(5, table.getLocationY());
            stmt.setString(6, table.getAssignedWaiterId());
            stmt.setString(7, table.getTableId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Notify observers about the table status change
                notifyTableStatusChanged(table.getTableId(), table.getStatus());
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating table: " + table.getTableId(), e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public boolean delete(String id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "DELETE FROM restaurant_table WHERE table_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting table: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    public boolean updateTableStatus(String tableId, String status, String waiterId) throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;

    try {
        conn = dbManager.getConnection();

        // SQL query to update the status and assigned waiter (if provided)
        String sql = "UPDATE restaurant_table SET status = ?, assigned_waiter_id = ? WHERE table_id = ?";
        stmt = conn.prepareStatement(sql);
        
        // Set parameters
        stmt.setString(1, status);           // Set the status (AVAILABLE, OCCUPIED, etc.)
        stmt.setString(2, waiterId);          // Set the assigned waiter, or NULL if not assigned
        stmt.setString(3, tableId);           // Identify the table to update

        // Execute the update
        int rowsAffected = stmt.executeUpdate();  
        return rowsAffected > 0;              // Return true if the update was successful
    } catch (SQLException ex) {
        logger.log(Level.SEVERE, "Error updating table status", ex);
        return false;  // Return false if an error occurred
    } finally {
        closeResources(conn, stmt, null);  // Always close resources after use
    }
}


    
    public List<RestaurantTable> getTablesByStatus(String status) throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM restaurant_table WHERE status = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                tables.add(extractTableFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving tables by status: " + status, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return tables;
    }
    
    public List<RestaurantTable> getTablesByWaiter(String waiterId) throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM restaurant_table WHERE assigned_waiter_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, waiterId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                tables.add(extractTableFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving tables by waiter: " + waiterId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return tables;
    }
    
    public boolean assignWaiterToTable(String tableId, String waiterId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "UPDATE restaurant_table SET assigned_waiter_id = ? WHERE table_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, waiterId);
            stmt.setString(2, tableId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error assigning waiter to table: " + tableId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private RestaurantTable extractTableFromResultSet(ResultSet rs) throws SQLException {
        String tableId = rs.getString("table_id");
        String tableNumber = rs.getString("table_number");
        String status = rs.getString("status");
        int capacity = rs.getInt("capacity");
        int locationX = rs.getInt("location_x");
        int locationY = rs.getInt("location_y");
        String assignedWaiterId = rs.getString("assigned_waiter_id");
        
        return new RestaurantTable(tableId, tableNumber, status, capacity, 
                                  locationX, locationY, assignedWaiterId);
    }
    /**
 * Get table count grouped by status
 * @return Map with status as key and count as value
 */
public Map<String, Integer> getTableStatusSummary() throws SQLException {
    Map<String, Integer> statusCounts = new HashMap<>();
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = dbManager.getConnection();
        stmt = conn.createStatement();
        String sql = "SELECT status, COUNT(*) as count FROM restaurant_table GROUP BY status";
        rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            statusCounts.put(rs.getString("status"), rs.getInt("count"));
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error retrieving table status summary", e);
        throw e;
    } finally {
        closeResources(conn, stmt, rs);
    }
    
    return statusCounts;
}
}