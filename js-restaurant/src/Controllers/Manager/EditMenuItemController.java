package Controllers.Manager;

import Model.MenuItem;
import Model.MenuItemAddon;
import DAO.MenuItemDAO;
import DAO.RestaurantService;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EditMenuItemController implements Initializable {
    private static final Logger logger = Logger.getLogger(EditMenuItemController.class.getName());
    private MenuItem menuItem;
    private List<MenuItemAddon> addons = new ArrayList<>();
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TextField addonNameField;
    
    @FXML
    private TextField addonPriceField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Button backButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up category options
        ObservableList<String> categories = FXCollections.observableArrayList(
            "Appetizers", "Salads", "Entrees", "Sandwiches", "Sides", "Burgers", "Beverages"
        );
        categoryComboBox.setItems(categories);
        
        // Add input validation
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                priceField.setText(oldValue);
            }
        });
        
        addonPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                addonPriceField.setText(oldValue);
            }
        });
        
        logger.info("Edit Menu Item controller initialized");
    }
    
    /**
     * Set the menu item to edit
     * @param item MenuItem to edit
     */
    public void setMenuItem(MenuItem item) {
        this.menuItem = item;
        
        // Populate fields with item data
        nameField.setText(item.getName());
        categoryComboBox.setValue(item.getCategoryName());
        priceField.setText(String.format("%.2f", item.getPrice()));
        descriptionArea.setText(item.getDescription());
        
        // Load addons
        this.addons = new ArrayList<>(item.getAddons());
        
        logger.info("Loaded menu item: " + item.getName());
    }
    
    /**
     * Handle save button click
     * @param event ActionEvent
     */
    @FXML
    void onSaveChanges(ActionEvent event) {
        // Validate inputs
        if (nameField.getText().isEmpty() || categoryComboBox.getValue() == null || priceField.getText().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Required Fields Empty", 
                    "Please fill in all required fields marked with *");
            return;
        }
        
        try {
            // Update menu item with form values
            menuItem.setName(nameField.getText());
            menuItem.setCategoryName(categoryComboBox.getValue());
            menuItem.setCategoryId(getCategoryId(categoryComboBox.getValue()));
            menuItem.setPrice(Double.parseDouble(priceField.getText()));
            menuItem.setDescription(descriptionArea.getText());
            
            // Add addon if provided
            if (!addonNameField.getText().isEmpty() && !addonPriceField.getText().isEmpty()) {
                MenuItemAddon addon = new MenuItemAddon();
                addon.setItemId(menuItem.getItemId());
                addon.setName(addonNameField.getText());
                addon.setPrice(Double.parseDouble(addonPriceField.getText()));
                menuItem.getAddons().add(addon);
            }
            
            // Save to database
            boolean success = MenuItemDAO.getInstance().update(menuItem);
            
            // In AddMenuItemController's onConfirm() method, after saving:
if (success) {
    logger.info("Menu item added: " + menuItem.getItemId());
    showAlert(AlertType.INFORMATION, "Success", "Menu Item Added", 
            "The menu item was successfully added.");
    
    // Force refresh of menu cache
    RestaurantService.getInstance().refreshMenuCache();
    
    // Close the form
    closeForm();
} else {
                logger.warning("Failed to update menu item: " + menuItem.getItemId());
                showAlert(AlertType.ERROR, "Update Error", "Could not update menu item", 
                        "There was an error updating the menu item. Please try again.");
            }
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Invalid number format", ex);
            showAlert(AlertType.ERROR, "Input Error", "Invalid Number Format", 
                    "Please enter valid numbers for price fields.");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error updating menu item", ex);
            showAlert(AlertType.ERROR, "Database Error", "Could not update menu item", 
                    "There was a database error updating the menu item: " + ex.getMessage());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error updating menu item", ex);
            showAlert(AlertType.ERROR, "Error", "Unexpected Error", 
                    "An unexpected error occurred: " + ex.getMessage());
        }
    }
    
    /**
     * Handle reset button click
     * @param event ActionEvent
     */
    @FXML
    void onReset(ActionEvent event) {
        // Reset form to original values
        if (menuItem != null) {
            nameField.setText(menuItem.getName());
            categoryComboBox.setValue(menuItem.getCategoryName());
            priceField.setText(String.format("%.2f", menuItem.getPrice()));
            descriptionArea.setText(menuItem.getDescription());
            addonNameField.clear();
            addonPriceField.clear();
        }
    }
    
    /**
     * Handle back button click
     * @param event ActionEvent
     */
    @FXML
    void onBack(ActionEvent event) {
        closeForm();
    }
    
    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Get category ID from name
     * @param categoryName Category name
     * @return Category ID
     */
    private int getCategoryId(String categoryName) {
        switch (categoryName) {
            case "Appetizers": return 1;
            case "Salads": return 2;
            case "Entrees": return 3;
            case "Sandwiches": return 4;
            case "Sides": return 5;
            case "Burgers": return 6;
            case "Beverages": return 7;
            default: return 1;
        }
    }
    
    /**
     * Show an alert dialog
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header text
     * @param content Alert content text
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}