package Controllers.cook;
import Controllers.Busboy.*;
import Controllers.UserAwareController;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import java.util.Optional;
import DAO.UserDAO;
import DAO.RestaurantService;
import DAO.TimeRecordDAO;
import DAO.UserDAO;
import Model.TimeRecord;
import Model.User;
import static com.sun.deploy.uitoolkit.ToolkitStore.dispose;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controller for the Account view
 */
public class cookAccountController implements Initializable {
    private static final Logger logger = Logger.getLogger(cookAccountController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    private TimeRecord activeTimeRecord;
    
    // User information
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label userIdLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Label contactLabel;
    
    @FXML
    private Label lastLoginLabel;
    
    // Time clock information
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label todayHoursLabel;
    
    @FXML
    private Label clockInTimeLabel;
    
    @FXML
    private Button clockInBtn;
    
    @FXML
    private Button clockOutBtn;
    
    // Timesheet information
    @FXML
    private Label totalHoursLabel;
    
    @FXML
    private Label scheduledHoursLabel;
    
    @FXML
    private Label completionLabel;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private AnchorPane accountPane;
    
    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.info("Initializing account controller");
            System.out.println("AccountController initializing...");
            
            // Verify UI components
            if (statusLabel == null) System.out.println("WARNING: statusLabel is null");
            if (todayHoursLabel == null) System.out.println("WARNING: todayHoursLabel is null");
            if (clockInTimeLabel == null) System.out.println("WARNING: clockInTimeLabel is null");
            if (clockInBtn == null) System.out.println("WARNING: clockInBtn is null");
            if (clockOutBtn == null) System.out.println("WARNING: clockOutBtn is null");
            if (logoutBtn == null) System.out.println("WARNING: logoutBtn is null");
            
            // Initialize clock buttons state - will be properly set when user is loaded
            if (clockInBtn != null) clockInBtn.setDisable(true);
            if (clockOutBtn != null) clockOutBtn.setDisable(true);
            
            logger.info("Account controller initialized");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error initializing account controller", ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Set the current user for this controller
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        System.out.println("Setting current user: " + (user != null ? user.getUserId() : "null"));
        this.currentUser = user;
        
        if (user != null) {
            // Update UI with user information
            updateUserInfo();
            
            // Check if user is already clocked in
            checkTimeClockStatus();
            
            // Start timer for ongoing time updates
            startTimeUpdateTimer();
        }
    }
        /**
     * Handle back button click
     * @param event ActionEvent
     */
@FXML
    void onBack(ActionEvent event) throws Exception {
        try {
            // Get the current stage
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();
            
            // Load the table view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/cook/kitchenView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the current user if needed
            Object controller = loader.getController();
            if (controller instanceof UserAwareController) {
                ((UserAwareController) controller).setCurrentUser(currentUser);
            }
            
            // Set up the scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("J's Restaurant - Kitchen View");
            
            // Cleanup resources
            dispose();
            
            logger.info("Navigated back to Kitchen view");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error navigating back to Kitchen view", ex);
            showAlert(AlertType.ERROR, "Navigation Error", 
                    "Could not navigate back to Kitchen view", 
                    ex.getMessage());
        }
    }

    /**
     * Update the UI with user information
     */
    private void updateUserInfo() {
        if (nameLabel != null) nameLabel.setText(currentUser.getFullName());
        if (userIdLabel != null) userIdLabel.setText(currentUser.getUserId());
        if (roleLabel != null) roleLabel.setText(currentUser.getRole());
        if (contactLabel != null) contactLabel.setText(currentUser.getContactNumber());
        
        // Set last login time
        if (lastLoginLabel != null) {
            lastLoginLabel.setText(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")));
        }
        
        // Update timesheet information
        updateTimesheetInfo();
        
        System.out.println("User info updated for: " + currentUser.getUserId());
    }
    
    /**
     * Check if the user is already clocked in and update UI accordingly
     */
    private void checkTimeClockStatus() {
        System.out.println("Checking time clock status for user: " + currentUser.getUserId());
        
        try {
            // Find most recent time record with null clock_out
            List<TimeRecord> records = TimeRecordDAO.getInstance().getRecordsForEmployee(currentUser.getUserId());
            
            if (!records.isEmpty()) {
                // Print out all records for debugging
                System.out.println("Found " + records.size() + " time records for user");
                for (int i = 0; i < Math.min(5, records.size()); i++) {
                    TimeRecord rec = records.get(i);
                    System.out.println("Record " + i + ": ID=" + rec.getRecordId() + 
                                      ", ClockIn=" + rec.getClockInTime() + 
                                      ", ClockOut=" + (rec.getClockOutTime() != null ? 
                                                     rec.getClockOutTime() : "NULL"));
                }
                
                // Get most recent record
                TimeRecord latestRecord = records.get(0);
                
                if (latestRecord.getClockOutTime() == null) {
                    // User is currently clocked in
                    activeTimeRecord = latestRecord;
                    System.out.println("User is currently clocked in, record ID: " + activeTimeRecord.getRecordId());
                    updateClockInStatus(true);
                } else {
                    // User is not clocked in
                    activeTimeRecord = null;
                    System.out.println("User is NOT currently clocked in");
                    updateClockInStatus(false);
                }
            } else {
                // No time records found
                activeTimeRecord = null;
                System.out.println("No time records found for user");
                updateClockInStatus(false);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error checking time clock status", ex);
            ex.printStackTrace();
            
            // Default to not clocked in on error
            activeTimeRecord = null;
            updateClockInStatus(false);
        }
    }
    
    /**
     * Update the UI based on clock in status
     * @param isClockedIn Whether the user is clocked in
     */
    private void updateClockInStatus(boolean isClockedIn) {
        System.out.println("Updating UI for clock status: " + (isClockedIn ? "CLOCKED IN" : "NOT CLOCKED IN"));
        
        if (isClockedIn && activeTimeRecord != null) {
            // User is clocked in
            if (statusLabel != null) {
                statusLabel.setText("Clocked In");
                statusLabel.getStyleClass().remove("status-inactive");
                statusLabel.getStyleClass().add("status-active");
            }
            
            // Calculate hours worked so far
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime clockInTime = activeTimeRecord.getClockInTime();
            Duration duration = Duration.between(clockInTime, now);
            long hoursWorked = duration.toHours();
            double minutesFraction = (duration.toMinutes() % 60) / 60.0;
            
            // Format as 5.0 hrs (Ongoing)
            if (todayHoursLabel != null) {
                todayHoursLabel.setText(String.format("%.1f hrs (Ongoing)", hoursWorked + minutesFraction));
            }
            
            // Format clock in time as "Today, 10:00 a.m"
            if (clockInTimeLabel != null) {
                clockInTimeLabel.setText(clockInTime.format(
                        DateTimeFormatter.ofPattern("'Today, 'h:mm a")));
            }
            
            // Enable clock out button and disable clock in button
            if (clockInBtn != null) clockInBtn.setDisable(true);
            if (clockOutBtn != null) clockOutBtn.setDisable(false);
            if (clockInBtn != null) clockInBtn.setOpacity(0.5);
            if (clockOutBtn != null) clockOutBtn.setOpacity(1.0);
            
            System.out.println("UI updated for clocked in state");
        } else {
            // User is not clocked in
            if (statusLabel != null) {
                statusLabel.setText("Not Clocked In");
                statusLabel.getStyleClass().remove("status-active");
                statusLabel.getStyleClass().add("status-inactive");
            }
            
            if (todayHoursLabel != null) todayHoursLabel.setText("0.0 hrs");
            if (clockInTimeLabel != null) clockInTimeLabel.setText("Not clocked in yet");
            
            // Enable clock in button and disable clock out button
            if (clockInBtn != null) clockInBtn.setDisable(false);
            if (clockOutBtn != null) clockOutBtn.setDisable(true);
            if (clockInBtn != null) clockInBtn.setOpacity(1.0);
            if (clockOutBtn != null) clockOutBtn.setOpacity(0.5);
            
            System.out.println("UI updated for not clocked in state");
        }
    }
    
    /**
     * Update the timesheet information
     */
    private void updateTimesheetInfo() {
        try {
            // Get all time records for this week
            List<TimeRecord> records = TimeRecordDAO.getInstance().getRecordsForEmployee(currentUser.getUserId());
            
            double totalHours = 0.0;
            
            for (TimeRecord record : records) {
                if (record.getTotalHours() != null) {
                    totalHours += record.getTotalHours();
                }
            }
            
            if (totalHoursLabel != null) totalHoursLabel.setText(String.format("%.1f hrs", totalHours));
            
            // This would typically come from a schedule in the database
            double scheduledHours = 40.0;
            if (scheduledHoursLabel != null) scheduledHoursLabel.setText(String.format("%.1f hrs", scheduledHours));
            
            // Calculate completion percentage
            double completionPercentage = (totalHours / scheduledHours) * 100;
            if (completionLabel != null) completionLabel.setText(String.format("%.1f%%", completionPercentage));
            
            System.out.println("Timesheet info updated: " + totalHours + " hrs worked");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating timesheet info", ex);
            ex.printStackTrace();
            
            if (totalHoursLabel != null) totalHoursLabel.setText("0.0 hrs");
            if (scheduledHoursLabel != null) scheduledHoursLabel.setText("0.0 hrs");
            if (completionLabel != null) completionLabel.setText("0%");
        }
    }
    
    /**
     * Handle clock in button click
     * @param event ActionEvent
     */
    @FXML
    void onClockIn(ActionEvent event) {
        System.out.println("Clock In button clicked");
        
        try {
            if (currentUser == null) {
                System.err.println("No current user set!");
                return;
            }
            
            // Check if already clocked in
            if (activeTimeRecord != null) {
                showAlert(AlertType.WARNING, "Already Clocked In", 
                        "You are already clocked in", 
                        "You must clock out before clocking in again.");
                return;
            }
            
            boolean success = service.clockInEmployee(currentUser.getUserId());
            
            if (success) {
                logger.info("User clocked in: " + currentUser.getUserId());
                
                // Refresh time clock status
                checkTimeClockStatus();
                
                showAlert(AlertType.INFORMATION, "Clock In", 
                        "Clock In Successful", "You have successfully clocked in.");
            } else {
                logger.warning("Failed to clock in user: " + currentUser.getUserId());
                showAlert(AlertType.ERROR, "Clock In Error", 
                        "Failed to Clock In", "There was an error clocking in. Please try again.");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error clocking in", ex);
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Clock In Error", 
                    "Failed to Clock In", "There was an error clocking in: " + ex.getMessage());
        }
    }
    
    /**
     * Handle clock out button click
     * @param event ActionEvent
     */
    @FXML
    void onClockOut(ActionEvent event) {
        System.out.println("Clock Out button clicked");
        
        try {
            if (currentUser == null) {
                System.err.println("No current user set!");
                return;
            }
            
            // Verify that we have an active time record
            if (activeTimeRecord == null) {
                System.err.println("No active time record found!");
                showAlert(AlertType.WARNING, "Not Clocked In", 
                        "No active clock-in record found", 
                        "You must be clocked in before you can clock out.");
                return;
            }
            
            // Show confirmation dialog
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Clock Out");
            confirmAlert.setHeaderText("Confirm Clock Out");
            confirmAlert.setContentText("Are you sure you want to clock out?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Direct database update for debugging
                boolean success = TimeRecordDAO.getInstance().clockOut(currentUser.getUserId());
                
                if (success) {
                    logger.info("User clocked out: " + currentUser.getUserId());
                    
                    // Reset active time record
                    activeTimeRecord = null;
                    
                    // Refresh time clock status
                    checkTimeClockStatus();
                    
                    // Update timesheet info which will have changed after clocking out
                    updateTimesheetInfo();
                    
                    showAlert(AlertType.INFORMATION, "Clock Out", 
                            "Clock Out Successful", "You have successfully clocked out.");
                } else {
                    logger.warning("Failed to clock out user: " + currentUser.getUserId());
                    showAlert(AlertType.ERROR, "Clock Out Error", 
                            "Failed to Clock Out", "There was an error clocking out. Please try again.");
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error clocking out", ex);
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Clock Out Error", 
                    "Failed to Clock Out", "There was an error clocking out: " + ex.getMessage());
        }
    }
    
    /**
     * Handle logout button click
     * @param event ActionEvent
     */
    @FXML
    void onLogout(ActionEvent event) {
        System.out.println("Logout button clicked");
        
        try {
            // Confirm logout
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Log Out");
            confirmAlert.setHeaderText("Confirm Log Out");
            confirmAlert.setContentText("Are you sure you want to log out?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If user is clocked in, ask if they want to clock out before logging out
                if (activeTimeRecord != null) {
                    Alert clockOutAlert = new Alert(AlertType.CONFIRMATION);
                    clockOutAlert.setTitle("Clock Out");
                    clockOutAlert.setHeaderText("You are still clocked in");
                    clockOutAlert.setContentText("Do you want to clock out before logging out?");
                    
                    Optional<ButtonType> clockOutResult = clockOutAlert.showAndWait();
                    if (clockOutResult.isPresent() && clockOutResult.get() == ButtonType.OK) {
                        TimeRecordDAO.getInstance().clockOut(currentUser.getUserId());
                    }
                }
                
                // Load the login screen
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/login.fxml"));
                    Parent root = loader.load();
                    
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("J's Restaurant - Login");
                    stage.show();
                    
                    System.out.println("Returned to login screen");
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error loading login view", ex);
                    ex.printStackTrace();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error logging out", ex);
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Logout Error", 
                    "Could not log out", "There was an error logging out: " + ex.getMessage());
        }
    }
    
    /**
     * Handle change password button click
     * @param event ActionEvent
     */
   @FXML
void onChangePassword(ActionEvent event) {
    // Check if current user is a manager
    if (currentUser != null && "MANAGER".equals(currentUser.getRole())) {
        // Create a dialog for password change
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password");
        
        // Set dialog buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create password fields
        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        
        // Add the fields to the grid
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        
        // Add some validation to password fields
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        grid.add(errorLabel, 1, 3);
        
        // Add validation listeners
        newPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters");
            } else {
                errorLabel.setText("");
            }
        });
        
        // Set the dialog content
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the current password field
        Platform.runLater(() -> currentPassword.requestFocus());
        
        // Enable/Disable OK button depending on whether fields are filled
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        
        // Validation for enabling OK button
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            boolean disableButton = currentPassword.getText().trim().isEmpty() || 
                    newPassword.getText().trim().isEmpty() || 
                    confirmPassword.getText().trim().isEmpty() ||
                    !newPassword.getText().equals(confirmPassword.getText()) ||
                    newPassword.getText().length() < 6;
            okButton.setDisable(disableButton);
        };
        
        currentPassword.textProperty().addListener(changeListener);
        newPassword.textProperty().addListener(changeListener);
        confirmPassword.textProperty().addListener(changeListener);
        
        // Show the dialog and wait for user action
        Optional<ButtonType> result = dialog.showAndWait();
        
        // Process the result
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // First check if current password is correct
                boolean authenticated = service.authenticateUser(currentUser.getUsername(), currentPassword.getText());
                
                if (authenticated) {
                    // Update password in database
                    UserDAO userDAO = UserDAO.getInstance();
                    User updatedUser = currentUser; // Create a copy if needed
                    boolean success = userDAO.updateWithPassword(updatedUser, newPassword.getText());
                    
                    if (success) {
                        showAlert(AlertType.INFORMATION, "Password Updated", 
                                "Password Changed Successfully", 
                                "Your password has been updated.");
                        logger.info("Password changed successfully for user: " + currentUser.getUserId());
                    } else {
                        showAlert(AlertType.ERROR, "Update Failed", 
                                "Could Not Update Password", 
                                "An error occurred while updating your password. Please try again.");
                        logger.warning("Failed to update password for user: " + currentUser.getUserId());
                    }
                } else {
                    showAlert(AlertType.ERROR, "Authentication Failed", 
                            "Current Password Is Incorrect", 
                            "The current password you entered is incorrect. Please try again.");
                    logger.warning("Failed password change attempt for user: " + currentUser.getUserId() + " - Incorrect current password");
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Database error updating password", ex);
                showAlert(AlertType.ERROR, "Database Error", 
                        "Could Not Update Password", 
                        "A database error occurred: " + ex.getMessage());
            }
        }
    } else {
        // For non-manager users
        showAlert(AlertType.INFORMATION, "Password Change", 
                "Contact Manager", 
                "Please contact your manager to change your password.");
        logger.info("Non-manager user attempted to change password: " + 
                (currentUser != null ? currentUser.getUserId() : "unknown user"));
    }
}
    
    /**
     * Handle change photo button click
     * @param event ActionEvent
     */
    @FXML
    void onChangePhoto(ActionEvent event) {
        showAlert(AlertType.INFORMATION, "Change Photo", 
                "Feature Not Available", "The change photo feature is not yet implemented.");
    }
    
    /**
     * Start a timer to update the ongoing time display
     */
    private void startTimeUpdateTimer() {
        // Create a timer to update the ongoing hours display every minute
        Thread timeUpdateThread = new Thread(() -> {
            while (true) {
                try {
                    // Sleep for 1 minute
                    Thread.sleep(60000);
                    
                    // Update the UI on the JavaFX thread
                    Platform.runLater(() -> {
                        if (activeTimeRecord != null && todayHoursLabel != null) {
                            // Calculate hours worked so far
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime clockInTime = activeTimeRecord.getClockInTime();
                            Duration duration = Duration.between(clockInTime, now);
                            long hoursWorked = duration.toHours();
                            double minutesFraction = (duration.toMinutes() % 60) / 60.0;
                            
                            // Update the hours display
                            todayHoursLabel.setText(String.format("%.1f hrs (Ongoing)", hoursWorked + minutesFraction));
                        }
                    });
                } catch (InterruptedException ex) {
                    // Thread was interrupted, exit the loop
                    break;
                }
            }
        });
        
        // Set as daemon thread so it doesn't prevent application exit
        timeUpdateThread.setDaemon(true);
        timeUpdateThread.start();
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
     * Refresh the account data
     */
    public void refreshData() {
        if (currentUser != null) {
            checkTimeClockStatus();
            updateTimesheetInfo();
        }
    }
}