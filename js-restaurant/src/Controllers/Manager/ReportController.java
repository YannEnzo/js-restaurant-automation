package Controllers.Manager;

import Model.*;
import DAO.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXTextField;
import javafx.util.StringConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportController implements Initializable {
    private static final Logger logger = Logger.getLogger(ReportController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    
    // Date range options
    private enum DateRange {
        LAST_7_DAYS("Last 7 Days"),
        LAST_30_DAYS("Last 30 Days"),
        LAST_24_HOURS("Last 24 Hours");
        
        private final String displayName;
        
        DateRange(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // FXML controls
    @FXML
    private ComboBox<DateRange> dateRangeCombo;
    
    @FXML
    private TabPane reportTabPane;
    
    @FXML
    private Tab topSellersTab;
    
    @FXML
    private Tab inventoryTab;
    
    @FXML
    private Tab kitchenPerformanceTab;
    
    @FXML
    private AnchorPane salesChartContainer;
    
    @FXML
    private Button exportTopSellersBtn;
    
    @FXML
    private TableView<InventoryItem> inventoryTable;
    
    @FXML
    private TableColumn<InventoryItem, String> itemNameColumn;
    
    @FXML
    private TableColumn<InventoryItem, String> categoryColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> currentStockColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> minRequiredColumn;
    
    @FXML
    private TableColumn<InventoryItem, String> statusColumn;
    
    @FXML
    private TableColumn<InventoryItem, Button> actionsColumn;
    
    @FXML
    private ComboBox<String> categoryFilterCombo;
    
    @FXML
    private ComboBox<String> statusFilterCombo;
    
    @FXML
    private ComboBox<String> searchInventoryField;
    
    @FXML
    private Button orderSuppliesBtn;
    
    @FXML
    private Label lowStockCount;
    
    @FXML
    private Label warningCount;
    
    @FXML
    private Label pendingOrdersCount;
    
    @FXML
    private Button exportInventoryBtn;
    
    @FXML
    private TableView<KitchenPerformanceItem> kitchenPerformanceTable;
    
    @FXML
    private TableColumn<KitchenPerformanceItem, String> menuItemColumn;
    
    @FXML
    private TableColumn<KitchenPerformanceItem, String> menuCategoryColumn;
    
    @FXML
    private TableColumn<KitchenPerformanceItem, String> avgPrepTimeColumn;
    
    @FXML
    private TableColumn<KitchenPerformanceItem, String> targetTimeColumn;
    
    @FXML
    private TableColumn<KitchenPerformanceItem, String> prepStatusColumn;
    
    @FXML
    private ComboBox<String> kitchenCategoryCombo;
    
    @FXML
    private ComboBox<String> kitchenStaffCombo;
    
    @FXML
    private Button exportKitchenBtn;
    
    // Data models
   @FXML
private final ObservableList<Model.MenuItem> topSellingItems = FXCollections.observableArrayList();
    private ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
    private ObservableList<KitchenPerformanceItem> kitchenPerformanceItems = FXCollections.observableArrayList();
    
    // Helper data classes
    public static class InventoryItem {
        private final String itemId;
        private final String name;
        private final String category;
        private final double currentStock;
        private final double minRequired;
        private final boolean isLow;
        
        public InventoryItem(String itemId, String name, String category, double currentStock, double minRequired, boolean isLow) {
            this.itemId = itemId;
            this.name = name;
            this.category = category;
            this.currentStock = currentStock;
            this.minRequired = minRequired;
            this.isLow = isLow;
        }
        
        public String getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getCurrentStock() { return currentStock; }
        public double getMinRequired() { return minRequired; }
        public boolean isLow() { return isLow; }
        public String getStatus() { return isLow ? "Low" : "OK"; }
    }
    
    public static class KitchenPerformanceItem {
        private final String itemId;
        private final String name;
        private final String category;
        private final String avgPrepTime;
        private final String targetTime;
        private final String status;
        
        public KitchenPerformanceItem(String itemId, String name, String category, String avgPrepTime, String targetTime, String status) {
            this.itemId = itemId;
            this.name = name;
            this.category = category;
            this.avgPrepTime = avgPrepTime;
            this.targetTime = targetTime;
            this.status = status;
        }
        
        public String getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getAvgPrepTime() { return avgPrepTime; }
        public String getTargetTime() { return targetTime; }
        public String getStatus() { return status; }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.info("Initializing report controller");
            
            // Set up date range combo
            setupDateRangeCombo();
            
            // Initialize tabs
            setupTopSellersTab();
            setupInventoryTab();
            setupKitchenPerformanceTab();
            
            // Add listeners for tab changes
            reportTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == topSellersTab) {
                    loadTopSellersData();
                } else if (newValue == inventoryTab) {
                    loadInventoryData();
                } else if (newValue == kitchenPerformanceTab) {
                    loadKitchenPerformanceData();
                }
            });
            
            // Load initial data for the first tab
            loadTopSellersData();
            
            logger.info("Report controller initialized successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error initializing report controller", ex);
        }
    }
    
    private void setupDateRangeCombo() {
        dateRangeCombo.getItems().setAll(DateRange.values());
        dateRangeCombo.setValue(DateRange.LAST_30_DAYS);
        
        dateRangeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshActiveTab();
            }
        });
    }
    
    private void refreshActiveTab() {
        Tab selectedTab = reportTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == topSellersTab) {
            loadTopSellersData();
        } else if (selectedTab == inventoryTab) {
            loadInventoryData();
        } else if (selectedTab == kitchenPerformanceTab) {
            loadKitchenPerformanceData();
        }
    }
    
    private void setupTopSellersTab() {
        // We'll create a bar chart for top sellers in the loadTopSellersData method
    }
    
    private void setupInventoryTab() {
        // Set up inventory table columns
        itemNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        categoryColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategory()));
        
        currentStockColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getCurrentStock()).asObject());
        
        minRequiredColumn.setCellValueFactory(cellData ->
            new SimpleDoubleProperty(cellData.getValue().getMinRequired()).asObject());
        
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Set up inventory filters
        setupInventoryFilters();
    }
    
    private void setupInventoryFilters() {
        // Initialize category filter
        categoryFilterCombo.getItems().add("All Categories");
        categoryFilterCombo.setValue("All Categories");
        
        // Initialize status filter
        statusFilterCombo.getItems().addAll("All Status", "Low", "OK");
        statusFilterCombo.setValue("All Status");
        
        // Add listeners for filters
        categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadInventoryData());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadInventoryData());
    }
    
    private void setupKitchenPerformanceTab() {
        // Set up kitchen performance table columns
        menuItemColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getName()));
        
        menuCategoryColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategory()));
        
        avgPrepTimeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAvgPrepTime()));
        
        targetTimeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getTargetTime()));
        
        prepStatusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Set up kitchen filters
        setupKitchenFilters();
    }
    
    private void setupKitchenFilters() {
        // Initialize category filter
        kitchenCategoryCombo.getItems().add("All Categories");
        kitchenCategoryCombo.setValue("All Categories");
        
        // Initialize staff filter
        kitchenStaffCombo.getItems().add("All Staff");
        kitchenStaffCombo.setValue("All Staff");
        
        // Add listeners for filters
        kitchenCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadKitchenPerformanceData());
        kitchenStaffCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadKitchenPerformanceData());
    }
    
    private void loadTopSellersData() {
        try {
            // Clear existing chart
            salesChartContainer.getChildren().clear();
            
            // Get date range
            LocalDate startDate = getStartDateFromSelection();
            LocalDate endDate = LocalDate.now();
            
            // Load top sellers data
            List<TopSellerData> topSellers = service.getTopSellers(startDate, endDate, 6);
            
            // Create bar chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            
            xAxis.setLabel("Menu Item");
            yAxis.setLabel("Orders");
            
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Top Selling Items");
            
            // Create data series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Number of Orders");
            
            for (TopSellerData data : topSellers) {
                series.getData().add(new XYChart.Data<>(data.getName(), data.getOrderCount()));
            }
            
            barChart.getData().add(series);
            barChart.setLegendVisible(false);
            
            // Style the chart
            barChart.getStyleClass().add("sales-chart");
            
            // Add to container
            barChart.setPrefWidth(salesChartContainer.getPrefWidth());
            barChart.setPrefHeight(salesChartContainer.getPrefHeight());
            salesChartContainer.getChildren().add(barChart);
            
            logger.info("Loaded top sellers data with " + topSellers.size() + " items");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading top sellers data", ex);
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load top sellers data", 
                    "There was an error loading the top sellers data: " + ex.getMessage());
        }
    }
    
    private void loadInventoryData() {
        try {
            // Get inventory data with filters
            String categoryFilter = categoryFilterCombo.getValue();
            String statusFilter = statusFilterCombo.getValue();
            
            List<InventoryItem> items = service.getInventoryItems(
                categoryFilter.equals("All Categories") ? null : categoryFilter,
                statusFilter.equals("All Status") ? null : statusFilter
            );
            
            // Update the table
            inventoryItems.setAll(items);
            inventoryTable.setItems(inventoryItems);
            
            // Update summary counts
            int lowCount = 0;
            int warningCount = 0;
            
            for (InventoryItem item : items) {
                if (item.isLow()) {
                    lowCount++;
                } else if (item.getCurrentStock() < item.getMinRequired() * 1.2) {
                    warningCount++;
                }
            }
            
            this.lowStockCount.setText(String.valueOf(lowCount));
            this.warningCount.setText(String.valueOf(warningCount));
            this.pendingOrdersCount.setText("0"); // This would need to be fetched from a purchase order table
            
            logger.info("Loaded inventory data with " + items.size() + " items");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading inventory data", ex);
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load inventory data", 
                    "There was an error loading the inventory data: " + ex.getMessage());
        }
    }
    
    private void loadKitchenPerformanceData() {
        try {
            // Get kitchen performance data with filters
            String categoryFilter = kitchenCategoryCombo.getValue();
            String staffFilter = kitchenStaffCombo.getValue();
            
            List<KitchenPerformanceItem> items = service.getKitchenPerformanceData(
                categoryFilter.equals("All Categories") ? null : categoryFilter,
                staffFilter.equals("All Staff") ? null : staffFilter
            );
            
            // Update the table
            kitchenPerformanceItems.setAll(items);
            kitchenPerformanceTable.setItems(kitchenPerformanceItems);
            
            logger.info("Loaded kitchen performance data with " + items.size() + " items");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading kitchen performance data", ex);
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load kitchen performance data", 
                    "There was an error loading the kitchen performance data: " + ex.getMessage());
        }
    }
    
    private LocalDate getStartDateFromSelection() {
        LocalDate now = LocalDate.now();
        DateRange selectedRange = dateRangeCombo.getValue();
        
        if (selectedRange == DateRange.LAST_24_HOURS) {
            return now.minusDays(1);
        } else if (selectedRange == DateRange.LAST_7_DAYS) {
            return now.minusDays(7);
        } else {
            return now.minusDays(30); // Default: Last 30 days
        }
    }
    
    @FXML
    void onExportReport(ActionEvent event) {
        try {
            Tab selectedTab = reportTabPane.getSelectionModel().getSelectedItem();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            if (selectedTab == topSellersTab) {
                fileChooser.setInitialFileName("top_sellers_report.csv");
                File file = fileChooser.showSaveDialog(null);
                
                if (file != null) {
                    exportTopSellersReport(file);
                }
            } else if (selectedTab == inventoryTab) {
                fileChooser.setInitialFileName("inventory_report.csv");
                File file = fileChooser.showSaveDialog(null);
                
                if (file != null) {
                    exportInventoryReport(file);
                }
            } else if (selectedTab == kitchenPerformanceTab) {
                fileChooser.setInitialFileName("kitchen_performance_report.csv");
                File file = fileChooser.showSaveDialog(null);
                
                if (file != null) {
                    exportKitchenPerformanceReport(file);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error exporting report", ex);
            showAlert(Alert.AlertType.ERROR, "Export Error", "Could not export report", 
                    "There was an error exporting the report: " + ex.getMessage());
        }
    }
    
    private void exportTopSellersReport(File file) throws IOException {
        LocalDate startDate = getStartDateFromSelection();
        LocalDate endDate = LocalDate.now();
        
        List<TopSellerData> topSellers = service.getTopSellers(startDate, endDate, 20);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Menu Item,Category,Orders,Revenue\n");
            
            for (TopSellerData data : topSellers) {
                writer.write(String.format("\"%s\",\"%s\",%d,%.2f\n", 
                        data.getName(), 
                        data.getCategory(),
                        data.getOrderCount(),
                        data.getRevenue()));
            }
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Export Success", "Report Exported", 
                "The report was successfully exported to " + file.getAbsolutePath());
    }
    
    private void exportInventoryReport(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Item Name,Category,Current Stock,Minimum Required,Status\n");
            
            for (InventoryItem item : inventoryItems) {
                writer.write(String.format("\"%s\",\"%s\",%.2f,%.2f,\"%s\"\n", 
                        item.getName(),
                        item.getCategory(),
                        item.getCurrentStock(),
                        item.getMinRequired(),
                        item.getStatus()));
            }
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Export Success", "Report Exported", 
                "The report was successfully exported to " + file.getAbsolutePath());
    }
    
    private void exportKitchenPerformanceReport(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Menu Item,Category,Average Preparation Time,Target Time,Status\n");
            
            for (KitchenPerformanceItem item : kitchenPerformanceItems) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n", 
                        item.getName(),
                        item.getCategory(),
                        item.getAvgPrepTime(),
                        item.getTargetTime(),
                        item.getStatus()));
            }
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Export Success", "Report Exported", 
                "The report was successfully exported to " + file.getAbsolutePath());
    }
    
    @FXML
    void onOrderSupplies(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Order Supplies", "Feature Not Implemented", 
                "The order supplies feature is not yet implemented.");
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Helper data class for top sellers
    public static class TopSellerData {
        private final String itemId;
        private final String name;
        private final String category;
        private final int orderCount;
        private final double revenue;
        
        public TopSellerData(String itemId, String name, String category, int orderCount, double revenue) {
            this.itemId = itemId;
            this.name = name;
            this.category = category;
            this.orderCount = orderCount;
            this.revenue = revenue;
        }
        
        public String getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getOrderCount() { return orderCount; }
        public double getRevenue() { return revenue; }
    }
}