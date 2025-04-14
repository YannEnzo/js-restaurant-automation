package Tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTest {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/js_restaurant";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASS = "yannenzo"; // Replace with your MySQL password

    private static Connection connection;

    public static void main(String[] args) {
        try {
            // Establish connection
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connection successful!");

            // Test various database operations
            testUserTable();
            testTablesTable();
            testMenuItems();
            testOrderOperations();
            testStoredProcedure();

            // Close connection
            connection.close();
            System.out.println("Database connection closed.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testUserTable() throws SQLException {
        System.out.println("\n--- Testing User Table ---");
        
        // Test SELECT query
        String sql = "SELECT * FROM user";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("Users in the database:");
        while (rs.next()) {
            String userId = rs.getString("user_id");
            String username = rs.getString("username");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String role = rs.getString("role");
            
            System.out.println(userId + ": " + firstName + " " + lastName + 
                               " (" + username + ") - " + role);
        }
        
        // Test INSERT query
        sql = "INSERT INTO user (user_id, username, password, first_name, last_name, role, contact_number) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "USER00");
        pstmt.setString(2, "waiter");
        pstmt.setString(3, "password123");
        pstmt.setString(4, "Jane");
        pstmt.setString(5, "Doe");
        pstmt.setString(6, "WAITER");
        pstmt.setString(7, "770-555-1234");
        
        int rowsAffected = pstmt.executeUpdate();
        System.out.println("Inserted " + rowsAffected + " new user(s)");
        
        rs.close();
        stmt.close();
        pstmt.close();
    }

    private static void testTablesTable() throws SQLException {
        System.out.println("\n--- Testing Restaurant Table ---");
        
        // Test SELECT query with WHERE clause
        String sql = "SELECT * FROM restaurant_table WHERE status = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "AVAILABLE");
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Available tables:");
        while (rs.next()) {
            String tableId = rs.getString("table_id");
            String tableNumber = rs.getString("table_number");
            int capacity = rs.getInt("capacity");
            
            System.out.println(tableId + ": Table " + tableNumber + 
                               " (Capacity: " + capacity + ")");
        }
        
        // Test UPDATE query
        sql = "UPDATE restaurant_table SET assigned_waiter_id = ? WHERE table_id = ?";
        pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "USER002");
        pstmt.setString(2, "TABLE001");
        
        int rowsAffected = pstmt.executeUpdate();
        System.out.println("Updated " + rowsAffected + " table(s)");
        
        rs.close();
        pstmt.close();
    }

    private static void testMenuItems() throws SQLException {
        System.out.println("\n--- Testing Menu Items ---");
        
        // Test JOIN query
        String sql = "SELECT m.item_id, m.name, m.price, c.name as category " +
                     "FROM menu_item m JOIN menu_category c ON m.category_id = c.category_id " +
                     "ORDER BY c.display_order, m.name";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("Menu items by category:");
        String currentCategory = "";
        
        while (rs.next()) {
            String itemId = rs.getString("item_id");
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            String category = rs.getString("category");
            
            if (!category.equals(currentCategory)) {
                System.out.println("\n" + category + ":");
                currentCategory = category;
            }
            
            System.out.println("  " + itemId + ": " + name + " - $" + price);
        }
        
        // Test querying with add-ons
        sql = "SELECT m.name as item_name, a.name as addon_name, a.price as addon_price " +
              "FROM menu_item m JOIN menu_item_addon a ON m.item_id = a.item_id " +
              "ORDER BY m.name, a.name";
        
        rs = stmt.executeQuery(sql);
        
        System.out.println("\nMenu items with add-ons:");
        while (rs.next()) {
            String itemName = rs.getString("item_name");
            String addonName = rs.getString("addon_name");
            double addonPrice = rs.getDouble("addon_price");
            
            System.out.println(itemName + " - " + addonName + " (+$" + addonPrice + ")");
        }
        
        rs.close();
        stmt.close();
    }

    private static void testOrderOperations() throws SQLException {
        System.out.println("\n--- Testing Order Operations ---");
        
        // Create a new order
        String orderId = "ORD" + String.format("%07d", (int)(Math.random() * 10000000));
        String sql = "INSERT INTO `order` (order_id, table_id, waiter_id, status) " +
                    "VALUES (?, ?, ?, ?)";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        pstmt.setString(2, "TABLE002");
        pstmt.setString(3, "USER002");
        pstmt.setString(4, "NEW");
        
        int rowsAffected = pstmt.executeUpdate();
        System.out.println("Created new order: " + orderId);
        
        // Add items to the order
        sql = "INSERT INTO order_item (order_id, menu_item_id, quantity, seat_number, price) " +
              "VALUES (?, ?, ?, ?, (SELECT price FROM menu_item WHERE item_id = ?))";
        
        pstmt = connection.prepareStatement(sql);
        
        // Add a burger
        pstmt.setString(1, orderId);
        pstmt.setString(2, "ITEM005");
        pstmt.setInt(3, 1);
        pstmt.setInt(4, 1);
        pstmt.setString(5, "ITEM005");
        pstmt.executeUpdate();
        
        // Add a drink
        pstmt.setString(1, orderId);
        pstmt.setString(2, "ITEM006");
        pstmt.setInt(3, 2);
        pstmt.setInt(4, 1);
        pstmt.setString(5, "ITEM006");
        pstmt.executeUpdate();
        
        System.out.println("Added items to order");
        
        // Calculate and update the order total
        sql = "UPDATE `order` o " +
              "SET total_amount = (SELECT SUM(price * quantity) FROM order_item WHERE order_id = ?), " +
              "    tax_amount = (SELECT SUM(price * quantity) FROM order_item WHERE order_id = ?) * 0.10 " +
              "WHERE o.order_id = ?";
        
        pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        pstmt.setString(2, orderId);
        pstmt.setString(3, orderId);
        pstmt.executeUpdate();
        
        // Query the order details
        sql = "SELECT o.order_id, o.total_amount, o.tax_amount, " +
              "       i.name, oi.quantity, oi.price " +
              "FROM `order` o " +
              "JOIN order_item oi ON o.order_id = oi.order_id " +
              "JOIN menu_item i ON oi.menu_item_id = i.item_id " +
              "WHERE o.order_id = ?";
        
        pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, orderId);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\nOrder details for " + orderId + ":");
        double totalAmount = 0;
        double taxAmount = 0;
        
        System.out.println("Items:");
        while (rs.next()) {
            if (totalAmount == 0) {
                totalAmount = rs.getDouble("total_amount");
                taxAmount = rs.getDouble("tax_amount");
            }
            
            String itemName = rs.getString("name");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            
            System.out.println("  " + quantity + "x " + itemName + " @ $" + price + 
                               " = $" + (quantity * price));
        }
        
        System.out.println("Subtotal: $" + totalAmount);
        System.out.println("Tax: $" + taxAmount);
        System.out.println("Total: $" + (totalAmount + taxAmount));
        
        rs.close();
        pstmt.close();
    }

    private static void testStoredProcedure() throws SQLException {
        System.out.println("\n--- Testing Stored Procedure ---");
        
        // Test the clock_in_employee procedure
        String sql = "{CALL clock_in_employee(?)}";
        CallableStatement cstmt = connection.prepareCall(sql);
        cstmt.setString(1, "USER003");
        cstmt.execute();
        
        System.out.println("Called clock_in_employee for USER003");
        
        // Check the time_clock table
        sql = "SELECT * FROM time_clock WHERE user_id = ? ORDER BY clock_in DESC LIMIT 1";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "USER003");
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int timeClockId = rs.getInt("time_clock_id");
            String clockIn = rs.getTimestamp("clock_in").toString();
            
            System.out.println("USER003 clocked in at " + clockIn + " (time_clock_id: " + timeClockId + ")");
        }
        
        // Test the update_table_status procedure
        sql = "{CALL update_table_status(?, ?, ?)}";
        cstmt = connection.prepareCall(sql);
        cstmt.setString(1, "TABLE003");
        cstmt.setString(2, "OCCUPIED");
        cstmt.setString(3, "USER001"); // Manager
        cstmt.execute();
        
        System.out.println("Called update_table_status for TABLE003");
        
        // Check the restaurant_table and activity_log tables
        sql = "SELECT status FROM restaurant_table WHERE table_id = ?";
        pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "TABLE003");
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String status = rs.getString("status");
            System.out.println("TABLE003 status is now: " + status);
        }
        
        sql = "SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT 1";
        pstmt = connection.prepareStatement(sql);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String actionType = rs.getString("action_type");
            String description = rs.getString("description");
            String timestamp = rs.getTimestamp("timestamp").toString();
            
            System.out.println("Latest activity: " + actionType + " at " + timestamp + " - " + description);
        }
        
        rs.close();
        pstmt.close();
        cstmt.close();
    }
}