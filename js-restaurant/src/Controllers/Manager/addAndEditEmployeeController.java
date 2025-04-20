package Controllers.Manager;

import Model.User;
import DAO.UserDAO;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXPasswordField;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class addAndEditEmployeeController implements Initializable {
    private static final Logger logger = Logger.getLogger(addAndEditEmployeeController.class.getName());
    private final UserDAO userDAO = UserDAO.getInstance();
    private User existingUser = null; // Will be null for add, non-null for edit
    private boolean isEditMode = false;
    
    @FXML
    private JFXTextField empContact;
    
    @FXML
    private JFXTextField empUsername;
    
    @FXML
    private JFXComboBox<String> empRole;
    
    @FXML
    private JFXTextField empName;
    
    @FXML
    private PasswordField empPwd;
    
    @FXML
    private Label titleLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup role dropdown
        empRole.setItems(FXCollections.observableArrayList(
                "MANAGER", "WAITER", "COOK", "BUSBOY"
        ));
        
        // Default selection
        empRole.getSelectionModel().select("WAITER");
    }
    
    /**
     * Set existing user for edit mode
     * @param user User to edit
     */
    public void setUserForEdit(User user) {
        this.existingUser = user;
        this.isEditMode = true;
        
        // Set title for edit mode
        if (titleLabel != null) {
            titleLabel.setText("Edit Employee");
        }
        
        // Populate fields with existing data
        empName.setText(user.getFullName());
        empContact.setText(user.getContactNumber());
        empUsername.setText(user.getUsername());
        empRole.getSelectionModel().select(user.getRole());
        
        // Password field left blank - will only update if entered
        empPwd.clear();
        
        logger.info("Form set up for editing user: " + user.getUserId());
    }
    
    @FXML
    void onReset(ActionEvent event) {
        // Clear form or reset to initial values
        if (isEditMode && existingUser != null) {
            // If editing, reset to original values
            empName.setText(existingUser.getFullName());
            empContact.setText(existingUser.getContactNumber());
            empUsername.setText(existingUser.getUsername());
            empRole.getSelectionModel().select(existingUser.getRole());
            empPwd.clear();
        } else {
            // If adding new, clear all fields
            empName.clear();
            empContact.clear();
            empUsername.clear();
            empRole.getSelectionModel().select("WAITER");
            empPwd.clear();
        }
        
        logger.info("Form reset");
    }
    
    @FXML
    void onSubmit(ActionEvent event) {
        // Validate fields
        if (!validateFields()) {
            return;
        }
        
        try {
            if (isEditMode) {
                // Update existing user
                updateUser();
            } else {
                // Add new user
                addUser();
            }
            
            // Close the form window
            Stage stage = (Stage) empName.getScene().getWindow();
            stage.close();
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error during user save", ex);
            showAlert(AlertType.ERROR, "Database Error", 
                     "A database error occurred while saving the employee.",
                     ex.getMessage());
        }
    }
    
    /**
     * Validate form fields
     * @return true if valid, false otherwise
     */
    private boolean validateFields() {
        // Check name
        String name = empName.getText().trim();
        if (name.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", 
                     "Name is required", 
                     "Please enter the employee's name.");
            empName.requestFocus();
            return false;
        }
        
        // Check username (must be unique if adding or changing)
        String username = empUsername.getText().trim();
        if (username.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", 
                     "Username is required", 
                     "Please enter a username.");
            empUsername.requestFocus();
            return false;
        }
        
        // Check username uniqueness
        try {
            User existingUserWithName = userDAO.getByUsername(username);
            if (existingUserWithName != null) {
                // If adding or changing to a username that already exists
                if (!isEditMode || !existingUserWithName.getUserId().equals(existingUser.getUserId())) {
                    showAlert(AlertType.ERROR, "Validation Error", 
                             "Username is already taken", 
                             "Please choose a different username.");
                    empUsername.requestFocus();
                    return false;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error checking username uniqueness", ex);
            showAlert(AlertType.ERROR, "Database Error", 
                     "Could not validate username", 
                     "A database error occurred: " + ex.getMessage());
            return false;
        }
        
        // Check role
        if (empRole.getSelectionModel().isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", 
                     "Role is required", 
                     "Please select a role for the employee.");
            empRole.requestFocus();
            return false;
        }
        
        // Check password (required for new users, optional for edits)
        String password = empPwd.getText();
        if (!isEditMode && password.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", 
                     "Password is required", 
                     "Please enter a password for the new employee.");
            empPwd.requestFocus();
            return false;
        }
        
        // All validations passed
        return true;
    }
    
    /**
     * Add a new user to the database
     * @throws SQLException If a database error occurs
     */
    private void addUser() throws SQLException {
        // Parse name into first and last name
        String[] nameParts = parseFullName(empName.getText().trim());
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        // Generate a new user ID
        String userId = generateUserId();
        
        // Create user object
        User newUser = new User(
                userId,
                empUsername.getText().trim(),
                firstName,
                lastName,
                empRole.getValue(),
                empContact.getText().trim(),
                true // isActive
        );
        
        // Save to database
        boolean success = userDAO.addWithPassword(newUser, empPwd.getText());
        
        if (success) {
            logger.info("New user added: " + userId);
            showAlert(AlertType.INFORMATION, "Success", 
                     "Employee Added", 
                     "Employee has been successfully added.");
        } else {
            logger.warning("Failed to add user: " + userId);
            showAlert(AlertType.ERROR, "Error", 
                     "Failed to Add Employee", 
                     "There was an error adding the employee. Please try again.");
        }
    }
    
    /**
     * Update an existing user in the database
     * @throws SQLException If a database error occurs
     */
    private void updateUser() throws SQLException {
        if (existingUser == null) {
            logger.severe("Attempted to update null user");
            return;
        }
        
        // Parse name into first and last name
        String[] nameParts = parseFullName(empName.getText().trim());
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        // Update user object
        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setUsername(empUsername.getText().trim());
        existingUser.setRole(empRole.getValue());
        existingUser.setContactNumber(empContact.getText().trim());
        
        boolean success;
        
        // If password field is not empty, update with new password
        if (!empPwd.getText().isEmpty()) {
            success = userDAO.updateWithPassword(existingUser, empPwd.getText());
        } else {
            // Otherwise just update user details
            success = userDAO.update(existingUser);
        }
        
        if (success) {
            logger.info("User updated: " + existingUser.getUserId());
            showAlert(AlertType.INFORMATION, "Success", 
                     "Employee Updated", 
                     "Employee has been successfully updated.");
        } else {
            logger.warning("Failed to update user: " + existingUser.getUserId());
            showAlert(AlertType.ERROR, "Error", 
                     "Failed to Update Employee", 
                     "There was an error updating the employee. Please try again.");
        }
    }
    
    /**
     * Parse full name into first and last name
     * @param fullName Full name string
     * @return Array with [firstName, lastName]
     */
    private String[] parseFullName(String fullName) {
        String[] parts = fullName.split("\\s+", 2);
        return parts;
    }
    
    /**
     * Generate a new user ID
     * @return New user ID in format USER###
     */
    private String generateUserId() {
    try {
        // Get the highest existing user ID
        List<User> users = userDAO.getAll();
        int maxId = 0;
        
        for (User user : users) {
            String userId = user.getUserId();
            if (userId.startsWith("USER")) {
                try {
                    // Extract the numeric portion
                    int idNum = Integer.parseInt(userId.substring(4));
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric IDs
                    logger.warning("Non-numeric user ID found: " + userId);
                }
            }
        }
        
        // Generate ID with next available number
        return "USER" + String.format("%03d", maxId + 1);
    } catch (SQLException ex) {
        logger.log(Level.WARNING, "Error getting user list, using timestamp ID", ex);
        // Fallback to using current timestamp
        return "USER" + System.currentTimeMillis() % 1000;
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