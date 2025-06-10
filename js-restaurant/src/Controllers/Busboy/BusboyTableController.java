package Controllers.Busboy;

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
import Model.RestaurantTable;
import Model.User;

/**
 * Controller for busboy's table view
 */
public class BusboyTableController implements Initializable, UserAwareController, TableStatusObserver {
    private static final Logger logger = Logger.getLogger(BusboyTableController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    
    @FXML private Label busboyNameLabel;
    
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
    
    @FXML private Button accountButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Register for table status updates
        service.addTableStatusObserver(this);
        
        // Initialize UI components
        logger.info("Initializing BusboyTableController");
        
        // Initial refresh of tables will happen when user is set
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update the UI with the current user's information
        if (busboyNameLabel != null && user != null) {
            busboyNameLabel.setText(user.getFullName() + " | " + user.getRole());
            logger.info("Set current user to: " + user.getFullName());
        }
        
        // Refresh tables to show status
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
     * @param tables List of tables to search
     * @param tableNumber The table number to find
     * @return The table object or null if not found
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
                button.setStyle("-fx-background-color: #f8312f;"); // Red - Highlight for busboys
                button.setStyle(button.getStyle() + "; -fx-font-weight: bold;");
                break;
            default:
                button.setStyle("-fx-background-color: #00d26a;"); // Default to green
                break;
        }
    }
    
    /**
 * Handle table button clicks - For busboys, this only shows the clean options for dirty tables
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
        
        // For busboys, we're only interested in dirty tables
        if (!table.getStatus().equals("DIRTY")) {
            showAlert("Table Not Dirty", "Table " + tableNumber + " is not dirty and doesn't need cleaning.");
            return;
        }
        
        // Open table status change screen - Use a safer method to open it
        try {
            openTableStatusScreen(table);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error opening table status screen", ex);
            showAlert("Error", "Could not open table status screen: " + ex.getMessage());
        }
        
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error handling table click: " + ex.getMessage(), ex);
        showAlert("Error", "Could not process table click: " + ex.getMessage());
    }
}

/**
 * Open the table status screen for a table
 */

private void openTableStatusScreen(RestaurantTable table) {
    try {
        if (table == null) {
            throw new IllegalStateException("Table is null");
        }
        
        logger.info("Opening status change screen for table: " + table.getTableNumber());
        
        // Make sure we're using the correct path to the FXML file
        String fxmlPath = "/Views/busboyViews/busboyTableStatus.fxml";
        URL fxmlUrl = getClass().getResource(fxmlPath);
        
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find FXML file: " + fxmlPath);
        }
        
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        
        // Make sure the controller was loaded correctly
        BusboyTableStatusController controller = loader.getController();
        if (controller == null) {
            throw new IllegalStateException("Could not get controller from loader");
        }
        
        // Pass data to the controller
        controller.setCurrentUser(currentUser);
        controller.setTable(table);
        
        // Create new scene with explicit dimensions
        Scene scene = new Scene(root, 600, 400);
        
        // Create and configure the stage
        Stage stage = new Stage();
        stage.setTitle("Clean Table " + table.getTableNumber());
        stage.setScene(scene);
        
        // Don't use StageStyle.UTILITY as it might be causing issues
        // stage.initStyle(StageStyle.UTILITY);
        
        // Show the dialog
        stage.showAndWait();
        
        // Refresh tables after dialog is closed
        refreshTables();
        
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error opening table status screen: " + ex.getMessage(), ex);
        showAlert("Error", "Could not open table status screen: " + ex.getMessage());
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
    void ontableC5(ActionEvent event) {
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
     * Handle account button click
     */
    @FXML
    void onAccount(ActionEvent event) {
        try {
            logger.info("Opening Account screen");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/busboyViews/busboyAccount.fxml"));
            Parent root = loader.load();
            
            BusboyAccountController controller = loader.getController();
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
     * This is called when any table's status changes
     */
    @Override
    public void onTableStatusChanged(String tableId, String newStatus) {
        // Update the UI on the JavaFX thread
        Platform.runLater(() -> {
            logger.info("Table status changed: " + tableId + " -> " + newStatus);
            refreshTables();
            
            // If it's a Dirty status notification, show a notification to the busboy
            try {
                if (newStatus.equals("DIRTY")) {
                    RestaurantTable table = null;
                    List<RestaurantTable> tables = service.getAllTables();
                    for (RestaurantTable t : tables) {
                        if (t.getTableId().equals(tableId)) {
                            table = t;
                            break;
                        }
                    }
                    
                    if (table != null) {
                        showAlert("Table Needs Cleaning", "Table " + table.getTableNumber() + " is now dirty and needs cleaning.");
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error checking table status change", ex);
            }
        });
    }
}
