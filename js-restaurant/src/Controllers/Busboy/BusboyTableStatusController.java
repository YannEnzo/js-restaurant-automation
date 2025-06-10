/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Busboy;

import Controllers.UserAwareController;
import DAO.RestaurantService;
import Model.RestaurantTable;
import Model.User;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for changing a table's status from Dirty to Clean
 */
public class BusboyTableStatusController implements Initializable, UserAwareController {
    private static final Logger logger = Logger.getLogger(BusboyTableStatusController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    private RestaurantTable currentTable;
    
    @FXML private Label tableLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea noteTextArea;
    @FXML private Button cleanButton;
    @FXML private Button closeButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing BusboyTableStatusController");
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        logger.info("Current user set: " + user.getUsername());
    }
    
    /**
 * Set the current table to process
 */
public void setTable(RestaurantTable table) {
    if (table == null) {
        logger.warning("Null table passed to setTable method");
        return;
    }
    
    this.currentTable = table;
    
    // Update the UI with table information
    if (tableLabel != null) {
        tableLabel.setText(table.getTableNumber());
    }
    
    if (statusLabel != null) {
        if ("DIRTY".equals(table.getStatus())) {
            statusLabel.setText("Dirty");
            statusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#f8312f"));
        } else {
            statusLabel.setText(table.getStatus());
        }
    }
    
    // Only enable the clean button if the table is dirty
    if (cleanButton != null) {
        cleanButton.setDisable(!table.getStatus().equals("DIRTY"));
    }
}
    
    /**
     * Convert database status to display status
     */
    private String getDisplayStatus(String status) {
        switch (status) {
            case "AVAILABLE":
                return "Clean";
            case "OCCUPIED":
                return "Occupied";
            case "DIRTY":
                return "Dirty";
            default:
                return status;
        }
    }
    
    /**
     * Handle set clean button click
     */
    @FXML
    void onSetClean(ActionEvent event) {
        if (currentTable == null || currentUser == null) {
            showAlert("Error", "Table not selected");
            return;
        }
        
        try {
            // Only allow changing from DIRTY to AVAILABLE
            if (!currentTable.getStatus().equals("DIRTY")) {
                showAlert("Invalid Action", "Only dirty tables can be cleaned");
                return;
            }
            
            // Add note if provided (could be stored in a table notes table in future)
            String note = noteTextArea.getText();
            if (note != null && !note.isEmpty()) {
                logger.info("Table note: " + note);
                // In a real app, we would save this note to the database
            }
            
            // Update the table status
            boolean success = service.updateTableStatus(currentTable.getTableId(), "AVAILABLE", currentUser.getUserId());
            
            if (success) {
                showAlert("Success", "Table has been cleaned and marked as available");
                
                // Close the dialog
                Stage stage = (Stage) cleanButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Error", "Failed to update table status");
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error cleaning table", ex);
            showAlert("Error", "Could not clean table: " + ex.getMessage());
        }
    }
    
    /**
     * Handle close button click
     */
    @FXML
    void onClose(ActionEvent event) {
        // Close the dialog
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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
