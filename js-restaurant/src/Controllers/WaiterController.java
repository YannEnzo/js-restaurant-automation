/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;
 

import Model.RestaurantTable;
import Model.User;
import DAO.TableStatusObserver;
import DAO.RestaurantService;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Controller for waiter screens
 */
public class WaiterController implements TableStatusObserver {
    private final RestaurantService service;
    private User currentWaiter;
    
    public WaiterController() {
        service = RestaurantService.getInstance();
        // Register for table status updates
        service.addTableStatusObserver(this);
    }
    
    public void setCurrentWaiter(User waiter) {
        this.currentWaiter = waiter;
    }
    
    public List<RestaurantTable> getAssignedTables() {
        return service.getTablesByWaiter(currentWaiter.getUserId());
    }
    
    public boolean changeTableStatus(String tableId, String newStatus) {
        return service.updateTableStatus(tableId, newStatus, currentWaiter.getUserId());
    }
    
    public void createOrder(String tableId) {
        service.createOrder(tableId, currentWaiter.getUserId());
    }
    
    @Override
    public void onTableStatusChanged(String tableId, String newStatus) {
        // This gets called whenever a table's status changes
        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            // Update UI component that shows tables
            // This is just an example - you would update your actual UI here
            if (newStatus.equals("READY")) {
                // Show notification to waiter
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Ready");
                alert.setHeaderText("Order for Table " + tableId + " is ready!");
                alert.show();
            }
        });
    }
    
    public void dispose() {
        // Unregister observer when controller is no longer needed
        service.removeTableStatusObserver(this);
    }
}