/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

 

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Data Access Object interface
 * @param <T> Entity type this DAO manages
 */
public abstract class BaseDAO<T> {
    protected static final Logger logger = Logger.getLogger(BaseDAO.class.getName());
    protected final DatabaseManager dbManager;
    
    protected BaseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // Generic operations that all DAOs must implement
    public abstract List<T> getAll() throws SQLException;
    public abstract T getById(String id) throws SQLException;
    public abstract boolean add(T entity) throws SQLException;
    public abstract boolean update(T entity) throws SQLException;
    public abstract boolean delete(String id) throws SQLException;
    
    // Utility method to close database resources safely
    protected void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing database resources", e);
        }
    }
}