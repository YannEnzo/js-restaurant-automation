package Controllers.Manager;

import Model.User;
import DAO.RestaurantService;
import DAO.UserDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXTextField;
import java.util.Optional;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the employee management view
 * @author yanne
 */
public class manager_EmployeeviewController implements Initializable {
    private static final Logger logger = Logger.getLogger(manager_EmployeeviewController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private ObservableList<User> employeeList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    
    @FXML
    private TableView<User> nodeTable;
    
    @FXML
    private TableColumn<User, String> idNumberCol;
    
    @FXML
    private TableColumn<User, String> NameCol;
    
    @FXML
    private TableColumn<User, String> roleCol;
    
    @FXML
    private TableColumn<User, String> contactCol;
    
    @FXML
    private TableColumn<User, String> statusCol;
    
    @FXML
    private JFXTextField searchItems;
    
    @FXML
    private AnchorPane employeePane;

    /**
     * Initializes the controller class.
     * @param url The location used to resolve relative paths
     * @param rb The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up the table columns
        idNumberCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        statusCol.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> isActive ? "Active" : "Inactive");
        });
        
        // Load employee data
        loadEmployeeData();
        
        // Set up filtering
        filteredData = new FilteredList<>(employeeList, p -> true);
        
        // Set up sorting
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(nodeTable.comparatorProperty());
        
        // Add data to the table
        nodeTable.setItems(sortedData);
        
        // Add a placeholder when table is empty
        nodeTable.setPlaceholder(new Label("No employees found"));
        
        logger.info("Employee view initialized");
    }
    
    /**
     * Load employee data from the service
     */
    private void loadEmployeeData() {
        try {
            // Clear existing data
            employeeList.clear();
            
            // Get all users from the service
            employeeList.addAll(service.getAllUsers());
            
            if (employeeList.isEmpty()) {
                logger.info("No employees found in the database");
            } else {
                logger.info("Loaded " + employeeList.size() + " employees");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading employees", ex);
            showAlert(AlertType.ERROR, "Data Error", "Could not load employee data", 
                    "There was an error loading the employee data. Please try again later.");
        }
    }
    
    /**
     * Handle search functionality
     * @param event KeyEvent from search field
     */
    @FXML
    void search(KeyEvent event) {
        String searchText = searchItems.getText().toLowerCase();
        
        filteredData.setPredicate(employee -> {
            // If the search text is empty, show all employees
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            // Match against ID, name, role, or contact
            return employee.getUserId().toLowerCase().contains(searchText) ||
                   employee.getFullName().toLowerCase().contains(searchText) ||
                   employee.getRole().toLowerCase().contains(searchText) ||
                   employee.getContactNumber().toLowerCase().contains(searchText);
        });
        
        logger.info("Filtering employees with search: " + searchText);
    }
    
    // Add this to your manager_EmployeeviewController.java

@FXML
void onAdd(ActionEvent event) {
    try {
        // Use the correct path to your FXML file
        URL fxmlUrl = getClass().getResource("/Views/managerViews/addAndEditEmployees.fxml");
        
        if (fxmlUrl == null) {
            // Debug information to help find the issue
            logger.severe("Could not locate FXML file. Check the path: /Views/managerViews/addAndEditEmployees.fxml");
            
            // Try to list available resources in that directory to help debug
            URL dirUrl = getClass().getResource("/Views/managerViews/");
            if (dirUrl != null) {
                logger.info("Directory exists: " + dirUrl);
            } else {
                logger.severe("Directory does not exist: /Views/managerViews/");
            }
            
            showAlert(AlertType.ERROR, "Resource Error", "Could not find form resource", 
                    "The application could not locate the required form. Please contact support.");
            return;
        }
        
        // Load the form using the verified URL
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        
        // Create a new stage for the form
        Stage stage = new Stage();
        stage.setTitle("Add New Employee");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Show the form and wait for it to close
        stage.showAndWait();
        
        // Refresh employee data
        loadEmployeeData();
        
        logger.info("Add employee dialog shown");
    } catch (IOException ex) {
        logger.log(Level.SEVERE, "Error loading add employee form", ex);
        showAlert(AlertType.ERROR, "Form Error", "Could not open add employee form", 
                "There was an error opening the add employee form: " + ex.getMessage());
    }
}

@FXML
void onEdit(ActionEvent event) {
    // Get the selected employee
    User selectedEmployee = nodeTable.getSelectionModel().getSelectedItem();
    
    if (selectedEmployee == null) {
        showAlert(AlertType.WARNING, "Selection Error", "No Employee Selected", 
                "Please select an employee to edit.");
        return;
    }
    
    try {
        // Load the edit employee form
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/managerViews/addAndEditEmployees.fxml"));
        Parent root = loader.load();
        
        // Get the controller and pass the selected employee
        addAndEditEmployeeController controller = loader.getController();
        controller.setUserForEdit(selectedEmployee);
        
        // Create a new stage for the form
        Stage stage = new Stage();
        stage.setTitle("Edit Employee");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Show the form and wait for it to close
        stage.showAndWait();
        
        // Refresh employee data
        loadEmployeeData();
        
        logger.info("Edit employee dialog shown for user: " + selectedEmployee.getUserId());
    } catch (IOException ex) {
        logger.log(Level.SEVERE, "Error loading edit employee form", ex);
        showAlert(AlertType.ERROR, "Form Error", "Could not open edit employee form", 
                "There was an error opening the edit employee form. Please try again later.");
    }
}
    
  @FXML
void onDelete(ActionEvent event) {
    // Get the selected employee
    User selectedEmployee = nodeTable.getSelectionModel().getSelectedItem();
    
    if (selectedEmployee == null) {
        showAlert(AlertType.WARNING, "Selection Error", "No Employee Selected", 
                "Please select an employee to delete.");
        return;
    }
    
    // Ask for confirmation
    Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
    confirmAlert.setTitle("Delete Employee");
    confirmAlert.setHeaderText("Confirm Employee Deletion");
    confirmAlert.setContentText("Are you sure you want to delete employee " + 
            selectedEmployee.getFullName() + "? This action cannot be undone.");
    
    // Customize the buttons
    ButtonType buttonTypeYes = new ButtonType("Yes, Delete");
    ButtonType buttonTypeNo = new ButtonType("No, Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    confirmAlert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
    
    // Show dialog and wait for response
    Optional<ButtonType> result = confirmAlert.showAndWait();
    
    if (result.isPresent() && result.get() == buttonTypeYes) {
        try {
            // Delete the employee
            boolean success = UserDAO.getInstance().delete(selectedEmployee.getUserId());
            
            if (success) {
                logger.info("Employee deleted: " + selectedEmployee.getUserId());
                
                // Show success message
                showAlert(AlertType.INFORMATION, "Success", "Employee Deleted", 
                        "The employee " + selectedEmployee.getFullName() + " has been successfully deleted.");
                
                // Refresh employee data
                loadEmployeeData();
            } else {
                logger.warning("Failed to delete employee: " + selectedEmployee.getUserId());
                showAlert(AlertType.ERROR, "Delete Error", "Could not delete employee", 
                        "There was an error deleting the employee. Please try again later.");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Database error deleting employee", ex);
            showAlert(AlertType.ERROR, "Database Error", "Could not delete employee", 
                    "There was a database error deleting the employee: " + ex.getMessage());
        }
    } else {
        // User chose to cancel
        logger.info("Employee deletion canceled for: " + selectedEmployee.getUserId());
    }
}
    
    /**
     * Handle view timesheet button
     * @param event ActionEvent
     */
    @FXML
    void onViewTimeSheet(ActionEvent event) {
        // Get the selected employee
        User selectedEmployee = nodeTable.getSelectionModel().getSelectedItem();
        
        if (selectedEmployee == null) {
            showAlert(AlertType.WARNING, "Selection Error", "No Employee Selected", 
                    "Please select an employee to view timesheet.");
            return;
        }
        
        try {
            // Load the timesheet view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/managerViews/manager_EmployeeTime.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the selected employee
            manager_EmployeeTimeController controller = loader.getController();
            controller.setEmployee(selectedEmployee);
            
            // Create a new stage for the form
            Stage stage = new Stage();
            stage.setTitle("Employee Timesheet - " + selectedEmployee.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Show the form and wait for it to close
            stage.showAndWait();
            
            logger.info("Timesheet shown for user: " + selectedEmployee.getUserId());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading timesheet view", ex);
            showAlert(AlertType.ERROR, "Form Error", "Could not open timesheet view", 
                    "There was an error opening the timesheet view. Please try again later.");
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
     * Refresh the employee data
     */
    public void refreshData() {
        loadEmployeeData();
    }
}