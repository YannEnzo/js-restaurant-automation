/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Waiter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import Controllers.UserAwareController;
import DAO.RestaurantService;
import DAO.TableStatusObserver;
import Model.Order;
import Model.RestaurantTable;
import Model.User;

/**
 * Controller for waiter's table view
 */
public class WaiterTableController implements Initializable, UserAwareController, TableStatusObserver {
    private static final Logger logger = Logger.getLogger(WaiterTableController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    
    @FXML private Label waiterNameLabel;
    
    // Table button references
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
    
    @FXML private Button editTableStatusButton;
    @FXML private Button accountButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Register for table status updates
        service.addTableStatusObserver(this);
        
        // Initialize UI components
        logger.info("Initializing WaiterTableController");
        
        // Initial refresh of tables will happen when user is set
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update the UI with the current user's information
        if (waiterNameLabel != null && user != null) {
            waiterNameLabel.setText(user.getFullName() + " | " + user.getRole());
            logger.info("Set current user to: " + user.getFullName());
        }
        
        // Refresh tables to show assignments
        refreshTables();
    }
    
    /**
     * Refresh the tables display to show current status
     */
    private void refreshTables() {
        if (currentUser == null) {
            logger.warning("Cannot refresh tables: currentUser is null");
            return;
        }
        
        try {
            // Get all tables
            List<RestaurantTable> tables = service.getAllTables();
            logger.info("Refreshing tables display with " + tables.size() + " tables");
            
            // Update the UI for each table
            updateTableButton(tableA1, findTableByNumber(tables, "A1"));
            updateTableButton(tableA2, findTableByNumber(tables, "A2"));
            updateTableButton(tableA3, findTableByNumber(tables, "A3"));
            updateTableButton(tableA4, findTableByNumber(tables, "A4"));
            updateTableButton(tableA5, findTableByNumber(tables, "A5"));
            updateTableButton(tableA6, findTableByNumber(tables, "A6"));
            updateTableButton(tableB1, findTableByNumber(tables, "B1"));
            updateTableButton(tableB2, findTableByNumber(tables, "B2"));
            updateTableButton(tableB3, findTableByNumber(tables, "B3"));
            updateTableButton(tableB4, findTableByNumber(tables, "B4"));
            updateTableButton(tableB5, findTableByNumber(tables, "B5"));
            updateTableButton(tableB6, findTableByNumber(tables, "B6"));
            updateTableButton(tableC5, findTableByNumber(tables, "C5"));
            updateTableButton(tableC6, findTableByNumber(tables, "C6"));
            updateTableButton(tableD5, findTableByNumber(tables, "D5"));
            updateTableButton(tableD6, findTableByNumber(tables, "D6"));
            updateTableButton(tableE1, findTableByNumber(tables, "E1"));
            updateTableButton(tableE2, findTableByNumber(tables, "E2"));
            updateTableButton(tableE3, findTableByNumber(tables, "E3"));
            updateTableButton(tableE4, findTableByNumber(tables, "E4"));
            updateTableButton(tableE5, findTableByNumber(tables, "E5"));
            updateTableButton(tableE6, findTableByNumber(tables, "E6"));
            updateTableButton(tableF1, findTableByNumber(tables, "F1"));
            updateTableButton(tableF2, findTableByNumber(tables, "F2"));
            updateTableButton(tableF3, findTableByNumber(tables, "F3"));
            updateTableButton(tableF4, findTableByNumber(tables, "F4"));
            updateTableButton(tableF5, findTableByNumber(tables, "F5"));
            updateTableButton(tableF6, findTableByNumber(tables, "F6"));
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error refreshing tables", ex);
            showAlert("Error", "Could not refresh tables: " + ex.getMessage());
        }
    }
    
    /**
     * Find a table by its table number
     */
    private RestaurantTable findTableByNumber(List<RestaurantTable> tables, String tableNumber) {
        for (RestaurantTable table : tables) {
            if (table.getTableNumber().equals(tableNumber)) {
                return table;
            }
        }
        logger.warning("Could not find table with number: " + tableNumber);
        return null;
    }
    
    /**
     * Update a table button's appearance based on its status
     */
    private void updateTableButton(Button button, RestaurantTable table) {
        if (button == null) {
            logger.warning("Button is null in updateTableButton");
            return;
        }
        
        if (table == null) {
            // If table not found in database, show as available by default
            button.setStyle("-fx-background-color: #00d26a;");
            return;
        }
        
        // Set the button color based on table status
        switch (table.getStatus()) {
            case "AVAILABLE":
                button.setStyle("-fx-background-color: #00d26a;"); // Green
                break;
            case "OCCUPIED":
                button.setStyle("-fx-background-color: #ffb02e;"); // Yellow
                break;
            case "DIRTY":
                button.setStyle("-fx-background-color: #f8312f;"); // Red
                break;
            default:
                button.setStyle("-fx-background-color: #00d26a;"); // Default to green
                break;
        }
        
        // Check if table is assigned to another waiter
        if (currentUser != null && 
            table.getAssignedWaiterId() != null && 
            !table.getAssignedWaiterId().equals(currentUser.getUserId()) &&
            !table.getStatus().equals("AVAILABLE")) {
            // Make it appear grey/faded for tables assigned to other waiters
            button.setStyle(button.getStyle() + "; -fx-opacity: 0.7;");
        } else if (currentUser != null && 
                  table.getAssignedWaiterId() != null && 
                  table.getAssignedWaiterId().equals(currentUser.getUserId())) {
            // Make assigned tables to current waiter appear bolder
            button.setStyle(button.getStyle() + "; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Handle table button clicks
     */
    private void handleTableClick(String tableNumber) {
        logger.info("Table clicked: " + tableNumber);
        
        try {
            // Find the table
            List<RestaurantTable> tables = service.getAllTables();
            RestaurantTable table = findTableByNumber(tables, tableNumber);
            
            if (table == null) {
                showAlert("Table Not Found", "Could not find table " + tableNumber);
                return;
            }
            
            // Check if table is assigned to another waiter
            if (table.getAssignedWaiterId() != null && 
                !table.getAssignedWaiterId().equals(currentUser.getUserId()) &&
                !table.getStatus().equals("AVAILABLE")) {
                
                showAlert("Table Assigned", "This table is assigned to another waiter.");
                return;
            }
            
            // If table is available, assign it to this waiter and change status to occupied
            if (table.getStatus().equals("AVAILABLE")) {
                logger.info("Assigning available table to current waiter...");
                boolean success = service.updateTableStatus(table.getTableId(), "OCCUPIED", currentUser.getUserId());
                if (!success) {
                    showAlert("Error", "Could not update table status");
                    return;
                }
                
                // Refresh tables to update UI
                refreshTables();
            }
            
            // Open table view screen
            openTableOrderScreen(table);
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error handling table click", ex);
            showAlert("Error", "Could not process table click: " + ex.getMessage());
        }
    }
    
    /**
     * Open the order screen for a table
     */
    private void openTableOrderScreen(RestaurantTable table) {
        try {
            logger.info("Opening order screen for table: " + table.getTableNumber());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/waiterViews/waiterOrder.fxml"));
            Parent root = loader.load();
            
            // Pass data to the controller
            WaiterOrderController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setTable(table);
            
            // Create new scene
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Table " + table.getTableNumber() + " - Order");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error opening order screen", ex);
            showAlert("Error", "Could not open order screen: " + ex.getMessage());
        }
    }
    
    // Table button action handlers
    @FXML
    void ontableA1(ActionEvent event) {
        handleTableClick("A1");
    }
    
    @FXML
    void ontableA2(ActionEvent event) {
        handleTableClick("A2");
    }
    
    @FXML
    void ontableA3(ActionEvent event) {
        handleTableClick("A3");
    }
    
    @FXML
    void ontableA4(ActionEvent event) {
        handleTableClick("A4");
    }
    
    @FXML
    void ontableA5(ActionEvent event) {
        handleTableClick("A5");
    }
    
    @FXML
    void ontableA6(ActionEvent event) {
        handleTableClick("A6");
    }
    
    @FXML
    void ontableB1(ActionEvent event) {
        handleTableClick("B1");
    }
    
    @FXML
    void ontableB2(ActionEvent event) {
        handleTableClick("B2");
    }
    
    @FXML
    void ontableB3(ActionEvent event) {
        handleTableClick("B3");
    }
    
    @FXML
    void ontableB4(ActionEvent event) {
        handleTableClick("B4");
    }
    
    @FXML
    void ontableB5(ActionEvent event) {
        handleTableClick("B5");
    }
    
    @FXML
    void ontableB6(ActionEvent event) {
        handleTableClick("B6");
    }
    
    @FXML
    void onontableC5(ActionEvent event) {
        handleTableClick("C5");
    }
    
    @FXML
    void ontableC6(ActionEvent event) {
        handleTableClick("C6");
    }
    
    @FXML
    void ontableD5(ActionEvent event) {
        handleTableClick("D5");
    }
    
    @FXML
    void ontableD6(ActionEvent event) {
        handleTableClick("D6");
    }
    
    @FXML
    void ontableE1(ActionEvent event) {
        handleTableClick("E1");
    }
    
    @FXML
    void ontableE2(ActionEvent event) {
        handleTableClick("E2");
    }
    
    @FXML
    void ontableE3(ActionEvent event) {
        handleTableClick("E3");
    }
    
    @FXML
    void ontableE4(ActionEvent event) {
        handleTableClick("E4");
    }
    
    @FXML
    void ontableE5(ActionEvent event) {
        handleTableClick("E5");
    }
    
    @FXML
    void ontableE6(ActionEvent event) {
        handleTableClick("E6");
    }
    
    @FXML
    void ontableF1(ActionEvent event) {
        handleTableClick("F1");
    }
    
    @FXML
    void ontableF2(ActionEvent event) {
        handleTableClick("F2");
    }
    
    @FXML
    void ontableF3(ActionEvent event) {
        handleTableClick("F3");
    }
    
    @FXML
    void ontableF4(ActionEvent event) {
        handleTableClick("F4");
    }
    
    @FXML
    void ontableF5(ActionEvent event) {
        handleTableClick("F5");
    }
    
    @FXML
    void ontableF6(ActionEvent event) {
        handleTableClick("F6");
    }
    
    /**
     * Handle edit table status button click
     */
    @FXML
    void onEditTableStatus(ActionEvent event) {
        try {
            logger.info("Opening Edit Table Status screen");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/waiterViews/waiterEditTableStatus.fxml"));
            Parent root = loader.load();
            
            WaiterEditTableStatusController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Create new scene
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Edit Table Status");
            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.showAndWait();
            
            // Refresh tables after the dialog is closed
            refreshTables();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error opening edit table status screen", ex);
            showAlert("Error", "Could not open edit table status screen: " + ex.getMessage());
        }
    }
    
    /**
     * Handle account button click
     */
    @FXML
    void onAccount(ActionEvent event) {
        try {
            logger.info("Opening Account screen");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/waiterViews/waiterAccount.fxml"));
            Parent root = loader.load();
            
            WaiterAccountController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Switch to account view
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error opening account screen", ex);
            showAlert("Error", "Could not open account screen: " + ex.getMessage());
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
    
    /**
     * TableStatusObserver implementation
     */
    @Override
    public void onTableStatusChanged(String tableId, String newStatus) {
        // Update the UI on the JavaFX thread
        Platform.runLater(() -> {
            logger.info("Table status changed: " + tableId + " -> " + newStatus);
            refreshTables();
            
            // If it's a Ready status notification and the table is assigned to this waiter,
            // show a notification
            try {
                RestaurantTable table = null;
                List<RestaurantTable> tables = service.getAllTables();
                for (RestaurantTable t : tables) {
                    if (t.getTableId().equals(tableId)) {
                        table = t;
                        break;
                    }
                }
                
                if (table != null && 
                    newStatus.equals("READY") && 
                    table.getAssignedWaiterId() != null &&
                    table.getAssignedWaiterId().equals(currentUser.getUserId())) {
                    
                    showAlert("Order Ready", "Order for Table " + table.getTableNumber() + " is ready!");
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error checking table status change", ex);
            }
        });
    }
}