package Controllers.Manager;

import Model.RestaurantTable;
import Model.User;
import DAO.RestaurantService;
import DAO.TableDAO;
import DAO.TableStatusObserver;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class manager_floorplanController implements Initializable, TableStatusObserver {
    private static final Logger logger = Logger.getLogger(manager_floorplanController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private Map<String, Button> tableButtons = new HashMap<>();
    private User currentUser;
    
    @FXML
    private Label nameandrole;
    
    // Table buttons
    @FXML private Button tableA1;
    @FXML private Button tableA2;
    @FXML private Button tableA3;
    @FXML private Button tableA4;
    @FXML private Button tableA5;
    @FXML private Button tableA6;
    @FXML private Button tableB1;
    @FXML private Button tableB2;
    @FXML private Button tableB3;
    @FXML private Button tableB4;
    @FXML private Button tableB5;
    @FXML private Button tableB6;
    @FXML private Button tableC5;
    @FXML private Button tableC6;
    @FXML private Button tableD5;
    @FXML private Button tableD6;
    @FXML private Button tableE1;
    @FXML private Button tableE2;
    @FXML private Button tableE3;
    @FXML private Button tableE4;
    @FXML private Button tableE5;
    @FXML private Button tableE6;
    @FXML private Button tableF1;
    @FXML private Button tableF2;
    @FXML private Button tableF3;
    @FXML private Button tableF4;
    @FXML private Button tableF5;
    @FXML private Button tableF6;
    
    @FXML
    private Button editStatusBtn;
    
    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Map table IDs to buttons for easier access
        initializeTableButtonMap();
        
        // Register for table status updates
        service.addTableStatusObserver(this);
        
        // Load initial table statuses
        loadTableData();
        
        logger.info("Floor plan controller initialized");
    }
    
    /**
     * Set the current user for this controller
     * @param user Current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update UI with user information
        if (nameandrole != null && user != null) {
            nameandrole.setText(user.getFullName() + " | " + user.getRole());
        }
    }
    
    /**
     * Create a map of table IDs to their respective buttons
     */
    private void initializeTableButtonMap() {
        tableButtons.put("A1", tableA1);
        tableButtons.put("A2", tableA2);
        tableButtons.put("A3", tableA3);
        tableButtons.put("A4", tableA4);
        tableButtons.put("A5", tableA5);
        tableButtons.put("A6", tableA6);
        tableButtons.put("B1", tableB1);
        tableButtons.put("B2", tableB2);
        tableButtons.put("B3", tableB3);
        tableButtons.put("B4", tableB4);
        tableButtons.put("B5", tableB5);
        tableButtons.put("B6", tableB6);
        tableButtons.put("C5", tableC5);
        tableButtons.put("C6", tableC6);
        tableButtons.put("D5", tableD5);
        tableButtons.put("D6", tableD6);
        tableButtons.put("E1", tableE1);
        tableButtons.put("E2", tableE2);
        tableButtons.put("E3", tableE3);
        tableButtons.put("E4", tableE4);
        tableButtons.put("E5", tableE5);
        tableButtons.put("E6", tableE6);
        tableButtons.put("F1", tableF1);
        tableButtons.put("F2", tableF2);
        tableButtons.put("F3", tableF3);
        tableButtons.put("F4", tableF4);
        tableButtons.put("F5", tableF5);
        tableButtons.put("F6", tableF6);
    }
    
    /**
     * Load table data from the database and update the UI
     */
    private void loadTableData() {
        try {
            // Get all tables
            List<RestaurantTable> tables = service.getAllTables();
            
            // Update UI for each table
            for (RestaurantTable table : tables) {
                updateTableButton(table);
            }
            
            logger.info("Table data loaded successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading table data", ex);
            showAlert(AlertType.ERROR, "Error", "Could not load table data", 
                    "An error occurred while loading table data: " + ex.getMessage());
        }
    }
    
    /**
     * Update a button's appearance based on table status
     * @param table The table to update
     */
    private void updateTableButton(RestaurantTable table) {
        // Get the table number (e.g., "A1")
        String tableNumber = table.getTableNumber();
        
        // Find the corresponding button
        Button tableButton = tableButtons.get(tableNumber);
        
        if (tableButton != null) {
            Platform.runLater(() -> {
                // Set button style based on status
                switch (table.getStatus()) {
                    case "AVAILABLE":
                        tableButton.setStyle("-fx-background-color: #00E676;"); // Green
                        break;
                    case "OCCUPIED":
                        tableButton.setStyle("-fx-background-color: #FFA726;"); // Yellow/Orange
                        break;
                    case "DIRTY":
                        tableButton.setStyle("-fx-background-color: #FF5252;"); // Red
                        break;
                    default:
                        tableButton.setStyle("-fx-background-color: #BDBDBD;"); // Gray for unknown status
                        break;
                }
                
                // Set button text to table number
                tableButton.setText(tableNumber);
            });
        }
    }
    
    /**
     * Handle table click events
     * @param tableNumber The table number (e.g., "A1")
     */
    private void handleTableClick(String tableNumber) {
        try {
            // Get the table from the database
            RestaurantTable table = TableDAO.getInstance().getByTableNumber(tableNumber);
            
            if (table != null) {
                // Show table information
                showTableInfo(table);
            } else {
                logger.warning("Table not found: " + tableNumber);
                showAlert(AlertType.WARNING, "Table Not Found", 
                        "Table " + tableNumber + " not found in the database", 
                        "The selected table could not be found.");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving table: " + tableNumber, ex);
            showAlert(AlertType.ERROR, "Database Error", 
                    "Could not retrieve table information", 
                    "An error occurred while retrieving table information: " + ex.getMessage());
        }
    }
    
    /**
     * Show table information in a dialog
     * @param table The table to show information for
     */
    private void showTableInfo(RestaurantTable table) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Table Information");
        alert.setHeaderText("Table " + table.getTableNumber());
        
        String statusText = "Status: " + (table.getStatus().equals("AVAILABLE") ? "Available" : 
                            table.getStatus().equals("OCCUPIED") ? "Occupied" : "Dirty");
        
        String waiterText = "";
        if (table.getAssignedWaiterId() != null && !table.getAssignedWaiterId().isEmpty()) {
            try {
                User waiter = service.getUserById(table.getAssignedWaiterId());
                if (waiter != null) {
                    waiterText = "\nAssigned Waiter: " + waiter.getFullName();
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error retrieving waiter information", ex);
            }
        }
        
        String content = statusText + 
                "\nCapacity: " + table.getCapacity() + " customers" +
                waiterText;
        
        alert.setContentText(content);
        
        // Add buttons for changing status
        ButtonType changeStatusButton = new ButtonType("Change Status");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(changeStatusButton, closeButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == changeStatusButton) {
            showChangeStatusDialog(table);
        }
    }
    
    /**
     * Show dialog for changing table status
     * @param table The table to change status for
     */
    private void showChangeStatusDialog(RestaurantTable table) {
        List<String> choices = Arrays.asList("AVAILABLE", "OCCUPIED", "DIRTY");

        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(table.getStatus(), choices);
        dialog.setTitle("Change Table Status");
        dialog.setHeaderText("Change status for Table " + table.getTableNumber());
        dialog.setContentText("Select new status:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(newStatus -> {
            try {
                // Only update if status has changed
                if (!newStatus.equals(table.getStatus())) {
                    boolean success = service.updateTableStatus(
                            table.getTableId(), newStatus, currentUser.getUserId());
                    
                    if (success) {
                        logger.info("Table status updated: " + table.getTableNumber() + 
                                " to " + newStatus);
                    } else {
                        logger.warning("Failed to update table status: " + table.getTableNumber());
                        showAlert(AlertType.ERROR, "Update Error", 
                                "Could not update table status", 
                                "The table status could not be updated.");
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error updating table status", ex);
                showAlert(AlertType.ERROR, "Database Error", 
                        "Could not update table status", 
                        "An error occurred while updating table status: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Handle edit table status button click
     * @param event Action event
     */
    @FXML
    void onEditTableStatus(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/Views/managerViews/editTableStatus.fxml");
            
            if (fxmlUrl == null) {
                logger.severe("Could not locate FXML file for edit table status");
                showAlert(AlertType.ERROR, "Resource Error", "Could not find form resource", 
                        "The application could not locate the required form.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Pass current user to the controller if needed
            Object controller = loader.getController();
            if (controller instanceof TableStatusObserver) {
                // Register the controller for table status updates
                service.addTableStatusObserver((TableStatusObserver) controller);
            }
            
            Stage stage = new Stage();
            stage.setTitle("Edit Table Status");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Show the form and wait for it to close
            stage.showAndWait();
            
            // Refresh table data
            loadTableData();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading edit table status form", ex);
            showAlert(AlertType.ERROR, "Form Error", "Could not open edit table status form", 
                    "There was an error opening the form: " + ex.getMessage());
        }
    }
    
    /**
     * Handle account button click
     * @param event Action event
     */
    @FXML
    void onAccount(ActionEvent event) {
        // Navigate to account view or show account dialog
        // This will be handled by the main controller's navigation system
    }
    
    // Table button click event handlers
    @FXML void ontableA1(ActionEvent event) { handleTableClick("A1"); }
    @FXML void ontableA2(ActionEvent event) { handleTableClick("A2"); }
    @FXML void ontableA3(ActionEvent event) { handleTableClick("A3"); }
    @FXML void ontableA4(ActionEvent event) { handleTableClick("A4"); }
    @FXML void ontableA5(ActionEvent event) { handleTableClick("A5"); }
    @FXML void ontableA6(ActionEvent event) { handleTableClick("A6"); }
    @FXML void ontableB1(ActionEvent event) { handleTableClick("B1"); }
    @FXML void ontableB2(ActionEvent event) { handleTableClick("B2"); }
    @FXML void ontableB3(ActionEvent event) { handleTableClick("B3"); }
    @FXML void ontableB4(ActionEvent event) { handleTableClick("B4"); }
    @FXML void ontableB5(ActionEvent event) { handleTableClick("B5"); }
    @FXML void ontableB6(ActionEvent event) { handleTableClick("B6"); }
    @FXML void ontableC5(ActionEvent event) { handleTableClick("C5"); }
    @FXML void ontableC6(ActionEvent event) { handleTableClick("C6"); }
    @FXML void ontableD5(ActionEvent event) { handleTableClick("D5"); }
    @FXML void ontableD6(ActionEvent event) { handleTableClick("D6"); }
    @FXML void ontableE1(ActionEvent event) { handleTableClick("E1"); }
    @FXML void ontableE2(ActionEvent event) { handleTableClick("E2"); }
    @FXML void ontableE3(ActionEvent event) { handleTableClick("E3"); }
    @FXML void ontableE4(ActionEvent event) { handleTableClick("E4"); }
    @FXML void ontableE5(ActionEvent event) { handleTableClick("E5"); }
    @FXML void ontableE6(ActionEvent event) { handleTableClick("E6"); }
    @FXML void ontableF1(ActionEvent event) { handleTableClick("F1"); }
    @FXML void ontableF2(ActionEvent event) { handleTableClick("F2"); }
    @FXML void ontableF3(ActionEvent event) { handleTableClick("F3"); }
    @FXML void ontableF4(ActionEvent event) { handleTableClick("F4"); }
    @FXML void ontableF5(ActionEvent event) { handleTableClick("F5"); }
    @FXML void ontableF6(ActionEvent event) { handleTableClick("F6"); }
    
    /**
     * Handle table status change notification from the observer pattern
     */
    @Override
    public void onTableStatusChanged(String tableId, String newStatus) {
        try {
            // Get updated table and refresh UI
            RestaurantTable table = TableDAO.getInstance().getById(tableId);
            if (table != null) {
                updateTableButton(table);
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error retrieving updated table", ex);
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
    
    /**
     * Clean up resources when controller is no longer needed
     */
    public void dispose() {
        // Unregister observer when controller is no longer needed
        service.removeTableStatusObserver(this);
    }
}