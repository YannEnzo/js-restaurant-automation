package DAO;

import Model.InventoryItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Inventory entities
 */
public class InventoryDAO extends BaseDAO<InventoryItem> {
    
    private static final Logger logger = Logger.getLogger(InventoryDAO.class.getName());
    private static InventoryDAO instance;
    
    // Private constructor for singleton pattern
    private InventoryDAO() {
        super();
    }
    
    // Get singleton instance
    public static synchronized InventoryDAO getInstance() {
        if (instance == null) {
            instance = new InventoryDAO();
        }
        return instance;
    }
    
    @Override
    public List<InventoryItem> getAll() throws SQLException {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM inventory ORDER BY item_name";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                inventoryItems.add(extractInventoryItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all inventory items", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return inventoryItems;
    }
    /**
     * Get low stock inventory items
     * @return List of low stock inventory items
     */
    public List<InventoryItem> getLowStockItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM inventory WHERE is_low = TRUE";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                items.add(extractInventoryItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving low stock inventory items", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return items;
    }
    @Override
    public InventoryItem getById(String id) throws SQLException {
        InventoryItem item = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM inventory WHERE inventory_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                item = extractInventoryItemFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving inventory item by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return item;
    }
        /**
     * Extract an InventoryItem from ResultSet
     */
    private InventoryItem extractInventoryItemFromResultSet(ResultSet rs) throws SQLException {
        int inventoryId = rs.getInt("inventory_id");
        String itemName = rs.getString("item_name");
        double quantity = rs.getDouble("quantity");
        String unit = rs.getString("unit");
        double reorderLevel = rs.getDouble("reorder_level");
        boolean isLow = rs.getBoolean("is_low");
        
        // Create and return the inventory item
        // Note: In your actual schema, you might have a category field
        // For now, we'll extract it if it exists or use null
        String category = null;
        try {
            category = rs.getString("category");
        } catch (SQLException e) {
            // Category field doesn't exist, ignore
        }
        
        return new InventoryItem(inventoryId, itemName, quantity, unit, reorderLevel, isLow, category);
    }
       /**
     * Update inventory item quantity
     * @param inventoryId Inventory item ID
     * @param newQuantity New quantity
     * @return true if successful, false otherwise
     */
    public boolean updateQuantity(int inventoryId, double newQuantity) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            
            // Update quantity and check if it's below reorder level
            String sql = "UPDATE inventory SET quantity = ?, " +
                         "is_low = (quantity < reorder_level), " +
                         "last_updated = CURRENT_TIMESTAMP " +
                         "WHERE inventory_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, inventoryId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating inventory quantity", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    @Override
    public boolean add(InventoryItem item) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "INSERT INTO inventory (inventory_id, item_name, quantity, unit, " +
                         "reorder_level, is_low) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, item.getInventoryId());
            stmt.setString(2, item.getItemName());
            stmt.setDouble(3, item.getQuantity());
            stmt.setString(4, item.getUnit());
            stmt.setDouble(5, item.getReorderLevel());
            stmt.setBoolean(6, item.isLow());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding inventory item: " + item.getItemName(), e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public boolean update(InventoryItem item) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "UPDATE inventory SET item_name = ?, quantity = ?, unit = ?, " +
                         "reorder_level = ?, is_low = ?, last_updated = ? " +
                         "WHERE inventory_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getItemName());
            stmt.setDouble(2, item.getQuantity());
            stmt.setString(3, item.getUnit());
            stmt.setDouble(4, item.getReorderLevel());
            stmt.setBoolean(5, item.isLow());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(7, item.getInventoryId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating inventory item: " + item.getInventoryId(), e);
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
            String sql = "DELETE FROM inventory WHERE inventory_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting inventory item: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Get inventory items with low stock (quantity below reorder level)
     * @return List of low inventory items
     
    public List<InventoryItem> getLowStockItems() throws SQLException {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM inventory WHERE quantity < reorder_level ORDER BY item_name";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                lowStockItems.add(extractInventoryItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving low stock inventory items", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return lowStockItems;
    }
    */
    /**
     * Get inventory items by category
     * @param category Category name
     * @return List of inventory items in the category
     */
    public List<InventoryItem> getByCategory(String category) throws SQLException {
        List<InventoryItem> categoryItems = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM inventory WHERE category = ? ORDER BY item_name";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                categoryItems.add(extractInventoryItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving inventory items by category: " + category, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return categoryItems;
    }
    
    /**
     * Update inventory quantity
     * @param inventoryId Inventory ID
     * @param quantity New quantity
     * @return true if successful, false otherwise
     
    public boolean updateQuantity(int inventoryId, double quantity) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // First get current reorder level to compare with new quantity
            String selectSql = "SELECT reorder_level FROM inventory WHERE inventory_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, inventoryId);
            ResultSet rs = selectStmt.executeQuery();
            
            double reorderLevel = 0;
            if (rs.next()) {
                reorderLevel = rs.getDouble("reorder_level");
            } else {
                // Item not found, rollback and return
                conn.rollback();
                return false;
            }
            
            rs.close();
            selectStmt.close();
            
            // Update the quantity and status
            String updateSql = "UPDATE inventory SET quantity = ?, is_low = ?, last_updated = ? " +
                              "WHERE inventory_id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setDouble(1, quantity);
            stmt.setBoolean(2, quantity < reorderLevel);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, inventoryId);
            
            int rowsAffected = stmt.executeUpdate();
            
            // Commit transaction
            conn.commit();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            logger.log(Level.SEVERE, "Error updating inventory quantity: " + inventoryId, e);
            throw e;
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
    }*/
       /**
     * Extract an InventoryItem from ResultSet
     */
    
    /**
     * Get a count of inventory items by status (low, ok)
     * @return Map with status and count
     */
    public int getLowStockCount() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) as count FROM inventory WHERE is_low = TRUE";
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving low stock count", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return count;
    }
    
    /**
     * Extract InventoryItem object from ResultSet
     * @param rs ResultSet to extract from
     * @return InventoryItem object
     */
 /**
     * Close database resources
     */

}