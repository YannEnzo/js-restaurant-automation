/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Manager;

import DAO.DatabaseManager;
import DAO.TableDAO;
import DAO.UserDAO;
import DAO.MenuItemDAO;
import Model.User;
import DAO.RestaurantService;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class manager_MainController implements Initializable {
    
    private static final Logger logger = Logger.getLogger(manager_MainController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    
    @FXML
    private AnchorPane content;
    
    @FXML
    private Label userNameLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load dashboard by default when manager view is opened
        try {
            onDashboard(null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to load default dashboard view", ex);
        }
    }
    
    /**
     * Set the current user for this controller
     * @param user The logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update UI elements with user information
        if (userNameLabel != null && user != null) {
            userNameLabel.setText(user.getFullName());
        }
    }
    
    /**
     * Load the dashboard view
     * @param event Action event
     */
    @FXML
    void onDashboard(ActionEvent event) {
      /*  try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/ManagerDashboardContent.fxml"));
            AnchorPane dashboardPane = loader.load();
            
            // Get the controller and initialize it with necessary data
            ManagerDashboardController controller = loader.getController();
            controller.initializeData();
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(dashboardPane);
            
            logger.info("Dashboard view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading dashboard view", ex);
        } */
    }
    
    /**
     * Load the employee management view
     * @param event Action event
     */
    @FXML
    void onEmployees(ActionEvent event) {
      /*  try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/EmployeeManagementView.fxml"));
            AnchorPane employeePane = loader.load();
            
            // Get the controller and initialize it with staff data
            EmployeeManagementController controller = loader.getController();
            controller.getEmployeeTable().getItems().clear();
            controller.getEmployeeTable().getItems().addAll(service.getAllUsers());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(employeePane);
            
            logger.info("Employee management view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading employee management view", ex);
        } */
    }
    
    /**
     * Load the floor plan view
     * @param event Action event
     */
    
    @FXML
    void onFloorPlan(ActionEvent event) {
      /*   try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/FloorPlanView.fxml"));
            AnchorPane floorPlanPane = loader.load();
            
            // Get the controller and initialize it with table data
            FloorPlanController controller = loader.getController();
            controller.initializeFloorPlan(service.getAllTables());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(floorPlanPane);
            
            logger.info("Floor plan view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading floor plan view", ex);
        }*/
    }

    /**
     * Load the menu management view
     * @param event Action event
     */
     @FXML
    void onMenu(ActionEvent event) {
      /*   try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/MenuManagementView.fxml"));
            AnchorPane menuPane = loader.load();
            
            // Get the controller and initialize it with menu data
            MenuManagementController controller = loader.getController();
            controller.getMenuItemTable().getItems().clear();
            controller.getMenuItemTable().getItems().addAll(service.getAllMenuItems());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(menuPane);
            
            logger.info("Menu management view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading menu management view", ex);
        }*/
    }

    /**
     * Load the reporting view
     * @param event Action event
     */
    @FXML
    void onReport(ActionEvent event) {
    /*   try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/ReportingView.fxml"));
            AnchorPane reportPane = loader.load();
            
            // Get the controller and initialize it with necessary data
            ReportingController controller = loader.getController();
            controller.initializeReports();
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(reportPane);
            
            logger.info("Reporting view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading reporting view", ex);
        }*/
    }  
    
    /**
     * Load the account management view
     * @param event Action event
     */
    @FXML
    void onAccount(ActionEvent event) {
    /*   try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Manager/AccountView.fxml"));
            AnchorPane accountPane = loader.load();
            
            // Get the controller and initialize it with user data
            AccountController controller = loader.getController();
            controller.setUserData(currentUser);
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(accountPane);
            
            logger.info("Account view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading account view", ex);
        } */
    }

}