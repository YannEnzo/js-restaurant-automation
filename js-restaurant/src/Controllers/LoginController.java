package Controllers;

import DAO.UserDAO;
import Model.User;
import DAO.RestaurantService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    
    // Track login attempts for each username
    private static final Map<String, Integer> loginAttempts = new HashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_SECONDS = 30;
    
    @FXML
    private TextField username;
    
    @FXML
    private PasswordField password;
    
    @FXML
    private Label errorMessage;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Button cancelButton;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Clear any existing error messages
        if (errorMessage != null) {
            errorMessage.setText("");
        }
        
        // Add listeners to input fields to clear error message when user types
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessage.setText("");
        });
        
        password.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessage.setText("");
        });
    }
    
    /**
     * Handle cancel button click
     * @param event ActionEvent
     */
    @FXML
    void onCancel(ActionEvent event) {
        // Get the source of the event and close the window
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        
        logger.info("Login cancelled by user");
        stage.close();
    }
    
    /**
     * Handle reset button click
     * @param event ActionEvent
     */
    @FXML
    void onReset(ActionEvent event) {
        // Clear username and password fields
        username.setText("");
        password.setText("");
        
        // Clear any error messages
        if (errorMessage != null) {
            errorMessage.setText("");
        }
        
        // Set focus to username field
        username.requestFocus();
        
        logger.info("Login form reset");
    }
    
    /**
     * Handle login button click
     * @param event ActionEvent
     */
    @FXML
    void onSubmit(ActionEvent event) {
        // Get username and password values
        String user = username.getText().trim();
        String pass = password.getText();
        
        // Validate input fields
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Username and password are required", Color.RED);
            return;
        }
        
        // Check if user is locked out
        if (isUserLockedOut(user)) {
            showError("Account temporarily locked. Please try again later.", Color.RED);
            return;
        }
        
        try {
            // First check if the username exists
            User existingUser = UserDAO.getInstance().getByUsername(user);
            
            if (existingUser == null) {
                // Username doesn't exist
                showError("Username not found. Please check your username.", Color.RED);
                recordFailedAttempt(user);
                return;
            }
            
            // Now check if the password is correct
            boolean authenticated = service.authenticateUser(user, pass);
            
            if (!authenticated) {
                // Password is incorrect
                showError("Incorrect password. Please try again.", Color.RED);
                recordFailedAttempt(user);
                return;
            }
            
            // Authentication successful - reset login attempts
            resetLoginAttempts(user);
            
            // Clock in the user
            service.clockInEmployee(existingUser.getUserId());
            
            // Log successful login
            logger.info("User " + existingUser.getUsername() + " logged in successfully");
            
            // Redirect to appropriate view based on role
            redirectToRoleBasedView(existingUser, event);
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error during login", ex);
            showError("Database error. Please try again later.", Color.RED);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error during login", ex);
            showError("An unexpected error occurred. Please try again.", Color.RED);
        }
    }
    
    /**
     * Record a failed login attempt for a username
     * @param username Username that failed to login
     */
    private void recordFailedAttempt(String username) {
        int attempts = loginAttempts.getOrDefault(username, 0) + 1;
        loginAttempts.put(username, attempts);
        
        logger.warning("Failed login attempt " + attempts + " for user: " + username);
        
        // If max attempts reached, show message about account being locked
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            showError("Too many failed attempts. Account locked for " + 
                    LOCKOUT_DURATION_SECONDS + " seconds.", Color.RED);
            
            // Disable the login button
            loginButton.setDisable(true);
            
            // Set a timer to unlock the account
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(LOCKOUT_DURATION_SECONDS));
            
            pause.setOnFinished(e -> {
                loginButton.setDisable(false);
                errorMessage.setText("Account unlocked. You may try again.");
                errorMessage.setTextFill(Color.GREEN);
            });
            
            pause.play();
        } else {
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts;
            showError("Login failed. " + remainingAttempts + 
                    " attempt" + (remainingAttempts != 1 ? "s" : "") + " remaining.", Color.RED);
        }
    }
    
    /**
     * Check if a user is locked out due to too many failed login attempts
     * @param username Username to check
     * @return true if user is locked out, false otherwise
     */
    private boolean isUserLockedOut(String username) {
        Integer attempts = loginAttempts.get(username);
        return attempts != null && attempts >= MAX_LOGIN_ATTEMPTS;
    }
    
    /**
     * Reset login attempts for a username after successful login
     * @param username Username to reset
     */
    private void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
    }
    
    /**
     * Redirect user to appropriate view based on role
     * @param user User object of authenticated user
     * @param event Action event for getting source node
     */
    private void redirectToRoleBasedView(User user, ActionEvent event) {
        try {
            // Close login window
            Node source = (Node) event.getSource();
            Stage loginStage = (Stage) source.getScene().getWindow();
            
            // Determine which screen to load based on user role
            String fxmlFile;
            String title;
            
            switch (user.getRole()) {
                case "MANAGER":
                    fxmlFile = "/Views/managerViews/managerDashboard.fxml";
                    title = "Manager Dashboard - " + user.getFullName();
                    break;
            /*    case "WAITER":
                    fxmlFile = "/Views/WaiterDashboard.fxml";
                    title = "Waiter Dashboard - " + user.getFullName();
                    break;
                case "COOK":
                    fxmlFile = "/Views/KitchenView.fxml";
                    title = "Kitchen View - " + user.getFullName();
                    break;
                case "BUSBOY":
                    fxmlFile = "/Views/BusboyView.fxml";
                    title = "Busboy View - " + user.getFullName();
                    break;*/
                default:
                    showError("Unknown user role: " + user.getRole(), Color.RED);
                    return;
            } 
            
            // Load the appropriate view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // If the controller needs the user object, pass it
            Object controller = loader.getController();
            if (controller instanceof UserAwareController) {
                ((UserAwareController) controller).setCurrentUser(user);
            }
            
            // Create and show new stage
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            
            // Close the login stage after successful login and navigation
            loginStage.close();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading view", ex);
            showError("Error loading application view: " + ex.getMessage(), Color.RED);
        }
    }
    
    /**
     * Show error message to user
     * @param message Error message text
     * @param color Text color for message
     */
    private void showError(String message, Color color) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setTextFill(color);
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

/**
 * Interface to be implemented by controllers that need user information
 */
interface UserAwareController {
    void setCurrentUser(User user);
}