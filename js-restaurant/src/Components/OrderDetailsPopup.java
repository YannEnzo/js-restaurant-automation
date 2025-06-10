package Components;

import DAO.RestaurantService;
import Model.Order;
import Model.OrderItem;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Popup window for displaying detailed order information
 */
public class OrderDetailsPopup extends Stage {
    
    private final Order order;
    private final RestaurantService service;
    
    /**
     * Constructor
     * @param order The order to display details for
     */
    public OrderDetailsPopup(Order order) {
        this.order = order;
        this.service = RestaurantService.getInstance();
        
        // Set up the stage
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        setTitle("Order #" + order.getOrderId() + " Details");
        setMinWidth(600);
        setMinHeight(500);
        
        // Create content
        BorderPane root = createContent();
        
        // Set the scene
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/kitchen.css").toExternalForm());
        setScene(scene);
    }
    
    /**
     * Create the popup content
     * @return BorderPane containing the content
     */
    private BorderPane createContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Create header
        VBox header = createHeader();
        root.setTop(header);
        
        // Create center content
        ScrollPane centerScroll = new ScrollPane();
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(true);
        
        VBox centerContent = createCenterContent();
        centerScroll.setContent(centerContent);
        
        root.setCenter(centerScroll);
        
        // Create footer
        HBox footer = createFooter();
        root.setBottom(footer);
        
        return root;
    }
    
    /**
     * Create the header section
     * @return HBox containing the header
     */
    private VBox createHeader() {
        VBox headerBox = new VBox();
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(20);
        
        // Order ID and time
        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.getStyleClass().add("popup-header");
        
        Label orderTimeLabel = new Label("Ordered at: " + 
                order.getOrderDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        orderTimeLabel.getStyleClass().add("popup-subheader");
        
        // Table and waiter info
        Label tableLabel = new Label("Table: " + order.getTableId().replace("TABLE", ""));
        tableLabel.getStyleClass().add("popup-info");
        
        String waiterName = "Unknown";
        try {
            waiterName = service.getUserById(order.getWaiterId()).getFullName();
        } catch (Exception e) {
            waiterName = order.getWaiterId();
        }
        
        Label waiterLabel = new Label("Waiter: " + waiterName);
        waiterLabel.getStyleClass().add("popup-info");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status label
        Label statusLabel = new Label("Status: " + order.getStatus());
        statusLabel.getStyleClass().add("popup-status");
        
        topRow.getChildren().addAll(orderIdLabel, spacer, statusLabel);
        
        HBox infoRow = new HBox();
        infoRow.setSpacing(20);
        infoRow.getChildren().addAll(orderTimeLabel, tableLabel, waiterLabel);
        
        Separator separator = new Separator();
        
        headerBox.getChildren().addAll(topRow, infoRow, separator);
        
        return headerBox;
    }
    
    /**
     * Create the center content
     * @return VBox containing the center content
     */
    private VBox createCenterContent() {
        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(10, 0, 10, 0));
        
        // Add header for items
        Label itemsHeader = new Label("Order Items");
        itemsHeader.getStyleClass().add("popup-section-header");
        content.getChildren().add(itemsHeader);
        
        // Add each order item
        for (OrderItem item : order.getOrderItems()) {
            VBox itemBox = createItemBox(item);
            content.getChildren().add(itemBox);
        }
        
        return content;
    }
    
    /**
     * Create a box for an individual order item
     * @param item The order item
     * @return VBox containing the item details
     */
    private VBox createItemBox(OrderItem item) {
        VBox itemBox = new VBox();
        itemBox.setSpacing(5);
        itemBox.setPadding(new Insets(10));
        itemBox.getStyleClass().add("order-item-box");
        
        // Get menu item details
        String itemName = service.getMenuItemById(item.getMenuItemId()).getName();
        
        // Item header with quantity and name
        HBox itemHeader = new HBox();
        itemHeader.setSpacing(10);
        
        Label quantityLabel = new Label(item.getQuantity() + "x");
        quantityLabel.getStyleClass().add("popup-quantity");
        
        Label nameLabel = new Label(itemName);
        nameLabel.getStyleClass().add("popup-item-name");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label priceLabel = new Label("$" + String.format("%.2f", item.getPrice()));
        priceLabel.getStyleClass().add("popup-price");
        
        itemHeader.getChildren().addAll(quantityLabel, nameLabel, spacer, priceLabel);
        
        // Item details
        VBox itemDetails = new VBox();
        itemDetails.setSpacing(3);
        itemDetails.setPadding(new Insets(5, 0, 0, 30));
        
        // Seat number
        Label seatLabel = new Label("Seat: " + item.getSeatNumber());
        seatLabel.getStyleClass().add("popup-detail");
        itemDetails.getChildren().add(seatLabel);
        
        // Special instructions
        if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
            Label instructionsLabel = new Label("Instructions: " + item.getSpecialInstructions());
            instructionsLabel.getStyleClass().add("popup-detail");
            instructionsLabel.setWrapText(true);
            itemDetails.getChildren().add(instructionsLabel);
        }
        
        // Status
        Label statusLabel = new Label("Status: " + item.getStatus());
        statusLabel.getStyleClass().add("popup-detail");
        itemDetails.getChildren().add(statusLabel);
        
        // Preparation times if available
        if (item.getPreparationStartTime() != null) {
            Label startTimeLabel = new Label("Prep Start: " + 
                    item.getPreparationStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            startTimeLabel.getStyleClass().add("popup-detail");
            itemDetails.getChildren().add(startTimeLabel);
            
            if (item.getCompletionTime() != null) {
                // Calculate prep time
                Duration prepTime = Duration.between(item.getPreparationStartTime(), item.getCompletionTime());
                long minutes = prepTime.toMinutes();
                long seconds = prepTime.getSeconds() % 60;
                
                Label completionTimeLabel = new Label("Completed: " + 
                        item.getCompletionTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                        " (Prep time: " + String.format("%02d:%02d", minutes, seconds) + ")");
                completionTimeLabel.getStyleClass().add("popup-detail");
                itemDetails.getChildren().add(completionTimeLabel);
            }
        }
        
        // Add all components
        itemBox.getChildren().addAll(itemHeader, itemDetails);
        
        return itemBox;
    }
    
    /**
     * Create the footer section
     * @return HBox containing the footer
     */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 0, 0, 0));
        
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("popup-close-btn");
        closeButton.setOnAction(e -> close());
        
        footer.getChildren().add(closeButton);
        
        return footer;
    }
}