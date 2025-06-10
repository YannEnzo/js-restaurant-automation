package Controllers.Waiter;

import Controllers.UserAwareController;
import DAO.RestaurantService;
import Model.RestaurantTable;
import Model.User;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controller for waiter's edit table status view
 */
public class WaiterEditTableStatusController implements Initializable, UserAwareController {
    private static final Logger logger = Logger.getLogger(WaiterEditTableStatusController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    private List<RestaurantTable> allTables = new ArrayList<>();
    
    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<String> tableComboBox;
    @FXML private Button setOpenButton;
    @FXML private Button setOccupiedButton;
    @FXML private Button setDirtyButton;
    @FXML private Button returnButton;
    
    // Color definitions
    private static final String COLOR_OPEN = "#00d26a"; // Green
    private static final String COLOR_OCCUPIED = "#ffb02e"; // Orange/Yellow
    private static final String COLOR_DIRTY = "#f8312f"; // Red
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing WaiterEditTableStatusController");
        
        // Setup ComboBox
        setupTableComboBox();
        
        // Add listener for table selection
        tableComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                logger.info("Table selected: " + newVal);
                updateStatusForTable(newVal);
            }
        });
    }
    
    /**
     * Setup the table combo box with custom cell factory for better display
     */
    private void setupTableComboBox() {
        // Set cell factory for better display in ComboBox
        Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);
                            
                            // Set background color based on table status
                            RestaurantTable table = findTableByNumber(item);
                            if (table != null) {
                                switch (table.getStatus()) {
                                    case "AVAILABLE":
                                        setStyle("-fx-background-color: " + COLOR_OPEN + "; -fx-text-fill: white;");
                                        break;
                                    case "OCCUPIED":
                                        setStyle("-fx-background-color: " + COLOR_OCCUPIED + "; -fx-text-fill: white;");
                                        break;
                                    case "DIRTY":
                                        setStyle("-fx-background-color: " + COLOR_DIRTY + "; -fx-text-fill: white;");
                                        break;
                                    default:
                                        setStyle("");
                                        break;
                                }
                                
                                // If table assigned to another waiter, show faded
                                if (currentUser != null && 
                                    table.getAssignedWaiterId() != null && 
                                    !table.getAssignedWaiterId().equals(currentUser.getUserId()) &&
                                    !table.getStatus().equals("AVAILABLE")) {
                                    setStyle(getStyle() + "; -fx-opacity: 0.7;");
                                }
                            }
                        }
                    }
                };
            }
        };
        
        tableComboBox.setCellFactory(cellFactory);
        
        // Set button factory for selected item display
        tableComboBox.setButtonCell(cellFactory.call(null));
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        logger.info("Current user set: " + user.getUsername());
        
        // Load tables after user is set - wait briefly for JavaFX to complete initialization
        Platform.runLater(() -> {
            loadTables();
        });
    }
    
    /**
     * Load tables for the dropdown
     */
    private void loadTables() {
        try {
            // Load all tables
            allTables = service.getAllTables();
            logger.info("Loaded " + allTables.size() + " total tables");
            
            // Create an observable list for the ComboBox
            ObservableList<String> tableNumbers = FXCollections.observableArrayList();
            
            // Add table numbers to the list
            for (RestaurantTable table : allTables) {
                tableNumbers.add(table.getTableNumber());
                logger.info("Added table " + table.getTableNumber() + " with status " + table.getStatus());
            }
            
            if (tableNumbers.isEmpty()) {
                logger.warning("No tables found in the database");
                currentStatusLabel.setText("No tables found");
                return;
            }
            
            // Set the items in the ComboBox
            tableComboBox.setItems(tableNumbers);
            
            // Select first table
            tableComboBox.getSelectionModel().select(0);
            updateStatusForTable(tableComboBox.getValue());
            logger.info("Selected first table: " + tableComboBox.getValue());
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading tables: " + ex.getMessage(), ex);
            showAlert("Error", "Could not load tables: " + ex.getMessage());
        }
    }
    
    /**
     * Update display with current status for selected table
     * @param tableNumber The selected table number
     */
    private void updateStatusForTable(String tableNumber) {
        try {
            // Find table in the list
            RestaurantTable table = findTableByNumber(tableNumber);
            
            if (table != null) {
                // Convert database status to display status and set colors
                String displayStatus;
                switch (table.getStatus()) {
                    case "AVAILABLE":
                        displayStatus = "Open";
                        currentStatusLabel.setTextFill(Color.web(COLOR_OPEN));
                        break;
                    case "OCCUPIED":
                        displayStatus = "Occupied";
                        currentStatusLabel.setTextFill(Color.web(COLOR_OCCUPIED));
                        break;
                    case "DIRTY":
                        displayStatus = "Dirty";
                        currentStatusLabel.setTextFill(Color.web(COLOR_DIRTY));
                        break;
                    default:
                        displayStatus = table.getStatus();
                        currentStatusLabel.setTextFill(Color.BLACK);
                        break;
                }
                
                // Update label
                currentStatusLabel.setText(displayStatus);
                logger.info("Updated status display for table " + tableNumber + ": " + displayStatus);
                
                // Disable action buttons based on valid transitions for waiters
                // Waiters can only:
                // - Change AVAILABLE to OCCUPIED
                // - Change OCCUPIED to DIRTY
                setOpenButton.setDisable(true); // Waiters can't set tables to Open (that's for busboys)
                
                if (table.getStatus().equals("AVAILABLE")) {
                    setOccupiedButton.setDisable(false);
                    setDirtyButton.setDisable(true);
                } else if (table.getStatus().equals("OCCUPIED")) {
                    setOccupiedButton.setDisable(true);
                    setDirtyButton.setDisable(false);
                } else {
                    setOccupiedButton.setDisable(true);
                    setDirtyButton.setDisable(true);
                }
                
                // Also disable buttons if table is assigned to another waiter
                if (currentUser != null && 
                    table.getAssignedWaiterId() != null && 
                    !table.getAssignedWaiterId().equals(currentUser.getUserId()) &&
                    !table.getStatus().equals("AVAILABLE")) {
                    
                    setOpenButton.setDisable(true);
                    setOccupiedButton.setDisable(true);
                    setDirtyButton.setDisable(true);
                }
            } else {
                logger.warning("Could not find table with number: " + tableNumber);
                currentStatusLabel.setText("Unknown");
                currentStatusLabel.setTextFill(Color.BLACK);
                setOpenButton.setDisable(true);
                setOccupiedButton.setDisable(true);
                setDirtyButton.setDisable(true);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating table status", ex);
            showAlert("Error", "Could not update table status: " + ex.getMessage());
        }
    }
    
    /**
     * Find a table by its table number
     * @param tableNumber The table number to find
     * @return The table object or null if not found
     */
    private RestaurantTable findTableByNumber(String tableNumber) {
        // First check in our cached list
        for (RestaurantTable table : allTables) {
            if (table.getTableNumber().equals(tableNumber)) {
                return table;
            }
        }
        
        // If not found in our cached list, try to get fresh data
        try {
            List<RestaurantTable> freshTables = service.getAllTables();
            // Update our cached list
            allTables = freshTables;
            
            for (RestaurantTable table : freshTables) {
                if (table.getTableNumber().equals(tableNumber)) {
                    return table;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error finding table in fresh data", ex);
        }
        
        return null;
    }
    
    /**
     * Refresh the ComboBox to show updated table colors/status
     */
    private void refreshTableComboBox() {
        String selectedTable = tableComboBox.getValue();
        ObservableList<String> items = tableComboBox.getItems();
        tableComboBox.setItems(null);
        tableComboBox.setItems(items);
        tableComboBox.getSelectionModel().select(selectedTable);
    }
    
    /**
     * Handle return button click
     */
    @FXML
    void onReturn(ActionEvent event) {
        logger.info("Return button clicked");
        // Close this window
        Stage stage = (Stage) returnButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handle set open button click
     */
    @FXML
    void onSetOpen(ActionEvent event) {
        logger.info("Set Open button clicked");
        updateTableStatus("AVAILABLE");
    }
    
    /**
     * Handle set occupied button click
     */
    @FXML
    void onSetOccupied(ActionEvent event) {
        logger.info("Set Occupied button clicked");
        updateTableStatus("OCCUPIED");
    }
    
    /**
     * Handle set dirty button click
     */
    @FXML
    void onSetDirty(ActionEvent event) {
        logger.info("Set Dirty button clicked");
        updateTableStatus("DIRTY");
    }
    
    /**
     * Update the status of the selected table
     * @param newStatus The new status to set (database format)
     */
    private void updateTableStatus(String newStatus) {
        String tableNumber = tableComboBox.getValue();
        
        if (tableNumber == null || tableNumber.isEmpty()) {
            showAlert("Error", "Please select a table");
            return;
        }
        
        logger.info("Updating table " + tableNumber + " to status " + newStatus);
        
        try {
            // Find the table
            RestaurantTable table = findTableByNumber(tableNumber);
            
            if (table == null) {
                showAlert("Error", "Table not found: " + tableNumber);
                return;
            }
            
            // Skip if status is already the same
            if (table.getStatus().equals(newStatus)) {
                logger.info("Table status is already " + newStatus + " - skipping update");
                return;
            }
            
            // Check if this is a valid status change for a waiter
            // Waiters can only:
            // - Change AVAILABLE to OCCUPIED
            // - Change OCCUPIED to DIRTY
            boolean isValidChange = false;
            
            if (table.getStatus().equals("AVAILABLE") && newStatus.equals("OCCUPIED")) {
                isValidChange = true;
            } else if (table.getStatus().equals("OCCUPIED") && newStatus.equals("DIRTY")) {
                isValidChange = true;
            }
            
            if (!isValidChange) {
                logger.warning("Invalid status change attempted: " + table.getStatus() + " -> " + newStatus);
                showAlert("Invalid Status Change", 
                          "Waiters can only change tables from Available to Occupied, or from Occupied to Dirty");
                return;
            }
            
            // Update the status in the database
            boolean success = service.updateTableStatus(table.getTableId(), newStatus, currentUser.getUserId());
            
            if (success) {
                logger.info("Table status updated successfully");
                // Update the in-memory table status
                table.setStatus(newStatus);
                // Update the display
                updateStatusForTable(tableNumber);
                
                // Update the ComboBox display to show the new color
                refreshTableComboBox();
                
                showAlert("Success", "Table status updated successfully");
                
                // Refresh the table list to get latest data
                Platform.runLater(() -> {
                    allTables = service.getAllTables();
                });
            } else {
                logger.warning("Failed to update table status");
                showAlert("Error", "Failed to update table status");
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating table status", ex);
            showAlert("Error", "Could not update table status: " + ex.getMessage());
        }
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}