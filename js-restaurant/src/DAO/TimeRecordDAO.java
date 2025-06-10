package DAO;

import Model.TimeRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for TimeRecord entities
 */
public class TimeRecordDAO {
    private static final Logger logger = Logger.getLogger(TimeRecordDAO.class.getName());
    private static TimeRecordDAO instance;
    private final DatabaseManager dbManager;
    
    // Private constructor for singleton pattern
    private TimeRecordDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // Get singleton instance
    public static synchronized TimeRecordDAO getInstance() {
        if (instance == null) {
            instance = new TimeRecordDAO();
        }
        return instance;
    }
    
    /**
     * Get all time records from the database
     * @return List of time records
     */
    public List<TimeRecord> getAll() throws SQLException {
        List<TimeRecord> records = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM time_clock ORDER BY clock_in DESC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                records.add(extractTimeRecordFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all time records", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return records;
    }
    
    /**
     * Get a time record by ID
     * @param id Time record ID
     * @return TimeRecord object or null if not found
     */
    public TimeRecord getById(int id) throws SQLException {
        TimeRecord record = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM time_clock WHERE time_clock_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                record = extractTimeRecordFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving time record by ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return record;
    }
    
    /**
     * Get time records for a specific employee
     * @param userId Employee's user ID
     * @return List of time records for the employee
     */
    public List<TimeRecord> getRecordsForEmployee(String userId) throws SQLException {
        List<TimeRecord> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM time_clock WHERE user_id = ? ORDER BY clock_in DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                records.add(extractTimeRecordFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving time records for employee: " + userId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return records;
    }
    
    /**
     * Get time records for a specific employee within a date range
     * @param userId Employee's user ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of time records for the employee within the date range
     */
    public List<TimeRecord> getRecordsForEmployeeAndDateRange(String userId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        List<TimeRecord> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "SELECT * FROM time_clock WHERE user_id = ? AND DATE(clock_in) BETWEEN ? AND ? " +
                         "ORDER BY clock_in ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                records.add(extractTimeRecordFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving time records for employee: " + userId + 
                    " between " + startDate + " and " + endDate, e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return records;
    }
    
    /**
     * Add a new time record (clock in)
     * @param userId Employee's user ID
     * @return true if successful, false otherwise
     */
    public boolean clockIn(String userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            String sql = "INSERT INTO time_clock (user_id, clock_in) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clocking in employee: " + userId, e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
/**
 * Clock out an employee directly
 * @param userId Employee ID to clock out
 * @return true if successful, false otherwise
 * @throws SQLException on database error
 */
public boolean clockOut(String userId) throws SQLException {
    Connection conn = null;
    PreparedStatement findStmt = null;
    PreparedStatement updateStmt = null;
    ResultSet rs = null;
    
    try {
        conn = dbManager.getConnection();
        
        System.out.println("Attempting to clock out user: " + userId);
        
        // Find the most recent clock-in without a clock-out
        String findSql = "SELECT time_clock_id, clock_in FROM time_clock " +
                        "WHERE user_id = ? AND clock_out IS NULL " +
                        "ORDER BY clock_in DESC LIMIT 1";
        findStmt = conn.prepareStatement(findSql);
        findStmt.setString(1, userId);
        rs = findStmt.executeQuery();
        
        if (rs.next()) {
            int recordId = rs.getInt("time_clock_id");
            java.sql.Timestamp clockInTimestamp = rs.getTimestamp("clock_in");
            LocalDateTime clockInTime = clockInTimestamp.toLocalDateTime();
            LocalDateTime clockOutTime = LocalDateTime.now();
            
            System.out.println("Found open time record: ID=" + recordId + ", ClockIn=" + clockInTime);
            
            // Calculate hours worked (as a decimal)
            Duration duration = Duration.between(clockInTime, clockOutTime);
            double hoursWorked = duration.toHours() + (duration.toMinutes() % 60) / 60.0;
            
            // Update the record with clock-out time and hours worked
            String updateSql = "UPDATE time_clock SET clock_out = ?, total_hours = ? " +
                            "WHERE time_clock_id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setTimestamp(1, java.sql.Timestamp.valueOf(clockOutTime));
            updateStmt.setDouble(2, hoursWorked);
            updateStmt.setInt(3, recordId);
            
            int rowsAffected = updateStmt.executeUpdate();
            
            System.out.println("Clock out update affected " + rowsAffected + " rows");
            System.out.println("Hours worked: " + hoursWorked);
            
            return rowsAffected > 0;
        } else {
            System.out.println("No open time records found for user: " + userId);
            return false;
        }
    } catch (SQLException ex) {
        logger.log(Level.SEVERE, "Error clocking out user: " + userId, ex);
        ex.printStackTrace();
        throw ex;
    } finally {
        if (rs != null) try { rs.close(); } catch (SQLException e) { }
        if (findStmt != null) try { findStmt.close(); } catch (SQLException e) { }
        if (updateStmt != null) try { updateStmt.close(); } catch (SQLException e) { }
        if (conn != null) try { conn.close(); } catch (SQLException e) { }
    }
}
    
    /**
     * Calculate hours worked between clock in and clock out
     * @param clockIn Clock in time
     * @param clockOut Clock out time
     * @return Hours worked as a decimal
     */
    private double calculateHoursWorked(LocalDateTime clockIn, LocalDateTime clockOut) {
        // Calculate difference in seconds
        long secondsDifference = java.time.Duration.between(clockIn, clockOut).getSeconds();
        
        // Convert to hours with 1 decimal place precision
        return Math.round((secondsDifference / 3600.0) * 10) / 10.0;
    }
    
    /**
     * Extract a TimeRecord object from a ResultSet
     * @param rs ResultSet containing time record data
     * @return TimeRecord object
     */
    private TimeRecord extractTimeRecordFromResultSet(ResultSet rs) throws SQLException {
        TimeRecord record = new TimeRecord();
        
        record.setRecordId(rs.getInt("time_clock_id"));
        record.setUserId(rs.getString("user_id"));
        record.setClockInTime(rs.getTimestamp("clock_in").toLocalDateTime());
        
        Timestamp clockOutTime = rs.getTimestamp("clock_out");
        if (clockOutTime != null) {
            record.setClockOutTime(clockOutTime.toLocalDateTime());
        }
        
        record.setTotalHours(rs.getDouble("total_hours"));
        
        return record;
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