package Controllers.cook;

import Components.OrderCardComponent;
import Components.OrderDetailsPopup;
import Controllers.UserAwareController;
import Controllers.Waiter.WaiterAccountController;
import DAO.RestaurantService;
import DAO.TableStatusObserver;
import Model.Order;
import Model.OrderItem;
import Model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;

/**
 * Controller for the Kitchen View
 */
public class CookViewController implements Initializable, TableStatusObserver,UserAwareController  {
    
    private static final Logger logger = Logger.getLogger(CookViewController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    
    
    // Map to store timers for orders in preparation
    private final Map<String, Timeline> orderTimers = new HashMap<>();
    
    // Constants for timer thresholds (in minutes)
    private static final int WARNING_THRESHOLD = 10;
    private static final int CRITICAL_THRESHOLD = 15;
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private Label timeLabel;
    
    @FXML
    private Label cookNameLabel;
    
    @FXML
    private JFXButton accountBtn;
    
    @FXML
    private TabPane orderTabPane;
    
    @FXML
    private Tab pendingTab;
    
    @FXML
    private Tab inProgressTab;
    
    @FXML
    private Tab readyTab;
    
    @FXML
    private VBox pendingOrdersContainer;
    
    @FXML
    private VBox inProgressOrdersContainer;
    
    @FXML
    private VBox readyOrdersContainer;
    
    @FXML
    private Label pendingOrdersCount;
    
    @FXML
    private Label inPrepCount;
    
    @FXML
    private Label readyOrdersCount;
    
    @FXML
    private Label avgPrepTime;
    
    private static User pendingUser = null;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.info("Initializing kitchen view controller");
            
            // Register as observer for table status changes
            service.addTableStatusObserver(this);
            // Apply the pending user if available
            if (pendingUser != null) {
                System.out.println("Using pending user: " + pendingUser.getUserId());
                setCurrentUser(pendingUser);
                pendingUser = null; // Clear it after use
            } else {
                System.out.println("No pending user available during initialization");
            }
            
            // Register as observer for table status changes
            service.addTableStatusObserver(this);
            // Start clock update timer
            startClockTimer();
            
            // Load orders
            loadOrders();
            
            logger.info("Kitchen view controller initialized successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error initializing kitchen view controller", ex);
            showAlert(AlertType.ERROR, "Initialization Error", 
                    "Could not initialize kitchen view", ex.getMessage());
        }
    }
    
    /**
     * Set the current user for this controller
     * @param user The current user
     */
public void setCurrentUser(User user) {
        System.out.println("KitchenViewController.setCurrentUser called with: " + 
            (user != null ? user.getUserId() : "null"));
        
        this.currentUser = user;
        
        if (user != null) {
            logger.info("Current user set to: " + user.getFullName());
            
            // Update UI with user information (using Platform.runLater to handle JavaFX thread safety)
            Platform.runLater(() -> {
                if (cookNameLabel != null) {
                    cookNameLabel.setText(user.getFullName());
                    System.out.println("Updated cookNameLabel with user: " + user.getFullName());
                } else {
                    System.out.println("WARNING: cookNameLabel is null!");
                }
                
                // Load initial orders
                loadOrders();
                
                // Start timer to periodically refresh orders
                startOrderRefreshTimer();
            });
        } else {
            logger.warning("Attempted to set null user");
        }
    }
        /**
     * Static method to set the user before the controller is created
     * @param user User to set
     */
    public static void setPendingUser(User user) {
        System.out.println("Setting pending user: " + (user != null ? user.getUserId() : "null"));
        pendingUser = user;
    }
    
    /**
     * Start a timer to update the clock display
     */
    private void startClockTimer() {
        Timeline clockTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            if (timeLabel != null) {
                timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        logger.info("Clock timer started");
    }
    
    /**
     * Start a timer to periodically refresh orders
     */
    private void startOrderRefreshTimer() {
        Timeline refreshTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(30), e -> {
            // Refresh orders every 30 seconds
            loadOrders();
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
        logger.info("Order refresh timer started");
    }
    
    /**
     * Load orders from the service
     */
    private void loadOrders() {
        try {
            logger.info("Loading orders from database");
            
            // Clear existing containers
            Platform.runLater(() -> {
                if (pendingOrdersContainer != null) {
                    pendingOrdersContainer.getChildren().clear();
                }
                
                if (inProgressOrdersContainer != null) {
                    inProgressOrdersContainer.getChildren().clear();
                }
                
                if (readyOrdersContainer != null) {
                    readyOrdersContainer.getChildren().clear();
                }
            });
            
            // Get active orders from database
            List<Order> activeOrders = service.getActiveOrders();
            
            if (activeOrders.isEmpty()) {
                logger.info("No active orders found in database");
                Platform.runLater(() -> {
                    // Update count labels to 0
                    if (pendingOrdersCount != null) {
                        pendingOrdersCount.setText("0");
                    }
                    if (inPrepCount != null) {
                        inPrepCount.setText("0");
                    }
                    if (readyOrdersCount != null) {
                        readyOrdersCount.setText("0");
                    }
                });
                return;
            }
            
            int pendingCount = 0;
            int inPrepCount = 0;
            int readyCount = 0;
            
            logger.info("Processing " + activeOrders.size() + " active orders");
            
            for (Order order : activeOrders) {
                final Order finalOrder = order;
                
                // Skip orders that are not in relevant statuses
                if (!order.getStatus().equals("PENDING") && 
                    !order.getStatus().equals("IN_PROGRESS") && 
                    !order.getStatus().equals("READY")) {
                    continue;
                }


                // Check order status and create appropriate card
                Platform.runLater(() -> {
                    try {
                        switch(finalOrder.getStatus()) {
                            case "PENDING":
                                createOrderCard(finalOrder, pendingOrdersContainer, "PENDING");
                                break;
                            case "IN_PROGRESS":
                                createOrderCard(finalOrder, inProgressOrdersContainer, "IN_PROGRESS");
                                break;
                            case "READY":
                                createOrderCard(finalOrder, readyOrdersContainer, "READY");
                                break;
                            default:
                                logger.warning("Unknown order status: " + finalOrder.getStatus());
                                break;
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error creating order card", ex);
                    }
                });
                
                // Count orders by status
                switch(order.getStatus()) {
                    case "PENDING": pendingCount++; break;
                    case "IN_PROGRESS": inPrepCount++; break;
                    case "READY": readyCount++; break;
                }
            }
            
            // Update counts
            final int finalPendingCount = pendingCount;
            final int finalInPrepCount = inPrepCount;
            final int finalReadyCount = readyCount;
            
            Platform.runLater(() -> {
                if (pendingOrdersCount != null) {
                    pendingOrdersCount.setText(String.valueOf(finalPendingCount));
                }
                if (this.inPrepCount != null) {
                    this.inPrepCount.setText(String.valueOf(finalInPrepCount));
                }
                if (readyOrdersCount != null) {
                    readyOrdersCount.setText(String.valueOf(finalReadyCount));
                }
            });
            
            // Update average prep time
            updateAveragePreparationTime();
            
            logger.info("Orders loaded - Pending: " + pendingCount + 
                      ", In Progress: " + inPrepCount + 
                      ", Ready: " + readyCount);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading orders", ex);
            showAlert(AlertType.ERROR, "Data Error", 
                    "Could not load orders", ex.getMessage());
        }
    }
    
    /**
     * Create an order card for display using the OrderCardComponent
     * @param order The order to display
     * @param container The container to add the card to
     * @param cardType The type of card (PENDING, IN_PROGRESS, READY)
     */
    private void createOrderCard(Order order, VBox container, String cardType) {
        if (container == null) {
            logger.warning("Cannot create order card - container is null");
            return;
        }
        
        try {
            // Create order card component
            OrderCardComponent orderCard = new OrderCardComponent(order, cardType);
            
            // Set handlers
            orderCard.setOnStartPreparation(() -> startPreparation(order));
            orderCard.setOnMarkAsReady(() -> markAsReady(order));
            orderCard.setOnClearOrder(() -> clearOrder(order));
            
            // Add double-click handler to show details
            orderCard.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    showOrderDetails(order);
                }
            });
            
            // Add card to container
            container.getChildren().add(orderCard);
            logger.fine("Added order card to container: " + order.getOrderId());
            
            // Start timer if the order is in progress
            if ("IN_PROGRESS".equals(cardType)) {
                startOrderTimer(order, orderCard.getTimerLabel());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error creating order card for order: " + order.getOrderId(), ex);
        }
    }
    
    /**
     * Show detailed order information in a popup
     * @param order The order to show details for
     */
    private void showOrderDetails(Order order) {
        try {
            OrderDetailsPopup popup = new OrderDetailsPopup(order);
            popup.show();
            logger.info("Showing details for order: " + order.getOrderId());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error showing order details", ex);
            showAlert(AlertType.ERROR, "View Error", 
                    "Could not show order details", ex.getMessage());
        }
    }
    
    /**
     * Start a timer for an order in preparation
     * @param order The order to track
     * @param timerLabel The label to update
     */
    private void startOrderTimer(Order order, Label timerLabel) {
        // Check if timer already exists
        if (orderTimers.containsKey(order.getOrderId())) {
            Timeline existingTimer = orderTimers.get(order.getOrderId());
            existingTimer.stop();
            orderTimers.remove(order.getOrderId());
        }
        
        if (timerLabel == null) {
            logger.warning("Cannot start timer - timer label is null");
            return;
        }
        
        // Calculate start time
        LocalDateTime startTime = null;
        for (OrderItem item : order.getOrderItems()) {
            if (item.getPreparationStartTime() != null) {
                if (startTime == null || item.getPreparationStartTime().isBefore(startTime)) {
                    startTime = item.getPreparationStartTime();
                }
            }
        }
        
        // If no start time found, use current time
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        
        // Store start time for use in the timer
        final LocalDateTime timerStartTime = startTime;
        
        // Create a new timeline to update the timer
        Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            // Calculate elapsed time
            java.time.Duration duration = java.time.Duration.between(timerStartTime, LocalDateTime.now());
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            
            // Update timer label
            String timeText = String.format("%02d:%02d", minutes, seconds);
            timerLabel.setText(timeText);
            
            // Update timer color based on thresholds
            if (minutes >= CRITICAL_THRESHOLD) {
                timerLabel.getStyleClass().removeAll("order-timer-normal", "order-timer-warning");
                timerLabel.getStyleClass().add("order-timer-critical");
            } else if (minutes >= WARNING_THRESHOLD) {
                timerLabel.getStyleClass().removeAll("order-timer-normal", "order-timer-critical");
                timerLabel.getStyleClass().add("order-timer-warning");
            } else {
                timerLabel.getStyleClass().removeAll("order-timer-warning", "order-timer-critical");
                timerLabel.getStyleClass().add("order-timer-normal");
            }
        }));
        
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        // Store the timeline
        orderTimers.put(order.getOrderId(), timeline);
        logger.info("Started timer for order: " + order.getOrderId());
    }
    
    /**
     * Stop and remove a timer for an order
     * @param orderId The order ID to stop timer for
     */
    private void stopOrderTimer(String orderId) {
        Timeline timeline = orderTimers.get(orderId);
        if (timeline != null) {
            timeline.stop();
            orderTimers.remove(orderId);
            logger.info("Stopped timer for order: " + orderId);
        }
    }
    
    /**
     * Handle start preparation button
     * @param order The order to start preparation for
     */
    private void startPreparation(Order order) {
        try {
            logger.info("Starting preparation for order: " + order.getOrderId());
            
            // Update order status
            order.setStatus("IN_PROGRESS");
            
            // Update order items status
            List<OrderItem> items = order.getOrderItems();
            LocalDateTime now = LocalDateTime.now();
            
            for (OrderItem item : items) {
                item.setStatus("IN_PREPARATION");
                item.setPreparationStartTime(now);
            }
            
            // Update order in database
            boolean success = service.updateOrder(order);
            
            if (success) {
                logger.info("Order status updated to IN_PROGRESS in database");
                
                // Refresh orders
                loadOrders();
                
                // Switch to In Preparation tab
                Platform.runLater(() -> {
                    orderTabPane.getSelectionModel().select(inProgressTab);
                });
            } else {
                logger.warning("Failed to update order status in database");
                showAlert(AlertType.WARNING, "Database Error", 
                        "Could not update order status", 
                        "The order status could not be updated in the database.");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting preparation for order: " + order.getOrderId(), ex);
            showAlert(AlertType.ERROR, "Processing Error", 
                    "Could not start preparation", ex.getMessage());
        }
    }
    
    /**
     * Handle mark as ready button
     * @param order The order to mark as ready
     */
    private void markAsReady(Order order) {
        try {
            logger.info("Marking order as ready: " + order.getOrderId());
            
            // Update order status
            order.setStatus("READY");
            
            // Update order items status
            List<OrderItem> items = order.getOrderItems();
            LocalDateTime now = LocalDateTime.now();
            
            for (OrderItem item : items) {
                item.setStatus("READY");
                item.setCompletionTime(now);
            }
            
            // Update order in database
            boolean success = service.updateOrder(order);
            
            if (success) {
                logger.info("Order status updated to READY in database");
                
                // Stop the timer
                stopOrderTimer(order.getOrderId());
                
                // Refresh orders
                loadOrders();
                
                // Switch to Ready tab
                Platform.runLater(() -> {
                    orderTabPane.getSelectionModel().select(readyTab);
                });
            } else {
                logger.warning("Failed to update order status in database");
                showAlert(AlertType.WARNING, "Database Error", 
                        "Could not update order status", 
                        "The order status could not be updated in the database.");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error marking order as ready: " + order.getOrderId(), ex);
            showAlert(AlertType.ERROR, "Processing Error", 
                    "Could not mark order as ready", ex.getMessage());
        }
    }
    
    /**
     * Handle clear order button
     * @param order The order to clear
     */
    private void clearOrder(Order order) {
        try {
            logger.info("Clearing order: " + order.getOrderId());
            
            // Update order status to DELIVERED
            order.setStatus("DELIVERED");
            
            // Update order items status
            List<OrderItem> items = order.getOrderItems();
            for (OrderItem item : items) {
                item.setStatus("DELIVERED");
            }
            
            // Update order in database
            boolean success = service.updateOrder(order);
            
            if (success) {
                logger.info("Order status updated to DELIVERED in database");
                
                // Refresh orders
                loadOrders();
            } else {
                logger.warning("Failed to update order status in database");
                showAlert(AlertType.WARNING, "Database Error", 
                        "Could not update order status", 
                        "The order status could not be updated in the database.");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error clearing order: " + order.getOrderId(), ex);
            showAlert(AlertType.ERROR, "Processing Error", 
                    "Could not clear order", ex.getMessage());
        }
    }
    
    /**
     * Update the average preparation time displayed
     */
    private void updateAveragePreparationTime() {
        try {
            // Calculate actual average prep time from orders with completion times
            double totalMinutes = 0;
            int count = 0;
            
            // Get active orders
            List<Order> orders = service.getActiveOrders();
            
            // Calculate average preparation time from completed items
            for (Order order : orders) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getPreparationStartTime() != null && item.getCompletionTime() != null) {
                        Duration prepTime = Duration.between(
                                item.getPreparationStartTime(), 
                                item.getCompletionTime());
                        
                        totalMinutes += prepTime.toMinutes();
                        count++;
                    }
                }
            }
            
            // Calculate average
            final double avgTime = count > 0 ? totalMinutes / count : 0;
            
            // Update the UI
            Platform.runLater(() -> {
                if (avgPrepTime != null) {
                    if (avgTime > 0) {
                        avgPrepTime.setText(String.format("%.1f", avgTime));
                    } else {
                        avgPrepTime.setText("--");
                    }
                }
            });
            
            logger.info("Updated average preparation time: " + (avgTime > 0 ? avgTime : "no data"));
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error updating average preparation time", ex);
            Platform.runLater(() -> {
                if (avgPrepTime != null) {
                    avgPrepTime.setText("--");
                }
            });
        }
    }
    
    /**
     * Handle account button click
     * @param event ActionEvent
     */ 
        @FXML
    void onAccount(ActionEvent event) {
        try {
            logger.info("Opening Account screen");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/cook/Accountview.fxml"));
            Parent root = loader.load();
            
            AccountController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Switch to account view
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error opening account screen", ex);
            showAlert("Error", "Could not open account screen: " + ex.getMessage());
        }
    }
   /* @FXML
    private void onAccount(ActionEvent event) {
        try {
            System.out.println("onAccount called, currentUser: " + 
                (currentUser != null ? currentUser.getUserId() : "null"));
                
            if (currentUser == null) {
                logger.warning("No current user set in KitchenViewController");
                showAlert(AlertType.WARNING, "User Error", 
                        "No user logged in", 
                        "Please log in to access your account.");
                return;
            }
            
            // Load the account view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/cook/cookAccount.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the current user
            Object controller = loader.getController();
            System.out.println("Account controller type: " + 
                (controller != null ? controller.getClass().getName() : "null"));
                
            if (controller instanceof Controllers.Manager.AccountController) {
                ((Controllers.Manager.AccountController) controller).setCurrentUser(currentUser);
                System.out.println("Set current user in account controller");
            } else {
                System.out.println("ERROR: Controller is not AccountController!");
            }
            
            // Create a new stage for the account view
            Stage stage = new Stage();
            stage.setTitle("My Account - " + currentUser.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
            
            logger.info("Opened account view for user: " + currentUser.getUserId());
        } catch (IOException ex) {
            System.out.println("Error opening account view: " + ex.getMessage());
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Error opening account view", ex);
            showAlert(AlertType.ERROR, "View Error", 
                    "Could not open account view", ex.getMessage());
        }
    }*/
     private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an alert to the user
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header text
     * @param content Alert content text
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Update when table status changes (Observer pattern)
     * @param tableId The table that changed
     * @param newStatus The new status
     */
    @Override
    public void onTableStatusChanged(String tableId, String newStatus) {
        // If table status changes, refresh orders
        // This is especially useful if waiters add new orders
        Platform.runLater(this::loadOrders);
        logger.info("Table status changed: " + tableId + " -> " + newStatus);
    }
    
    /**
     * Clean up resources when controller is no longer needed
     */
    public void dispose() {
        // Stop all timers
        for (Timeline timeline : orderTimers.values()) {
            timeline.stop();
        }
        orderTimers.clear();
        
        // Remove observer
        service.removeTableStatusObserver(this);
        
        logger.info("Kitchen view controller resources disposed");
    }
}