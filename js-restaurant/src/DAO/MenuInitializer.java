/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import utils.MenuImageHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Initializes and loads menu data when the application starts
 */
public class MenuInitializer {
    private static final Logger logger = Logger.getLogger(MenuInitializer.class.getName());
    
    /**
     * Initialize menu data
     * This should be called early in the application startup process
     */
    public static void initialize() {
        // Create a background task for menu initialization
        Task<Void> initTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Create menu images directory if it doesn't exist
                    createMenuImagesDirectory();
                    
                    // Extract default placeholder image
                    extractDefaultImage();
                    
                    // Extract category images
                    extractCategoryImages();
                    
                    // Extract menu item images
                    extractMenuItemImages();
                    
                    // Load menu data
                    loadMenuData();
                    
                    logger.info("Menu initialization completed successfully");
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error initializing menu data", ex);
                }
                return null;
            }
        };
        
        // Run the initialization task in a background thread
        Thread initThread = new Thread(initTask);
        initThread.setDaemon(true);
        initThread.start();
    }
    
    /**
     * Create the menu images directory if it doesn't exist
     */
    private static void createMenuImagesDirectory() {
        try {
            Path imagesDir = Paths.get("src/main/resources/images/menu");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
                logger.info("Created menu images directory: " + imagesDir);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to create images directory", ex);
        }
    }
    
    /**
     * Extract default placeholder image
     */
    private static void extractDefaultImage() {
        try {
            String imageName = "placeholder.png";
            String targetPath = "src/main/resources/images/menu/" + imageName;
            Path target = Paths.get(targetPath);
            
            // Only extract if it doesn't exist
            if (!Files.exists(target)) {
                // Try to load from resources
                URL resourceUrl = MenuInitializer.class.getResource("/images/defaults/" + imageName);
                if (resourceUrl != null) {
                    try (InputStream is = resourceUrl.openStream()) {
                        Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                        logger.info("Extracted default placeholder image to " + targetPath);
                    }
                } else {
                    // If resource not found, generate a simple placeholder
                    generatePlaceholderImage(targetPath);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not extract default placeholder image", ex);
        }
    }
    
    /**
     * Generate a simple placeholder image
     * @param targetPath Path to save the image
     */
    private static void generatePlaceholderImage(String targetPath) {
        try {
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    300, 200, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = image.createGraphics();
            
            // Fill with light gray
            g2d.setColor(java.awt.Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, 300, 200);
            
            // Draw text
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            g2d.drawString("No Image Available", 40, 100);
            
            g2d.dispose();
            
            // Save the image
            javax.imageio.ImageIO.write(image, "png", new java.io.File(targetPath));
            logger.info("Generated placeholder image at " + targetPath);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to generate placeholder image", ex);
        }
    }
    
    /**
     * Extract category images
     */
    private static void extractCategoryImages() {
        // Category image mapping
        String[][] categoryImages = {
            {"1", "appetizers.jpg"},
            {"2", "salads.jpg"},
            {"3", "entrees.jpg"},
            {"4", "sandwiches.jpg"},
            {"5", "burgers.jpg"},
            {"6", "sides.jpg"},
            {"7", "beverages.jpg"}
        };
        
        for (String[] categoryImage : categoryImages) {
            try {
                String categoryId = categoryImage[0];
                String imageName = categoryImage[1];
                String targetPath = "src/main/resources/images/menu/category_" + categoryId + ".jpg";
                Path target = Paths.get(targetPath);
                
                // Only extract if it doesn't exist
                if (!Files.exists(target)) {
                    // Try to load from resources
                    URL resourceUrl = MenuInitializer.class.getResource("/images/categories/" + imageName);
                    if (resourceUrl != null) {
                        try (InputStream is = resourceUrl.openStream()) {
                            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Extracted category image to " + targetPath);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not extract category image", ex);
            }
        }
    }
    
    /**
     * Extract menu item images
     */
    private static void extractMenuItemImages() {
        // Menu item image mapping
        String[][] menuItemImages = {
            // Appetizers
            {"APP001", "chicken_nachos.jpg"},
            {"APP002", "pork_nachos.jpg"},
            {"APP003", "sliders.jpg"},
            {"APP004", "catfish_bites.jpg"},
            {"APP005", "fried_veggies.jpg"},
            
            // Salads
            {"SAL001", "house_salad.jpg"},
            {"SAL002", "wedge_salad.jpg"},
            {"SAL003", "caesar_salad.jpg"},
            {"SAL004", "sweet_potato_chicken_salad.jpg"},
            
            // Entrees
            {"ENT001", "shrimp_grits.jpg"},
            {"ENT002", "sweet_tea_chicken.jpg"},
            {"ENT003", "caribbean_chicken.jpg"},
            {"ENT004", "pork_chops.jpg"},
            {"ENT005", "ny_strip.jpg"},
            {"ENT006", "seared_tuna.jpg"},
            {"ENT007", "chicken_tenders.jpg"},
            {"ENT008", "grouper_fingers.jpg"},
            {"ENT009", "mac_cheese_bar.jpg"},
            
            // Sandwiches
            {"SAN001", "grilled_cheese.jpg"},
            {"SAN002", "chicken_blta.jpg"},
            {"SAN003", "philly.jpg"},
            {"SAN004", "club.jpg"},
            {"SAN005", "meatball_sub.jpg"},
            
            // Burgers
            {"BUR001", "bacon_cheeseburger.jpg"},
            {"BUR002", "carolina_burger.jpg"},
            {"BUR003", "portobello_burger.jpg"},
            {"BUR004", "vegan_burger.jpg"},
            
            // Sides
            {"SID001", "curly_fries.jpg"},
            {"SID002", "wing_chips.jpg"},
            {"SID003", "sweet_potato_fries.jpg"},
            {"SID004", "cabbage_slaw.jpg"},
            {"SID005", "cheese_grits.jpg"},
            {"SID006", "mashed_potatoes.jpg"},
            {"SID007", "mac_cheese.jpg"},
            {"SID008", "seasonal_vegetables.jpg"},
            {"SID009", "baked_beans.jpg"},
            
            // Beverages
            {"BEV001", "sweet_tea.jpg"},
            {"BEV002", "unsweetened_tea.jpg"},
            {"BEV003", "coke.jpg"},
            {"BEV004", "diet_coke.jpg"},
            {"BEV005", "sprite.jpg"},
            {"BEV006", "bottled_water.jpg"},
            {"BEV007", "lemonade.jpg"},
            {"BEV008", "orange_juice.jpg"}
        };
        
        for (String[] menuItemImage : menuItemImages) {
            try {
                String itemId = menuItemImage[0];
                String imageName = menuItemImage[1];
                String targetPath = "src/main/resources/images/menu/" + itemId.toLowerCase() + ".jpg";
                Path target = Paths.get(targetPath);
                
                // Only extract if it doesn't exist
                if (!Files.exists(target)) {
                    // Try to load from resources
                    URL resourceUrl = MenuInitializer.class.getResource("/images/menu-items/" + imageName);
                    if (resourceUrl != null) {
                        try (InputStream is = resourceUrl.openStream()) {
                            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Extracted menu item image to " + targetPath);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not extract menu item image: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Load menu data
     */
    private static void loadMenuData() {
        try {
            // Create a menu data loader
            MenuDataLoader dataLoader = new MenuDataLoader();
            
            // Load menu items
            boolean success = dataLoader.loadMenuItems();
            
            if (success) {
                logger.info("Menu data loaded successfully");
                
                // Refresh the menu cache
                Platform.runLater(() -> {
                    MenuCache.refreshCache();
                    logger.info("Menu cache refreshed");
                });
            } else {
                logger.warning("Failed to load menu data");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading menu data", ex);
        }
    }
}
