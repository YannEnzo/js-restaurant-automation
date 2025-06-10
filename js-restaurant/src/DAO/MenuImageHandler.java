/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Utility class to handle menu item images
 */
public class MenuImageHandler {
    private static final Logger logger = Logger.getLogger(MenuImageHandler.class.getName());
    
    // Map to cache image paths
    private static final Map<String, String> imageCache = new HashMap<>();
    
    // Default colors for different menu categories
    private static final Map<Integer, Color> categoryColors = new HashMap<>();
    
    static {
        // Initialize category colors
        categoryColors.put(1, new Color(255, 152, 0));  // Appetizers: Orange
        categoryColors.put(2, new Color(76, 175, 80));   // Salads: Green
        categoryColors.put(3, new Color(244, 67, 54));   // Entrees: Red
        categoryColors.put(4, new Color(156, 39, 176));  // Sandwiches: Purple
        categoryColors.put(5, new Color(121, 85, 72));   // Burgers: Brown
        categoryColors.put(6, new Color(139, 195, 74));  // Sides: Light Green
        categoryColors.put(7, new Color(33, 150, 243));  // Beverages: Blue
    }
    
    /**
     * Ensure the images directory exists
     */
    private static void ensureImagesDirectoryExists() {
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
     * Get the image path for a menu item
     * @param itemId Menu item ID
     * @param categoryId Category ID
     * @return Path to the image file or null if not found
     */
    public static String getImagePath(String itemId, int categoryId) {
    // Check cache first
    if (imageCache.containsKey(itemId)) {
        return imageCache.get(itemId);
    }

    // Ensure images directory exists
    ensureImagesDirectoryExists();

    // Try to find image file based on item ID
    String basePath = "/main.resources.images.menu/";
    String[] possibleExtensions = {".jpg", ".png", ".jpeg", ".gif"};

    for (String ext : possibleExtensions) {
        String imagePath = basePath + itemId.toLowerCase() + ext;
        URL resourceUrl = MenuImageHandler.class.getResource(imagePath);

        if (resourceUrl != null) {
            // Image found, cache it and return
            imageCache.put(itemId, imagePath);
            return imagePath;
        }
    }

    // Image not found, generate default one for this category
    String generatedImagePath = generateDefaultImage(itemId, categoryId);
    if (generatedImagePath != null) {
        imageCache.put(itemId, generatedImagePath);
        return generatedImagePath;
    }

    // Return a generic placeholder
    return basePath + "placeholder.png";
}

    
    /**
     * Generate a default image for a menu item
     * @param itemId Menu item ID
     * @param categoryId Category ID
     * @return Path to the generated image
     */
    private static String generateDefaultImage(String itemId, int categoryId) {
        try {
            // Create a colored rectangle for this category
            int width = 300;
            int height = 200;
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Fill with category color or default to gray
            Color color = categoryColors.getOrDefault(categoryId, Color.LIGHT_GRAY);
            g2d.setColor(color);
            g2d.fillRect(0, 0, width, height);
            
            // Draw item ID text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            String text = itemId;
            int textWidth = fm.stringWidth(text);
            int x = (width - textWidth) / 2;
            int y = height / 2;
            g2d.drawString(text, x, y);
            
            g2d.dispose();
            
            // Save the image
            String imagePath = "/images/menu/" + itemId.toLowerCase() + ".png";
            String fullPath = "src/main/resources" + imagePath;
            File outputFile = new File(fullPath);
            ImageIO.write(image, "png", outputFile);
            
            logger.info("Generated default image for " + itemId + " at " + fullPath);
            return imagePath;
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to generate default image for " + itemId, ex);
            return null;
        }
    }
    
    /**
     * Copy a menu item image from a source path to the images directory
     * @param itemId Menu item ID
     * @param sourcePath Source path for the image file
     * @return Path to the copied image
     */
    public static String copyImage(String itemId, String sourcePath) {
        try {
            // Ensure images directory exists
            ensureImagesDirectoryExists();
            
            // Get source file extension
            String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
            
            // Create target file
            String targetFileName = itemId.toLowerCase() + extension;
            String targetPath = "src/main/resources/images/menu/" + targetFileName;
            
            // Copy file
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            
            // Cache and return path
            String imagePath = "/images/menu/" + targetFileName;
            imageCache.put(itemId, imagePath);
            
            logger.info("Copied image for " + itemId + " to " + targetPath);
            return imagePath;
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to copy image for " + itemId, ex);
            return null;
        }
    }
    
    /**
     * Upload an image from an input stream
     * @param itemId Menu item ID
     * @param inputStream Input stream for the image file
     * @param extension File extension (e.g., ".jpg")
     * @return Path to the uploaded image
     */
    public static String uploadImage(String itemId, InputStream inputStream, String extension) {
        try {
            // Ensure images directory exists
            ensureImagesDirectoryExists();
            
            // Create target file
            String targetFileName = itemId.toLowerCase() + extension;
            String targetPath = "src/main/resources/images/menu/" + targetFileName;
            
            // Copy file
            Path target = Paths.get(targetPath);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            
            // Cache and return path
            String imagePath = "/images/menu/" + targetFileName;
            imageCache.put(itemId, imagePath);
            
            logger.info("Uploaded image for " + itemId + " to " + targetPath);
            return imagePath;
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to upload image for " + itemId, ex);
            return null;
        }
    }
}
