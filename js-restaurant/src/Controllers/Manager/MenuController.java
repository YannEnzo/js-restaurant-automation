package Controllers.Manager;

import Model.MenuItem;
import DAO.MenuItemDAO;
import DAO.RestaurantService;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;

public class MenuController implements Initializable {
    private static final Logger logger = Logger.getLogger(MenuController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private ObservableList<MenuItem> menuItemsList = FXCollections.observableArrayList();
    private FilteredList<MenuItem> filteredData;
    
    @FXML
    private TableView<MenuItem> menuTable;
    
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    
    @FXML
    private TableColumn<MenuItem, String> categoryColumn;
    
    @FXML
    private TableColumn<MenuItem, Double> priceColumn;
    
    @FXML
    private JFXTextField searchField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private AnchorPane menuPane;
    
    @FXML
    private HBox categoryButtonsBox;
    
    // Category filter buttons
    @FXML
    private Button allButton;
    
    @FXML
    private Button appetizersButton;
    
    @FXML
    private Button saladsButton;
    
    @FXML
    private Button entreesButton;
    
    @FXML
    private Button sandwichesButton;
    
    @FXML
    private Button sidesButton;
    
    @FXML
    private Button burgersButton;
    
    @FXML
    private Button beveragesButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(cellData -> {
            MenuItem item = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> item.getCategoryName());
        });
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Format price column to show currency
        priceColumn.setCellFactory(col -> 
            new TableCell<MenuItem, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", price));
                    }
                }
            }
        );
        // In your MenuController's initialize method, after setting up the cell value factories:

// Configure the cell factories for better text display
nameColumn.setCellFactory(col -> {
    TableCell<MenuItem, String> cell = new TableCell<MenuItem, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setWrapText(true);
            }
        }
    };
    cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    return cell;
});

categoryColumn.setCellFactory(col -> {
    TableCell<MenuItem, String> cell = new TableCell<MenuItem, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setWrapText(true);
            }
        }
    };
    cell.setAlignment(javafx.geometry.Pos.CENTER);
    return cell;
});

priceColumn.setCellFactory(col -> {
    TableCell<MenuItem, Double> cell = new TableCell<MenuItem, Double>() {
        @Override
        protected void updateItem(Double price, boolean empty) {
            super.updateItem(price, empty);
            if (empty || price == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(String.format("$%.2f", price));
                setWrapText(true);
            }
        }
    };
    cell.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
    return cell;
});
        // Load menu data
        loadMenuItems();
        
        // Set up filtering
        filteredData = new FilteredList<>(menuItemsList, p -> true);
        
        // Set up search filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(menuItem -> {
                // If search text is empty, show all items
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against name, category, or price
                if (menuItem.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (menuItem.getCategoryName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (String.valueOf(menuItem.getPrice()).contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            });
        });
        
        // Wrap the filtered list in a sorted list
        SortedList<MenuItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(menuTable.comparatorProperty());
        
        // Add data to the table
        menuTable.setItems(sortedData);
        
        // Add a placeholder when table is empty
        menuTable.setPlaceholder(new Label("No menu items found"));
        
        // Set up category buttons
        setupCategoryButtons();
        // Set column resize policy and width constraints
menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

// Set column percentages for better distribution
nameColumn.prefWidthProperty().bind(menuTable.widthProperty().multiply(0.45)); // 45% of table width
categoryColumn.prefWidthProperty().bind(menuTable.widthProperty().multiply(0.30)); // 30% of table width
priceColumn.prefWidthProperty().bind(menuTable.widthProperty().multiply(0.25)); // 25% of table width

// Prevent columns from being too narrow
nameColumn.setMinWidth(250);
categoryColumn.setMinWidth(150);
priceColumn.setMinWidth(100);

// Ensure the table has enough height for all rows
menuTable.setFixedCellSize(40); // Set a fixed height for each row
menuTable.prefHeightProperty().bind(
    menuTable.fixedCellSizeProperty().multiply(Bindings.size(menuTable.getItems()).add(1.01))
);
menuTable.setMinHeight(200); // Set a minimum height
        
        // Set up table selection for edit/delete buttons
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        // Disable edit/delete buttons initially
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        
        logger.info("Menu controller initialized");
    }
    
    /**
     * Load menu items from the service
     */
private void loadMenuItems() {
    try {
        // Clear existing data
        menuItemsList.clear();
        
        // Get all menu items
        List<MenuItem> items = service.getAllMenuItems();
        menuItemsList.addAll(items);
        
        // This is important - if you're using a FilteredList, it needs to be invalidated
        if (filteredData != null) {
            filteredData.setPredicate(filteredData.getPredicate());
        }
        
        // Force the table to refresh
        menuTable.refresh();
        
        if (menuItemsList.isEmpty()) {
            logger.info("No menu items found in the database");
        } else {
            logger.info("Loaded " + menuItemsList.size() + " menu items");
        }
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error loading menu items", ex);
        showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load menu items", 
                "There was an error loading the menu items. Please try again later.");
    }
}
    
    /**
     * Set up category filter buttons
     */
    private void setupCategoryButtons() {
        // Set up all buttons action
        allButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> true);
            setActiveButton(allButton);
        });
        
        // Set up appetizers button action
        appetizersButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Appetizers"));
            setActiveButton(appetizersButton);
        });
        
        // Set up salads button action
        saladsButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Salads"));
            setActiveButton(saladsButton);
        });
        
        // Set up entrees button action
        entreesButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Entrees"));
            setActiveButton(entreesButton);
        });
        
        // Set up sandwiches button action
        sandwichesButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Sandwiches"));
            setActiveButton(sandwichesButton);
        });
        
        // Set up sides button action
        sidesButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Sides"));
            setActiveButton(sidesButton);
        });
        
        // Set up burgers button action
        burgersButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Burgers"));
            setActiveButton(burgersButton);
        });
        
        // Set up beverages button action
        beveragesButton.setOnAction(event -> {
            filteredData.setPredicate(menuItem -> 
                menuItem.getCategoryName().equalsIgnoreCase("Beverages"));
            setActiveButton(beveragesButton);
        });
        
        // Set All as active by default
        setActiveButton(allButton);
    }
    
    /**
     * Set the active category button style
     * @param activeButton The button to set as active
     */
    private void setActiveButton(Button activeButton) {
        // Reset all buttons
        allButton.getStyleClass().remove("category-button-active");
        appetizersButton.getStyleClass().remove("category-button-active");
        saladsButton.getStyleClass().remove("category-button-active");
        entreesButton.getStyleClass().remove("category-button-active");
        sandwichesButton.getStyleClass().remove("category-button-active");
        sidesButton.getStyleClass().remove("category-button-active");
        burgersButton.getStyleClass().remove("category-button-active");
        beveragesButton.getStyleClass().remove("category-button-active");
        
        // Set active button
        activeButton.getStyleClass().add("category-button-active");
    }
    
    /**
     * Handle search event
     * @param event KeyEvent
     */
    @FXML
    void onSearch(KeyEvent event) {
        // Handled by listener in initialize()
    }
    
    /**
     * Handle search button click
     * @param event ActionEvent
     */
    @FXML
    void onSearchClick(ActionEvent event) {
        // Trigger same search logic as typing
        String searchText = searchField.getText();
        searchField.setText(searchText);
    }
    
    /**
     * Handle add menu item button click
     * @param event ActionEvent
     */
    @FXML
    void onAddMenuItem(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/Views/managerViews/addMenuItem.fxml");
            if (fxmlUrl == null) {
                logger.severe("Could not locate FXML file for add menu item");
                showAlert(Alert.AlertType.ERROR, "Resource Error", 
                        "Could not find form resource", 
                        "The application could not locate the required form.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add Menu Item");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Show the form and wait for it to close
            stage.showAndWait();
            
            // Refresh menu data
            loadMenuItems();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading add menu item form", ex);
            showAlert(Alert.AlertType.ERROR, "Form Error", 
                    "Could not open add menu item form", 
                    "There was an error opening the form: " + ex.getMessage());
        }
    }
    
    /**
     * Handle edit menu item button click
     * @param event ActionEvent
     */
    @FXML
    void onEditMenuItem(ActionEvent event) {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", 
                    "No Menu Item Selected", 
                    "Please select a menu item to edit.");
            return;
        }
        
        try {
            URL fxmlUrl = getClass().getResource("/Views/managerViews/editMenuItem.fxml");
            if (fxmlUrl == null) {
                logger.severe("Could not locate FXML file for edit menu item");
                showAlert(Alert.AlertType.ERROR, "Resource Error", 
                        "Could not find form resource", 
                        "The application could not locate the required form.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Get the controller and pass the selected menu item
            EditMenuItemController controller = loader.getController();
            controller.setMenuItem(selectedItem);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Menu Item");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Show the form and wait for it to close
            stage.showAndWait();
            
            // Refresh menu data
            loadMenuItems();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading edit menu item form", ex);
            showAlert(Alert.AlertType.ERROR, "Form Error", 
                    "Could not open edit menu item form", 
                    "There was an error opening the form: " + ex.getMessage());
        }
    }
    
    /**
     * Handle delete menu item button click
     * @param event ActionEvent
     */
    @FXML
    void onDeleteMenuItem(ActionEvent event) {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", 
                    "No Menu Item Selected", 
                    "Please select a menu item to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Menu Item");
        confirmAlert.setHeaderText("Confirm Menu Item Deletion");
        confirmAlert.setContentText("Are you sure you want to delete the menu item: " + 
                selectedItem.getName() + "?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = MenuItemDAO.getInstance().delete(selectedItem.getItemId());
                
                if (success) {
                    logger.info("Menu item deleted: " + selectedItem.getItemId());
                    loadMenuItems();
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Menu Item Deleted", 
                            "The menu item was successfully deleted.");
                    RestaurantService.getInstance().refreshMenuCache();
                    loadMenuItems();
                } else {
                    logger.warning("Failed to delete menu item: " + selectedItem.getItemId());
                    showAlert(Alert.AlertType.ERROR, "Delete Error", 
                            "Could not delete menu item", 
                            "There was an error deleting the menu item. Please try again later.");
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Database error deleting menu item", ex);
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                        "Could not delete menu item", 
                        "There was a database error deleting the menu item: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Show an alert dialog
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header text
     * @param content Alert content text
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Refresh the menu data
     */
    public void refreshData() {
        loadMenuItems();
    }
}