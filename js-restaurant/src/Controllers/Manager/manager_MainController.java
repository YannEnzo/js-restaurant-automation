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
        logger.info("Manager Main Controller initialized");
        
        // Load dashboard as default view on initialization
        try {
            onDashboard(null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to load dashboard on initialization", ex);
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
            logger.info("User name set to: " + user.getFullName());
        } else {
            logger.warning("userNameLabel is null or user is null");
        }
    }
    
    /**
     * Load the dashboard view
     * @param event Action event
     */
    @FXML
    void onDashboard(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load dashboard.");
                return;
            }
            
            // Load the dashboard view
            URL dashboardUrl = getClass().getResource("/Views/managerViews/manager_Dashboard.fxml");
            if (dashboardUrl == null) {
                logger.severe("Dashboard FXML file not found");
                return;
            }
            
            logger.info("Loading dashboard from: " + dashboardUrl);
            
            FXMLLoader loader = new FXMLLoader(dashboardUrl);
            AnchorPane dashboardPane = loader.load();
            
            // Get the controller
            manager_DashboardController dashboardController = loader.getController();
            
            // Clear existing content and add new content
            content.getChildren().clear();
            
            // Manually set size to match parent container
            dashboardPane.prefWidthProperty().bind(content.widthProperty());
            dashboardPane.prefHeightProperty().bind(content.heightProperty());
            
            content.getChildren().add(dashboardPane);
            
            logger.info("Dashboard view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading dashboard view", ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error loading dashboard view", ex);
        }
    }
    
    /**
     * Load the employee management view
     * @param event Action event
     */
    @FXML
    void onEmployees(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load employee view.");
                return;
            }
            
            // Based on the project structure shown, use the correct path
            String fxmlPath = "/Views/managerViews/manager_Employeeview.fxml";
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.severe("Employee management FXML file not found at: " + fxmlPath);
                return;
            }
            
            logger.info("Loading employee management from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane employeePane = loader.load();
            
            // Set appropriate size
            employeePane.prefWidthProperty().bind(content.widthProperty());
            employeePane.prefHeightProperty().bind(content.heightProperty());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(employeePane);
            
            logger.info("Employee management view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading employee management view", ex);
        }
    }
    
    /**
     * Load the floor plan view
     * @param event Action event
     */
    @FXML
    void onFloorPlan(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load floor plan view.");
                return;
            }
            
            // Based on the project structure shown
            String fxmlPath = "/Views/managerViews/managerFloorPlan.fxml";
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.severe("Floor plan FXML file not found at: " + fxmlPath);
                return;
            }
            
            logger.info("Loading floor plan from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane floorPlanPane = loader.load();
            
            // Set appropriate size
            floorPlanPane.prefWidthProperty().bind(content.widthProperty());
            floorPlanPane.prefHeightProperty().bind(content.heightProperty());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(floorPlanPane);
            
            logger.info("Floor plan view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading floor plan view", ex);
        }
    }

    /**
     * Load the menu management view
     * @param event Action event
     */
    @FXML
    void onMenu(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load menu view.");
                return;
            }
            
            // Based on the project structure
            String fxmlPath = "/Views/managerViews/menuItems.fxml";
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.severe("Menu items FXML file not found at: " + fxmlPath);
                return;
            }
            
            logger.info("Loading menu management from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane menuPane = loader.load();
            
            // Set appropriate size
            menuPane.prefWidthProperty().bind(content.widthProperty());
            menuPane.prefHeightProperty().bind(content.heightProperty());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(menuPane);
            
            logger.info("Menu management view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading menu management view", ex);
        }
    }

    /**
     * Load the reporting view
     * @param event Action event
     */
    @FXML
    void onReport(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load report view.");
                return;
            }
            
            // Based on the project structure
            String fxmlPath = "/Views/managerViews/report-view.fxml";
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.severe("Reporting FXML file not found at: " + fxmlPath);
                return;
            }
            
            logger.info("Loading reporting view from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane reportPane = loader.load();
            
            // Set appropriate size
            reportPane.prefWidthProperty().bind(content.widthProperty());
            reportPane.prefHeightProperty().bind(content.heightProperty());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(reportPane);
            
            logger.info("Reporting view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading reporting view", ex);
        }
    }
    
    /**
     * Load the account management view
     * @param event Action event
     */
    @FXML
    void onAccount(ActionEvent event) {
        try {
            // Check if the content pane is available
            if (content == null) {
                logger.warning("Content pane is null. Cannot load account view.");
                return;
            }
            
            // Based on the project structure
            String fxmlPath = "/Views/managerViews/account-view.fxml";
            
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.severe("Account FXML file not found at: " + fxmlPath);
                return;
            }
            
            logger.info("Loading account view from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane accountPane = loader.load();
            
            // Set appropriate size
            accountPane.prefWidthProperty().bind(content.widthProperty());
            accountPane.prefHeightProperty().bind(content.heightProperty());
            
            // Clear existing content and add new content
            content.getChildren().clear();
            content.getChildren().add(accountPane);
            
            logger.info("Account view loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading account view", ex);
        }
    }
}