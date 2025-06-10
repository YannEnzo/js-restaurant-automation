package DAO;

import Model.TopSellerItem;
import Model.KitchenPerformanceItem;
import Model.Order;
import Model.OrderItem;
import Model.MenuItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ReportDAO for generating reports
 */
public class ReportDAOImpl implements ReportDAOint {
    private static final Logger logger = Logger.getLogger(ReportDAOImpl.class.getName());
    private static ReportDAOImpl instance;
    private final DatabaseManager dbManager;
    
    private ReportDAOImpl() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // Get singleton instance
    public static synchronized ReportDAOImpl getInstance() {
        if (instance == null) {
            instance = new ReportDAOImpl();
        }
        return instance;
    }
    
    @Override
    public List<TopSellerItem> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) throws SQLException {
        List<TopSellerItem> topSellers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            
            // SQL query to get top selling items
            String sql = 
                "SELECT mi.item_id, mi.name, mc.name as category_name, " +
                "COUNT(DISTINCT oi.order_id) as order_count, " +
                "SUM(oi.quantity) as total_quantity, " +
                "SUM(oi.quantity * oi.price) as total_revenue " +
                "FROM order_item oi " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.item_id " +
                "JOIN menu_category mc ON mi.category_id = mc.category_id " +
                "JOIN `order` o ON oi.order_id = o.order_id " +
                "WHERE o.payment_status = 'COMPLETED' " +
                "AND DATE(o.payment_datetime) BETWEEN ? AND ? " +
                "GROUP BY mi.item_id " +
                "ORDER BY total_quantity DESC " +
                "LIMIT ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            stmt.setInt(3, limit);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                topSellers.add(new TopSellerItem(
                    rs.getString("item_id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    rs.getInt("order_count"),
                    rs.getInt("total_quantity"),
                    rs.getDouble("total_revenue")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving top selling items", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return topSellers;
    }
    
    @Override
    public List<KitchenPerformanceItem> getKitchenPerformance(
            LocalDate startDate, LocalDate endDate, 
            String categoryFilter, String staffFilter) throws SQLException {
        
        List<KitchenPerformanceItem> performanceItems = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            
            StringBuilder sql = new StringBuilder(
                "SELECT mi.item_id, mi.name, mc.name as category_name, " +
                "mi.preparation_time as target_time, " +
                "COUNT(oi.order_item_id) as order_count, " +
                "AVG(TIMESTAMPDIFF(SECOND, oi.preparation_start_time, oi.completion_time)) as avg_prep_time_seconds " +
                "FROM order_item oi " +
                "JOIN menu_item mi ON oi.menu_item_id = mi.item_id " +
                "JOIN menu_category mc ON mi.category_id = mc.category_id " +
                "JOIN `order` o ON oi.order_id = o.order_id " +
                "WHERE oi.preparation_start_time IS NOT NULL " +
                "AND oi.completion_time IS NOT NULL " +
                "AND DATE(o.order_datetime) BETWEEN ? AND ? ");
            
            // Add category filter if specified
            if (categoryFilter != null && !categoryFilter.equals("All Categories")) {
                sql.append("AND mc.name = ? ");
            }
            
            // Add staff filter if specified
            if (staffFilter != null && !staffFilter.equals("All Staff")) {
                sql.append("AND o.waiter_id = ? ");
            }
            
            sql.append(
                "GROUP BY mi.item_id " +
                "ORDER BY order_count DESC");
            
            stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            // Set date parameters
            stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            
            // Set category filter parameter if specified
            if (categoryFilter != null && !categoryFilter.equals("All Categories")) {
                stmt.setString(paramIndex++, categoryFilter);
            }
            
            // Set staff filter parameter if specified
            if (staffFilter != null && !staffFilter.equals("All Staff")) {
                stmt.setString(paramIndex++, staffFilter);
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Convert seconds to minutes
                double avgPrepTimeMinutes = rs.getDouble("avg_prep_time_seconds") / 60.0;
                
                performanceItems.add(new KitchenPerformanceItem(
                    rs.getString("item_id"),
                    rs.getString("name"),
                    rs.getString("category_name"),
                    avgPrepTimeMinutes,
                    rs.getInt("target_time"),
                    rs.getInt("order_count")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving kitchen performance data", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return performanceItems;
    }
    
    @Override
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, Double> revenueMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            
            String sql = 
                "SELECT DATE(payment_datetime) as sale_date, " +
                "SUM(total_amount) as total_revenue " +
                "FROM `order` " +
                "WHERE payment_status = 'COMPLETED' " +
                "AND DATE(payment_datetime) BETWEEN ? AND ? " +
                "GROUP BY DATE(payment_datetime) " +
                "ORDER BY sale_date";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                double revenue = rs.getDouble("total_revenue");
                revenueMap.put(date, revenue);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving daily revenue", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return revenueMap;
    }
    
    @Override
    public Map<LocalDate, Integer> getDailyCustomerCount(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, Integer> customerCountMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            
            String sql = 
                "SELECT DATE(order_datetime) as order_date, " +
                "COUNT(DISTINCT table_id) as customer_count " +
                "FROM `order` " +
                "WHERE DATE(order_datetime) BETWEEN ? AND ? " +
                "GROUP BY DATE(order_datetime) " +
                "ORDER BY order_date";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                LocalDate date = rs.getDate("order_date").toLocalDate();
                int count = rs.getInt("customer_count");
                customerCountMap.put(date, count);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving daily customer count", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return customerCountMap;
    }
    
    /**
     * Close database resources
     */
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error closing database resources", ex);
        }
    }
}