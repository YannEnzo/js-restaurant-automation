/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

 

import Model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Data Access Object for User entities
 */
public class UserDAO extends BaseDAO<User> {
    
    private static UserDAO instance;
    
    // Private constructor for singleton pattern
    private UserDAO() {
        super();
    }
    
    // Get singleton instance
    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }
    
    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM user";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all users", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return users;
    }
    
    @Override
    public User getById(String id) throws SQLException {
        User user = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM user WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving user by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return user;
    }
    
    public User getByUsername(String username) throws SQLException {
        User user = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM user WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving user by username: " + username, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return user;
    }
    
    @Override
    public boolean add(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "INSERT INTO user (user_id, username, password, first_name, last_name, " +
                         "role, contact_number, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, "password123"); // Use a real hashing function in production
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getContactNumber());
            stmt.setBoolean(8, user.isActive());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding user: " + user.getUserId(), e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public boolean update(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "UPDATE user SET username = ?, first_name = ?, last_name = ?, " +
                         "role = ?, contact_number = ?, is_active = ? WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getContactNumber());
            stmt.setBoolean(6, user.isActive());
            stmt.setString(7, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user: " + user.getUserId(), e);
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
            String sql = "DELETE FROM user WHERE user_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    public boolean authenticateUser(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT password FROM user WHERE username = ? AND is_active = TRUE";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // In production, use a proper password comparison method
                return storedPassword.equals(password);
            }
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error authenticating user: " + username, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    public boolean clockInEmployee(String userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "{CALL clock_in_employee(?)}";
            stmt = conn.prepareCall(sql);
            stmt.setString(1, userId);
            
            stmt.execute();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clocking in employee: " + userId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    public boolean clockOutEmployee(String userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "{CALL clock_out_employee(?)}";
            stmt = conn.prepareCall(sql);
            stmt.setString(1, userId);
            
            stmt.execute();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clocking out employee: " + userId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String role = rs.getString("role");
        String contactNumber = rs.getString("contact_number");
        boolean isActive = rs.getBoolean("is_active");
        
        return new User(userId, username, firstName, lastName, role, contactNumber, isActive);
    }
}