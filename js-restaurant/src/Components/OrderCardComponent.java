package Components;

import DAO.RestaurantService;
import Model.MenuItem;
import Model.Order;
import Model.OrderItem;
import Model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Custom component for displaying order cards in kitchen view
 */
public class OrderCardComponent extends VBox {
    private static final Logger logger = Logger.getLogger(OrderCardComponent.class.getName());
    
    private final Order order;
    private final String cardType;
    private final RestaurantService service;
    
    private Label timerLabel;
    private Runnable onStartPrep;
    private Runnable onMarkReady;
    private Runnable onClearOrder;
    
    /**
     * Constructor
     * @param order The order to display
     * @param cardType The type of card (PENDING, IN_PROGRESS, READY)
     */
    public OrderCardComponent(Order order, String cardType) {
        this.order = order;
        this.cardType = cardType;
        this.service = RestaurantService.getInstance();
        
        // Log order information for debugging
        logOrderDetails();
        
        // Set up styling
        getStyleClass().add("order-card");
        setSpacing(10);
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);
        setId("order-" + order.getOrderId());
        
        // Create the UI
        createCard();
    }
    
    /**
     * Log order details for debugging
     */
    private void logOrderDetails() {
        logger.info("Creating order card for order ID: " + order.getOrderId());
        
        List<OrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            logger.warning("Order " + order.getOrderId() + " has no items");
        } else {
            logger.info("Order " + order.getOrderId() + " has " + items.size() + " items");
            for (OrderItem item : items) {
                logger.info("  Item: " + item.getMenuItemId() + 
                           ", Name: " + (item.getMenuItemName() != null ? item.getMenuItemName() : "null") +
                           ", Quantity: " + item.getQuantity());
            }
        }
    }
    
    /**
     * Create the card UI
     */
    private void createCard() {
        // Create header with order info
        HBox header = createHeader();
        
        // Create separator
        Separator separator = new Separator();
        
        // Create order items container
        VBox itemsContainer = createItemsContainer();
        
        // Create buttons container
        HBox buttonsContainer = createButtonsContainer();
        
        // Add all components to the card
        getChildren().addAll(header, separator, itemsContainer, buttonsContainer);
    }
    
    /**
     * Create the header section
     * @return HBox containing the header
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        // Order ID and table
        VBox orderInfo = new VBox();
        orderInfo.setSpacing(2);
        
        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.getStyleClass().add("order-header");
        
        String tableNumber = order.getTableId().replace("TABLE", "");
        Label tableLabel = new Label("Table " + tableNumber);
        tableLabel.getStyleClass().add("order-subheader");
        
        orderInfo.getChildren().addAll(orderIdLabel, tableLabel);
        
        // Waiter info
        VBox waiterInfo = new VBox();
        waiterInfo.setSpacing(2);
        
        Label waiterLabel = new Label("Waiter:");
        waiterLabel.getStyleClass().add("order-subheader");
        
        String waiterName = "Unknown";
        try {
            User waiter = service.getUserById(order.getWaiterId());
            if (waiter != null) {
                waiterName = waiter.getFullName();
            } else {
                waiterName = order.getWaiterId();
                logger.warning("Could not find waiter name for ID: " + order.getWaiterId());
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error getting waiter info", ex);
            waiterName = order.getWaiterId();
        }
        
        Label waiterNameLabel = new Label(waiterName);
        waiterNameLabel.getStyleClass().add("order-subheader");
        
        waiterInfo.getChildren().addAll(waiterLabel, waiterNameLabel);
        
        // Create spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Order time and timer
        VBox timeInfo = new VBox();
        timeInfo.setAlignment(Pos.CENTER_RIGHT);
        timeInfo.setSpacing(2);
        
        Label timeLabel = new Label("Time:");
        timeLabel.getStyleClass().add("order-subheader");
        
        LocalDateTime orderTime = order.getOrderDateTime();
        Label orderTimeLabel = new Label(orderTime != null ? 
                orderTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "--:--");
        orderTimeLabel.getStyleClass().add("order-subheader");
        
        // Create timer label
        timerLabel = new Label("--:--");
        timerLabel.getStyleClass().addAll("order-timer", "order-timer-normal");
        timerLabel.setId("timer-" + order.getOrderId());
        
        timeInfo.getChildren().addAll(timeLabel, orderTimeLabel, timerLabel);
        
        // Add all header components
        header.getChildren().addAll(orderInfo, waiterInfo, spacer, timeInfo);
        
        return header;
    }
    
    /**
     * Create the items container section
     * @return VBox containing the items
     */
    private VBox createItemsContainer() {
        VBox itemsContainer = new VBox();
        itemsContainer.setSpacing(5);
        
        // Get order items
        List<OrderItem> orderItems = order.getOrderItems();
        
        if (orderItems == null || orderItems.isEmpty()) {
            // Handle case where order has no items
            Label noItemsLabel = new Label("No items in order");
            noItemsLabel.getStyleClass().add("order-no-items");
            itemsContainer.getChildren().add(noItemsLabel);
            logger.warning("No items found for order: " + order.getOrderId());
            return itemsContainer;
        }
        
        for (OrderItem item : orderItems) {
            // Create item row
            VBox itemRow = new VBox();
            itemRow.setSpacing(2);
            
            // Get item name (try different methods)
            String itemName = getItemName(item);
            
            // Item name and quantity
            Label itemLabel = new Label(item.getQuantity() + "x " + itemName);
            itemLabel.getStyleClass().add("order-item");
            
            itemRow.getChildren().add(itemLabel);
            
            // Add special instructions if any
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
                Label noteLabel = new Label("Note: " + item.getSpecialInstructions());
                noteLabel.getStyleClass().add("order-item-note");
                itemRow.getChildren().add(noteLabel);
            }
            
            // Show seat number
            if (item.getSeatNumber() > 0) {
                Label seatLabel = new Label("Seat: " + item.getSeatNumber());
                seatLabel.getStyleClass().add("order-item-note");
                itemRow.getChildren().add(seatLabel);
            }
            
            itemsContainer.getChildren().add(itemRow);
        }
        
        return itemsContainer;
    }
    
    /**
     * Get item name using multiple approaches to ensure we have something to display
     * @param item OrderItem to get name for
     * @return Item name or fallback text
     */
    private String getItemName(OrderItem item) {
        // Try to get name from the OrderItem directly first (might be set by waiter)
        if (item.getMenuItemName() != null && !item.getMenuItemName().isEmpty()) {
            return item.getMenuItemName();
        }
        
        // Try to look up the item in the menu
        try {
            MenuItem menuItem = service.getMenuItemById(item.getMenuItemId());
            if (menuItem != null) {
                return menuItem.getName();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error getting menu item", ex);
        }
        
        // Fallback to the item ID if we can't get the name
        return "Item #" + item.getMenuItemId();
    }
    
    /**
     * Create the buttons container section
     * @return HBox containing the buttons
     */
    private HBox createButtonsContainer() {
        HBox buttonsContainer = new HBox();
        buttonsContainer.setSpacing(10);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        
        // Add appropriate buttons based on card type
        if ("PENDING".equals(cardType)) {
            Button startButton = new Button("Start Preparation");
            startButton.getStyleClass().add("btn-start");
            startButton.setOnAction(e -> {
                if (onStartPrep != null) {
                    onStartPrep.run();
                }
            });
            buttonsContainer.getChildren().add(startButton);
        } else if ("IN_PROGRESS".equals(cardType)) {
            Button readyButton = new Button("Mark as Ready");
            readyButton.getStyleClass().add("btn-ready");
            readyButton.setOnAction(e -> {
                if (onMarkReady != null) {
                    onMarkReady.run();
                }
            });
            buttonsContainer.getChildren().add(readyButton);
        } else if ("READY".equals(cardType)) {
            Button clearButton = new Button("Clear Order");
            clearButton.getStyleClass().add("btn-clear");
            clearButton.setOnAction(e -> {
                if (onClearOrder != null) {
                    onClearOrder.run();
                }
            });
            buttonsContainer.getChildren().add(clearButton);
        }
        
        return buttonsContainer;
    }
    
    /**
     * Get the timer label for this card
     * @return The timer label
     */
    public Label getTimerLabel() {
        return timerLabel;
    }
    
    /**
     * Set the action for start preparation button
     * @param action The action to run
     */
    public void setOnStartPreparation(Runnable action) {
        this.onStartPrep = action;
    }
    
    /**
     * Set the action for mark as ready button
     * @param action The action to run
     */
    public void setOnMarkAsReady(Runnable action) {
        this.onMarkReady = action;
    }
    
    /**
     * Set the action for clear order button
     * @param action The action to run
     */
    public void setOnClearOrder(Runnable action) {
        this.onClearOrder = action;
    }
}