/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;
 

import DAO.MenuItemDAO;
import Model.MenuItem;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cache for menu items to reduce database queries
 */
public class MenuCache {
    private static final Logger logger = Logger.getLogger(MenuCache.class.getName());
    private static List<MenuItem> menuItems = new ArrayList<>();
    private static boolean initialized = false;
    private static long lastRefreshTime = 0;
    private static final long REFRESH_INTERVAL = 3600000; // 1 hour in milliseconds
    
    private MenuCache() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initialize the cache
     */
    public static synchronized void initialize() {
        if (!initialized) {
            refreshCache();
            initialized = true;
        }
    }
    
    /**
     * Get all menu items from cache
     * @return Unmodifiable list of menu items
     */
    public static List<MenuItem> getMenuItems() {
        checkRefresh();
        return Collections.unmodifiableList(new ArrayList<>(menuItems));
    }
    
    /**
     * Get menu items by category
     * @param categoryId The category ID
     * @return List of menu items in the specified category
     */
    public static List<MenuItem> getMenuItemsByCategory(int categoryId) {
        checkRefresh();
        List<MenuItem> result = new ArrayList<>();
        
        for (MenuItem item : menuItems) {
            if (item.getCategoryId() == categoryId) {
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Find a menu item by ID
     * @param itemId The item ID
     * @return The menu item or null if not found
     */
    public static MenuItem getMenuItemById(String itemId) {
        checkRefresh();
        
        for (MenuItem item : menuItems) {
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Force a refresh of the cache
     */
    public static synchronized void refreshCache() {
        try {
            menuItems = MenuItemDAO.getInstance().getAll();
            lastRefreshTime = System.currentTimeMillis();
            logger.info("Menu cache refreshed successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error refreshing menu cache", e);
        }
    }
    
    /**
     * Check if cache needs refreshing based on time interval
     */
    private static void checkRefresh() {
        if (!initialized || System.currentTimeMillis() - lastRefreshTime > REFRESH_INTERVAL) {
            refreshCache();
        }
    }
}