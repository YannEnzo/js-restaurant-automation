package Controllers.Waiter;

import Controllers.UserAwareController;
import DAO.RestaurantService;
import Model.MenuItem;
import Model.Order;
import Model.OrderItem;
import Model.RestaurantTable;
import Model.User;
import java.io.File;
import utils.MenuImageHandler;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Controller for waiter's order view
 */
public class WaiterOrderController implements Initializable, UserAwareController {
    private static final Logger logger = Logger.getLogger(WaiterOrderController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    private RestaurantTable currentTable;
    private Order currentOrder;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    // Map to track menu items by category
    private Map<Integer, List<MenuItem>> menuItemsByCategory = new HashMap<>();
    
    @FXML private Label tableNumberLabel;
    @FXML private Label chequeLabel;
    
    @FXML private ListView<String> orderListView;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    
    @FXML private TextField tipTextField;
    @FXML private Label tip15Label;
    @FXML private Label tip18Label;
    @FXML private Label tip20Label;
    
    @FXML private Button saveButton;
    @FXML private Button payButton;
    @FXML private Button returnButton;
    
    @FXML private TabPane menuTabPane;
    @FXML private Tab appetizersTab;
    @FXML private Tab saladsTab;
    @FXML private Tab entreesTab;
    @FXML private Tab sandwichesTab;
    @FXML private Tab burgersTab;
    @FXML private Tab sidesTab;
    @FXML private Tab beveragesTab;
    
    // Container panes for each menu category
    @FXML private FlowPane appetizersPane;
    @FXML private FlowPane saladsPane;
    @FXML private FlowPane entreesPane;
    @FXML private FlowPane sandwichesPane;
    @FXML private FlowPane burgersPane;
    @FXML private FlowPane sidesPane;
    @FXML private FlowPane beveragesPane;
    
    @FXML private TextArea specialInstructionsTextArea;
    @FXML private TextField seatNumberTextField;
    
    private ObservableList<String> orderItems = FXCollections.observableArrayList();
    
    // Grid panes for each menu category
    private Map<Integer, FlowPane> categoryPanes = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize lists
        orderListView.setItems(orderItems);
        
        // Setup custom cell factory for order items for better formatting
        setupOrderListView();
        
        // Map category IDs to panes
        categoryPanes.put(1, appetizersPane);  // Appetizers
        categoryPanes.put(2, saladsPane);      // Salads
        categoryPanes.put(3, entreesPane);     // Entrees
        categoryPanes.put(4, sandwichesPane);  // Sandwiches
        categoryPanes.put(5, burgersPane);     // Burgers
        categoryPanes.put(6, sidesPane);       // Sides
        categoryPanes.put(7, beveragesPane);   // Beverages
        
        // Load menu items
        loadMenuItems();
        
        // Set up tab selection listener to update menu items
        menuTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            logger.info("Tab selected: " + newTab.getText());
        });
        
        // Set up listeners for tip field
        tipTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double tipAmount = Double.parseDouble(newValue);
                updateTotals();
            } catch (NumberFormatException e) {
                // Ignore non-numeric input
            }
        });
    }
    
    /**
     * Setup the order list view with custom cell factory
     */
    private void setupOrderListView() {
        orderListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Format special instructions and seat numbers differently
                    if (item.startsWith("* ")) {
                        setStyle("-fx-font-style: italic; -fx-text-fill: #555555;");
                    } else if (item.isEmpty()) {
                        // This is a separator line
                        setStyle("");
                    } else {
                        // This is a main menu item
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Set the current table and load its order
     */
    public void setTable(RestaurantTable table) {
        this.currentTable = table;
        
        if (tableNumberLabel != null) {
            tableNumberLabel.setText("Viewing Table " + table.getTableNumber());
        }
        
        // Load existing order for this table or create a new one
        loadOrCreateOrder();
    }
    
    /**
     * Load existing order for the current table or create a new one
     */
    private void loadOrCreateOrder() {
        try {
            // Get active orders for this table
            List<Order> tableOrders = service.getActiveOrdersByTable(currentTable.getTableId());
            
            if (tableOrders.isEmpty()) {
                // Create a new order for this table
                currentOrder = service.createOrder(currentTable.getTableId(), currentUser.getUserId());
                
                if (currentOrder == null) {
                    showAlert("Error", "Could not create a new order for this table");
                    return;
                }
            } else {
                // Use the existing order
                currentOrder = tableOrders.get(0);
            }
            
            // Update the UI with the order details
            refreshOrderDisplay();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading or creating order", ex);
            showAlert("Error", "Could not load or create order: " + ex.getMessage());
        }
    }
    
    /**
     * Load menu items and populate UI
     */
    private void loadMenuItems() {
        try {
            List<MenuItem> allItems = service.getAllMenuItems();
            
            // Group items by category
            for (MenuItem item : allItems) {
                if (item.isAvailable()) {
                    int categoryId = item.getCategoryId();
                    if (!menuItemsByCategory.containsKey(categoryId)) {
                        menuItemsByCategory.put(categoryId, new ArrayList<>());
                    }
                    menuItemsByCategory.get(categoryId).add(item);
                }
            }
            
            // Populate each category pane
            for (Map.Entry<Integer, List<MenuItem>> entry : menuItemsByCategory.entrySet()) {
                int categoryId = entry.getKey();
                List<MenuItem> items = entry.getValue();
                
                FlowPane pane = categoryPanes.get(categoryId);
                if (pane != null) {
                    populateCategoryPane(pane, items, categoryId);
                }
            }
            
            logger.info("Menu items loaded successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading menu items", ex);
            showAlert("Error", "Could not load menu items: " + ex.getMessage());
        }
    }
    
    
    /**
     * Populate a category pane with menu items
     * @param pane The FlowPane to populate
     * @param items The menu items to add
     * @param categoryId The category ID
     */
   private void populateCategoryPane(FlowPane pane, List<MenuItem> items, int categoryId) {
        // Clear existing items in the pane
        pane.getChildren().clear();

        // Set spacing and padding
        pane.setHgap(15); // Horizontal gap
        pane.setVgap(15); // Vertical gap
        pane.setPadding(new Insets(10)); // Padding

        // Loop through items and add them to the FlowPane
        for (MenuItem item : items) {
            VBox itemBox = createMenuItemBox(item); // Create VBox with image, name, price, etc.
            pane.getChildren().add(itemBox); // Add to FlowPane
        }
    }

    
    /**
     * Create a VBox with the menu item details and controls
     * @param item The MenuItem to display
     * @return A styled VBox with the item details
     */
    private VBox createMenuItemBox(MenuItem item) {
    VBox box = new VBox(8);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(10));
    box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
    box.setPrefWidth(180);
    box.setPrefHeight(240);

    // Create image view
    ImageView imageView = new ImageView();
    imageView.setFitWidth(160);
    imageView.setFitHeight(100);
    imageView.setPreserveRatio(true);

    // Load image dynamically using the item ID
    try {
        // Adjust the image path based on your folder structure
        String imagePath = "/Resources/images/menu/" + item.getItemId().toLowerCase() + ".jpg";
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl != null) {
            imageView.setImage(new Image(imageUrl.toString()));
        } else {
            // Fallback image if not found
            imageView.setImage(new Image(getClass().getResource("/Resources/images/menu/placeholder.jpg").toString()));
        }
    } catch (Exception ex) {
        logger.log(Level.WARNING, "Error loading image for " + item.getItemId(), ex);
    }

    // Create labels for item name and price
    Label nameLabel = new Label(item.getName());
    nameLabel.setWrapText(true);
    nameLabel.setMaxWidth(160);
    nameLabel.setAlignment(Pos.CENTER);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label priceLabel = new Label(currencyFormat.format(item.getPrice()));
    priceLabel.setFont(Font.font("System", 14));

    // Create quantity control
    HBox quantityBox = new HBox(8);
    quantityBox.setAlignment(Pos.CENTER);

    Button minusButton = new Button("-");
    minusButton.setPrefWidth(36);
    minusButton.setPrefHeight(36);
    minusButton.setStyle("-fx-background-color: #dddddd; -fx-font-weight: bold;");

    Label quantityLabel = new Label("0");
    quantityLabel.setMinWidth(30);
    quantityLabel.setAlignment(Pos.CENTER);
    quantityLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

    Button plusButton = new Button("+");
    plusButton.setPrefWidth(36);
    plusButton.setPrefHeight(36);
    plusButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

    quantityBox.getChildren().addAll(minusButton, quantityLabel, plusButton);

    // Create add button
    Button addButton = new Button("Add to Order");
    addButton.setPrefWidth(160);
    addButton.setStyle("-fx-background-color: #00b050; -fx-text-fill: white;");

    // Add all controls to the box
    box.getChildren().addAll(imageView, nameLabel, priceLabel, quantityBox, addButton);

    // Add event handlers
    minusButton.setOnAction(event -> {
        int quantity = Integer.parseInt(quantityLabel.getText());
        if (quantity > 0) {
            quantity--;
            quantityLabel.setText(Integer.toString(quantity));
        }
    });

    plusButton.setOnAction(event -> {
        int quantity = Integer.parseInt(quantityLabel.getText());
        quantity++;
        quantityLabel.setText(Integer.toString(quantity));
    });

    addButton.setOnAction(event -> {
        int quantity = Integer.parseInt(quantityLabel.getText());
        if (quantity > 0) {
            // Add the item to the order
            for (int i = 0; i < quantity; i++) {
                addItemToOrder(item);
            }

            // Reset the quantity
            quantityLabel.setText("0");
        }
    });

    return box;
}


    
    /**
     * Refresh the order display with current items
     */
 private void refreshOrderDisplay() {
        if (currentOrder == null) {
            return;
        }
        
        // Clear existing items
        orderItems.clear();
        
        // Add each order item to the display
        for (OrderItem item : currentOrder.getOrderItems()) {
            // Add the main item with price
            String displayString = null;
            
            // Get item name - try direct name first, then look up if needed
            if (item.getMenuItemName() != null && !item.getMenuItemName().isEmpty()) {
                displayString = item.getMenuItemName();
            } else {
                // Try to get name from the service
                MenuItem menuItem = service.getMenuItemById(item.getMenuItemId());
                if (menuItem != null) {
                    displayString = menuItem.getName();
                } else {
                    displayString = "Item #" + item.getMenuItemId();
                }
            }
            
            if (item.getQuantity() > 1) {
                displayString += " (" + item.getQuantity() + "x)";
            }
            displayString += " - " + currencyFormat.format(item.getPrice() * item.getQuantity());
            
            orderItems.add(displayString);
            
            // Add seat number if specified
            if (item.getSeatNumber() > 0) {
                orderItems.add("* Seat #" + item.getSeatNumber());
            }
            
            // Add special instructions (prefixed with *)
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
                orderItems.add("* " + item.getSpecialInstructions());
            }
            
            // Add a blank line between items for better readability
            if (currentOrder.getOrderItems().indexOf(item) < currentOrder.getOrderItems().size() - 1) {
                orderItems.add("");
            }
        }
        
        // Update the totals
        updateTotals();
    }
    
    
    /**
     * Update the subtotal, tax, and total labels
     */
    private void updateTotals() {
        if (currentOrder == null) {
            return;
        }
        
        double subtotal = currentOrder.calculateSubtotal();
        double tax = currentOrder.calculateTax();
        double total = subtotal + tax;
        
        subtotalLabel.setText("Subtotal: " + currencyFormat.format(subtotal));
        taxLabel.setText("Tax (10%): " + currencyFormat.format(tax));
        totalLabel.setText("Total: " + currencyFormat.format(total));
        
        // Calculate suggested tips
        double tip15 = total * 0.15;
        double tip18 = total * 0.18;
        double tip20 = total * 0.20;
        
        tip15Label.setText("15% = " + currencyFormat.format(tip15));
        tip18Label.setText("18% = " + currencyFormat.format(tip18));
        tip20Label.setText("20% = " + currencyFormat.format(tip20));
    }
    
    /**
     * Add an item to the order
     * @param menuItem The menu item to add
     */
    private void addItemToOrder(MenuItem menuItem) {
        if (currentOrder == null || menuItem == null) {
            return;
        }
        
        try {
            // Create a new order item
            OrderItem newItem = new OrderItem();
            newItem.setOrderId(currentOrder.getOrderId());
            newItem.setMenuItemId(menuItem.getItemId());
            newItem.setMenuItemName(menuItem.getName());
            newItem.setQuantity(1);
            newItem.setPrice(menuItem.getPrice());
            
            // Get special instructions if any
            if (specialInstructionsTextArea.getText() != null && !specialInstructionsTextArea.getText().isEmpty()) {
                newItem.setSpecialInstructions(specialInstructionsTextArea.getText());
            }
            
            // Get seat number if specified
            try {
                if (seatNumberTextField.getText() != null && !seatNumberTextField.getText().isEmpty()) {
                    int seatNumber = Integer.parseInt(seatNumberTextField.getText());
                    if (seatNumber > 0) {
                        newItem.setSeatNumber(seatNumber);
                    }
                }
            } catch (NumberFormatException e) {
                // Show warning about invalid seat number
                showAlert("Warning", "Invalid seat number. Using default.");
            }
            
            // Add the item to the order
            boolean success = service.addItemToOrder(newItem, currentOrder.getOrderId());
            
            if (success) {
                // Refresh the order to show the new item
                currentOrder = service.getOrderById(currentOrder.getOrderId());
                refreshOrderDisplay();
                
                // Clear input fields
                specialInstructionsTextArea.clear();
                seatNumberTextField.clear();
            } else {
                showAlert("Error", "Could not add item to order");
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error adding item to order", ex);
            showAlert("Error", "Could not add item to order: " + ex.getMessage());
        }
    }
    
    /**
     * Handle save button click
     */
    @FXML
    void onSave(ActionEvent event) {
        try {
            // Make sure we have items in the order
            if (currentOrder.getOrderItems().isEmpty()) {
                showAlert("Error", "Cannot save an empty order. Please add items first.");
                return;
            }
            
            // Update order status to IN_PROGRESS
            currentOrder.setStatus("PENDING");
            boolean success = service.updateOrder(currentOrder);
            
            if (success) {
                showAlert("Success", "Order saved and sent to the kitchen");
            } else {
                showAlert("Error", "Could not save order");
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error saving order", ex);
            showAlert("Error", "Could not save order: " + ex.getMessage());
        }
    }
    
    /**
     * Handle pay button click
     */
    @FXML
    void onPay(ActionEvent event) {
        try {
            // Make sure we have items in the order
            if (currentOrder.getOrderItems().isEmpty()) {
                showAlert("Error", "Cannot process payment for an empty order. Please add items first.");
                return;
            }
            
            // Make sure the order has been saved to the kitchen
            if (!"IN_PROGRESS".equals(currentOrder.getStatus()) && 
                !"READY".equals(currentOrder.getStatus()) && 
                !"DELIVERED".equals(currentOrder.getStatus()) &&
                !"PENDING".equals(currentOrder.getStatus())){
                
                showAlert("Warning", "Please save the order to the kitchen before processing payment.");
                return;
            }
            
            // Open payment screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/waiterViews/waiterPayment.fxml"));
            Parent root = loader.load();
            
            WaiterPaymentController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setOrder(currentOrder);
            
            // Get tip amount if entered
            double tipAmount = 0.0;
            try {
                if (tipTextField.getText() != null && !tipTextField.getText().isEmpty()) {
                    tipAmount = Double.parseDouble(tipTextField.getText());
                }
            } catch (NumberFormatException e) {
                // Ignore invalid tip amount
            }
            controller.setTipAmount(tipAmount);
            
            // Show payment screen
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Payment - Table " + currentTable.getTableNumber());
            stage.setScene(scene);
            stage.showAndWait();
            
            // After payment, refresh or close this window
            if (controller.isPaymentCompleted()) {
                // Close this window
                Stage currentStage = (Stage) payButton.getScene().getWindow();
                currentStage.close();
            }
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error opening payment screen", ex);
            showAlert("Error", "Could not open payment screen: " + ex.getMessage());
        }
    }
    
    /**
     * Handle return button click
     */
    @FXML
    void onReturn(ActionEvent event) {
        // Close this window
        Stage stage = (Stage) returnButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}