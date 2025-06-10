package DAO;

import Model.MenuItem;
import Model.MenuItemAddon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for MenuItem entities
 */
public class MenuItemDAO {
    private static final Logger logger = Logger.getLogger(MenuItemDAO.class.getName());
    private static MenuItemDAO instance;
    private final DatabaseManager dbConnection;
    
    // Private constructor for singleton pattern
    private MenuItemDAO() {
        this.dbConnection = DatabaseManager.getInstance();
    }
    
    // Get singleton instance
    public static synchronized MenuItemDAO getInstance() {
        if (instance == null) {
            instance = new MenuItemDAO();
        }
        return instance;
    }
    
    /**
     * Get all menu items from the database
     * @return List of menu items
     */
    public List<MenuItem> getAll() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT m.*, c.name as category_name " +
                         "FROM menu_item m " +
                         "JOIN menu_category c ON m.category_id = c.category_id";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                MenuItem item = extractMenuItemFromResultSet(rs);
                // Load add-ons for this menu item
                item.setAddons(getAddonsByMenuItemId(item.getItemId(), conn));
                menuItems.add(item);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving all menu items", ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return menuItems;
    }
    private int preparationTime; 
    public int getPreparationTime() {
    return preparationTime; // You should have this field in your class
}
    /**
     * Get a menu item by ID
     * @param id Menu item ID
     * @return MenuItem object or null if not found
     */
    public MenuItem getById(String id) throws SQLException {
        MenuItem menuItem = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT m.*, c.name as category_name " +
                         "FROM menu_item m " +
                         "JOIN menu_category c ON m.category_id = c.category_id " +
                         "WHERE m.item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                menuItem = extractMenuItemFromResultSet(rs);
                // Load add-ons for this menu item
                menuItem.setAddons(getAddonsByMenuItemId(menuItem.getItemId(), conn));
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving menu item by ID: " + id, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return menuItem;
    }
    
    /**
     * Get menu items by category
     * @param categoryId Category ID
     * @return List of menu items in the category
     */
    public List<MenuItem> getByCategory(int categoryId) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT m.*, c.name as category_name " +
                         "FROM menu_item m " +
                         "JOIN menu_category c ON m.category_id = c.category_id " +
                         "WHERE m.category_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                MenuItem item = extractMenuItemFromResultSet(rs);
                // Load add-ons for this menu item
                item.setAddons(getAddonsByMenuItemId(item.getItemId(), conn));
                menuItems.add(item);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving menu items by category: " + categoryId, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return menuItems;
    }
    
    /**
     * Get available menu items
     * @return List of available menu items
     */
    public List<MenuItem> getAvailableItems() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT m.*, c.name as category_name " +
                         "FROM menu_item m " +
                         "JOIN menu_category c ON m.category_id = c.category_id " +
                         "WHERE m.is_available = TRUE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                MenuItem item = extractMenuItemFromResultSet(rs);
                // Load add-ons for this menu item
                item.setAddons(getAddonsByMenuItemId(item.getItemId(), conn));
                menuItems.add(item);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving available menu items", ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return menuItems;
    }
    
    /**
     * Add a new menu item
     * @param menuItem Menu item to add
     * @return true if successful, false otherwise
     */
    public boolean add(MenuItem menuItem) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Insert menu item
            String sql = "INSERT INTO menu_item (item_id, name, description, category_id, " +
                         "price, is_available) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, menuItem.getItemId());
            stmt.setString(2, menuItem.getName());
            stmt.setString(3, menuItem.getDescription());
            stmt.setInt(4, menuItem.getCategoryId());
            stmt.setDouble(5, menuItem.getPrice());
            stmt.setBoolean(6, menuItem.isAvailable());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0 && menuItem.getAddons() != null && !menuItem.getAddons().isEmpty()) {
                // Add add-ons for this menu item
                for (MenuItemAddon addon : menuItem.getAddons()) {
                    addon.setItemId(menuItem.getItemId());
                    addAddon(addon, conn);
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
            logger.log(Level.SEVERE, "Error adding menu item: " + menuItem.getItemId(), ex);
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
     * Update an existing menu item
     * @param menuItem Menu item to update
     * @return true if successful, false otherwise
     */
    public boolean update(MenuItem menuItem) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Update menu item
            String sql = "UPDATE menu_item SET name = ?, description = ?, category_id = ?, " +
                         "price = ?, is_available = ? WHERE item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, menuItem.getName());
            stmt.setString(2, menuItem.getDescription());
            stmt.setInt(3, menuItem.getCategoryId());
            stmt.setDouble(4, menuItem.getPrice());
            stmt.setBoolean(5, menuItem.isAvailable());
            stmt.setString(6, menuItem.getItemId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0 && menuItem.getAddons() != null) {
                // Remove existing add-ons
                deleteAddons(menuItem.getItemId(), conn);
                
                // Add new add-ons
                for (MenuItemAddon addon : menuItem.getAddons()) {
                    addon.setItemId(menuItem.getItemId());
                    addAddon(addon, conn);
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
            logger.log(Level.SEVERE, "Error updating menu item: " + menuItem.getItemId(), ex);
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
     * Delete a menu item
     * @param id Menu item ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Delete add-ons first (foreign key constraint)
            deleteAddons(id, conn);
            
            // Delete menu item
            String sql = "DELETE FROM menu_item WHERE item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            
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
            logger.log(Level.SEVERE, "Error deleting menu item: " + id, ex);
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
     * Update menu item availability
     * @param id Menu item ID
     * @param available Availability status
     * @return true if successful, false otherwise
     */
    public boolean updateAvailability(String id, boolean available) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            String sql = "UPDATE menu_item SET is_available = ? WHERE item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, available);
            stmt.setString(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating menu item availability: " + id, ex);
            throw ex;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Get add-ons for a menu item
     * @param menuItemId Menu item ID
     * @param existingConn Existing database connection (optional)
     * @return List of add-ons for the menu item
     */
    private List<MenuItemAddon> getAddonsByMenuItemId(String menuItemId, Connection existingConn) throws SQLException {
        List<MenuItemAddon> addons = new ArrayList<>();
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
            
            String sql = "SELECT * FROM menu_item_addon WHERE item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, menuItemId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                MenuItemAddon addon = extractAddonFromResultSet(rs);
                addons.add(addon);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving add-ons for menu item: " + menuItemId, ex);
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
        
        return addons;
    }
    
    /**
     * Add an add-on for a menu item
     * @param addon Add-on to add
     * @param conn Existing database connection
     * @return true if successful, false otherwise
     */
    private boolean addAddon(MenuItemAddon addon, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        
        try {
            String sql = "INSERT INTO menu_item_addon (item_id, name, price) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, addon.getItemId());
            stmt.setString(2, addon.getName());
            stmt.setDouble(3, addon.getPrice());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error adding menu item add-on", ex);
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * Delete all add-ons for a menu item
     * @param menuItemId Menu item ID
     * @param conn Existing database connection
     * @return true if successful, false otherwise
     */
    private boolean deleteAddons(String menuItemId, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        
        try {
            String sql = "DELETE FROM menu_item_addon WHERE item_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, menuItemId);
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting menu item add-ons", ex);
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    /**
     * Extract a MenuItem object from a ResultSet
     * @param rs ResultSet containing menu item data
     * @return MenuItem object
     */
    private MenuItem extractMenuItemFromResultSet(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        
        item.setItemId(rs.getString("item_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setPrice(rs.getDouble("price"));
        item.setAvailable(rs.getBoolean("is_available"));
        
        return item;
    }
    
    /**
     * Extract a MenuItemAddon object from a ResultSet
     * @param rs ResultSet containing add-on data
     * @return MenuItemAddon object
     */
    private MenuItemAddon extractAddonFromResultSet(ResultSet rs) throws SQLException {
        MenuItemAddon addon = new MenuItemAddon();
        
        addon.setAddonId(rs.getInt("addon_id"));
        addon.setItemId(rs.getString("item_id"));
        addon.setName(rs.getString("name"));
        addon.setPrice(rs.getDouble("price"));
        
        return addon;
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