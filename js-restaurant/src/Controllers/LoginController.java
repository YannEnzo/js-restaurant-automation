package Controllers;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import DAO.UserDAO;
import Model.User;
import DAO.RestaurantService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    private Label errorMessage;
    
    @FXML
    void onCancel(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    void onReset(ActionEvent event) {
        username.setText("");
        password.setText("");
        if (errorMessage != null) {
            errorMessage.setText("");
        }
    }
    
    @FXML
    void onSubmit(ActionEvent event) {
        String user = username.getText();
        String pass = password.getText();
        
        // Validate input
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Username and password are required.");
            return;
        }
        
        // Authenticate user
        boolean authenticated = service.authenticateUser(user, pass);
        
        if (authenticated) {
            // Get user details
            User currentUser = service.getUserByUsername(user);
            
            if (currentUser != null) {
                // Clock in the user
                service.clockInEmployee(currentUser.getUserId());
                
                // Redirect to appropriate view based on role
                try {
                    // Close login window
                    Node source = (Node) event.getSource();
                    Stage loginStage = (Stage) source.getScene().getWindow();
                    loginStage.close();
                    
                    // Determine which screen to load based on user role
                    String fxmlFile;
                    String title;
                    
                    switch (currentUser.getRole()) {
                        case "MANAGER":
                            fxmlFile = "/Views/ManagerDashboard.fxml";
                            title = "Manager Dashboard";
                            break;
                        case "WAITER":
                            fxmlFile = "/Views/WaiterDashboard.fxml";
                            title = "Waiter Dashboard";
                            break;
                        case "COOK":
                            fxmlFile = "/Views/KitchenView.fxml";
                            title = "Kitchen View";
                            break;
                        case "BUSBOY":
                            fxmlFile = "/Views/BusboyView.fxml";
                            title = "Busboy View";
                            break;
                        default:
                            showError("Unknown user role: " + currentUser.getRole());
                            return;
                    }
                    
                    // Load the appropriate view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Parent root = loader.load();
                    
                    // If the controller needs the user object, we can pass it
                    Object controller = loader.getController();
                    if (controller instanceof UserAwareController) {
                        ((UserAwareController) controller).setCurrentUser(currentUser);
                    }
                    
                    // Create and show new stage
                    Stage stage = new Stage();
                    stage.setTitle(title);
                    stage.setScene(new Scene(root));
                    stage.setMaximized(true);
                    stage.show();
                    
                    logger.info("User " + currentUser.getUsername() + " logged in successfully");
                    
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error loading view", ex);
                    showError("Error loading application view: " + ex.getMessage());
                }
            } else {
                showError("Error retrieving user information.");
            }
        } else {
            showError("Invalid username or password.");
        }
    }
    
    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
        } else {
            // Fallback to dialog if no error label is available
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}

// Interface to be implemented by controllers that need user information
interface UserAwareController {
    void setCurrentUser(User user);
}