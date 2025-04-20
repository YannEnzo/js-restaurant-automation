package Controllers.Manager;

import com.jfoenix.controls.JFXTextField;
import DAO.RestaurantService;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Parent;

public class manager_DashboardController implements Initializable {
    private static final Logger logger = Logger.getLogger(manager_DashboardController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private LocalDate currentLocalDate = LocalDate.now();

    @FXML
    private AnchorPane homeAnchorPane;

    @FXML
    private Label profitPercent;

    @FXML
    private Label profit;

    @FXML
    private JFXTextField currentDate;

    @FXML
    private Label customerPercent;

    @FXML
    private Label customersCount;

    @FXML
    private Label staffDetail;

    @FXML
    private Label staffCount;

    @FXML
    private Label tablePercentage;

    @FXML
    private Label tableCount;

    /**
     * Initializes the controller class.
     * @param url the location used to resolve relative paths for the root object
     * @param rb the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.info("Initializing dashboard controller");
            
            // Check if UI elements are properly injected
            if (profit == null || profitPercent == null || 
                customerPercent == null || customersCount == null ||
                staffDetail == null || staffCount == null ||
                tablePercentage == null || tableCount == null) {
                
                logger.warning("One or more UI elements not properly injected. Check FXML file.");
            } else {
                logger.info("All UI elements successfully injected");
                
                // Set current date
                if (currentDate != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    currentDate.setText(java.time.LocalDate.now().format(formatter));
                    logger.info("Date set to: " + currentDate.getText());
                } else {
                    logger.warning("currentDate field is null");
                }
                
                // Load actual data
                loadDashboardData();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error initializing dashboard", ex);
        }
    }
    
    /**
     * Load actual dashboard data from the service
     */
    private void loadDashboardData() {
        try {
            logger.info("Loading dashboard data from service");
            
            // Load revenue data
            try {
                double todayRevenue = service.getDailyRevenue(currentLocalDate);
                double yesterdayRevenue = service.getDailyRevenue(currentLocalDate.minusDays(1));
                double revenueChange = 0;
                
                if (yesterdayRevenue > 0) {
                    revenueChange = ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
                }
                
                profit.setText("$" + String.format("%,.2f", todayRevenue));
                
                String changeSymbol = revenueChange >= 0 ? "↑" : "↓";
                String changeColor = revenueChange >= 0 ? "#61f84d" : "#ff5252";
                profitPercent.setText(changeSymbol + " " + String.format("%.1f", Math.abs(revenueChange)) + "% from yesterday");
                profitPercent.setStyle("-fx-text-fill: " + changeColor + ";");
                
                logger.info("Set revenue data: $" + todayRevenue + " (" + revenueChange + "% change)");
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error loading revenue data", ex);
                profit.setText("$0.00");
                profitPercent.setText("No data available");
            }
            
            // Load customer data
            try {
                int customerCount = service.getDailyCustomerCount(currentLocalDate);
                int yesterdayCount = service.getDailyCustomerCount(currentLocalDate.minusDays(1));
                double customerChange = 0;
                
                if (yesterdayCount > 0) {
                    customerChange = ((double)(customerCount - yesterdayCount) / yesterdayCount) * 100;
                }
                
                customersCount.setText(String.valueOf(customerCount));
                
                String changeSymbol = customerChange >= 0 ? "↑" : "↓";
                String changeColor = customerChange >= 0 ? "#61f84d" : "#ff5252";
                customerPercent.setText(changeSymbol + " " + String.format("%.1f", Math.abs(customerChange)) + "% from yesterday");
                customerPercent.setStyle("-fx-text-fill: " + changeColor + ";");
                
                logger.info("Set customer data: " + customerCount + " (" + customerChange + "% change)");
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error loading customer data", ex);
                customersCount.setText("0");
                customerPercent.setText("No data available");
            }
            
            // Load staff data
            try {
                Map<String, Integer> staffByRole = service.getStaffCountByRole();
                int totalStaff = 0;
                
                for (Integer count : staffByRole.values()) {
                    totalStaff += count;
                }
                
                staffCount.setText(String.valueOf(totalStaff));
                
                // Build staff details string (e.g. "5 waiters, 2 busboys, 5 chefs")
                StringBuilder staffDetails = new StringBuilder();
                
                if (staffByRole.containsKey("WAITER")) {
                    int waiterCount = staffByRole.get("WAITER");
                    staffDetails.append(waiterCount).append(" waiter");
                    if (waiterCount != 1) staffDetails.append("s");
                }
                
                if (staffByRole.containsKey("BUSBOY")) {
                    if (staffDetails.length() > 0) staffDetails.append(", ");
                    int busboyCount = staffByRole.get("BUSBOY");
                    staffDetails.append(busboyCount).append(" busboy");
                    if (busboyCount != 1) staffDetails.append("s");
                }
                
                if (staffByRole.containsKey("COOK")) {
                    if (staffDetails.length() > 0) staffDetails.append(", ");
                    int cookCount = staffByRole.get("COOK");
                    staffDetails.append(cookCount).append(" chef");
                    if (cookCount != 1) staffDetails.append("s");
                }
                
                staffDetail.setText(staffDetails.toString());
                logger.info("Set staff data: " + totalStaff + " (" + staffDetails.toString() + ")");
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error loading staff data", ex);
                staffCount.setText("0");
                staffDetail.setText("No staff data available");
            }
            
            // Load table data
            try {
                Map<String, Integer> tableStatus = service.getTableStatusSummary();
                int occupiedTables = tableStatus.getOrDefault("OCCUPIED", 0);
                int totalTables = 0;
                
                for (Integer count : tableStatus.values()) {
                    totalTables += count;
                }
                
                double occupancyRate = 0;
                if (totalTables > 0) {
                    occupancyRate = ((double) occupiedTables / totalTables) * 100;
                }
                
                tableCount.setText(occupiedTables + "/" + totalTables);
                tablePercentage.setText(String.format("%.0f%%", occupancyRate) + " Occupancy");
                
                logger.info("Set table data: " + occupiedTables + "/" + totalTables + " (" + occupancyRate + "% occupancy)");
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error loading table data", ex);
                tableCount.setText("0/0");
                tablePercentage.setText("0% Occupancy");
            }
            
            logger.info("Dashboard data loaded successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading dashboard data", e);
        }
    }

    /**
     * Navigate to employee time management screen
     * @param event ActionEvent
     */
    @FXML
    void onEmployee(ActionEvent event) {
        logger.info("Employee time button clicked");
        // This will be handled by the main controller's navigation system
    }

    /**
     * Navigate to floor plan screen
     * @param event ActionEvent
     */
    @FXML
    void onFloorPlan(ActionEvent event) {
        logger.info("Floor plan button clicked");
        // This will be handled by the main controller's navigation system
    }

    /**
     * Navigate to reports screen
     * @param event ActionEvent
     */
    @FXML
    void onReport(ActionEvent event) {
        logger.info("Report button clicked");
        // This will be handled by the main controller's navigation system
    }
    
    /**
     * Refresh dashboard data
     */
    public void refreshData() {
        loadDashboardData();
    }
}