package Controllers.Waiter;

import Controllers.UserAwareController;
import Model.Order;
import Model.Receipt;
import Model.User;
import DAO.RestaurantService;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for waiter's payment screen
 */
public class WaiterPaymentController implements Initializable, UserAwareController {
    private static final Logger logger = Logger.getLogger(WaiterPaymentController.class.getName());
    private final RestaurantService service = RestaurantService.getInstance();
    private User currentUser;
    private Order currentOrder;
    private double tipAmount = 0.0;
    private boolean paymentCompleted = false;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    @FXML private Label orderNumberLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label tipLabel;
    @FXML private Label totalLabel;
    @FXML private Label changeLabel;
    
    @FXML private TextField cardNumberField;
    @FXML private TextField nameField;
    @FXML private TextField expirationField;
    @FXML private TextField ccvField;
    
    @FXML private TextField cashAmountField;
    
    @FXML private Button returnButton;
    @FXML private Button payButton;
    @FXML private TabPane paymentTabPane;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Initializing WaiterPaymentController");
        
        // Add a listener for cash amount to calculate change
        cashAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double cashAmount = Double.parseDouble(newValue);
                double total = currentOrder.calculateSubtotal() + currentOrder.calculateTax() + tipAmount;
                double change = cashAmount - total;
                
                if (change >= 0) {
                    changeLabel.setText(currencyFormat.format(change));
                } else {
                    changeLabel.setText("Insufficient amount");
                }
            } catch (NumberFormatException e) {
                changeLabel.setText("$0.00");
            }
        });
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        logger.info("Current user set: " + user.getUsername());
    }
    
    /**
     * Set the current order to process
     */
    public void setOrder(Order order) {
        this.currentOrder = order;
        
        if (order != null) {
            logger.info("Processing payment for order: " + order.getOrderId());
            
            // Update the labels
            orderNumberLabel.setText("Order Number: " + order.getOrderId());
            subtotalLabel.setText("Subtotal: " + currencyFormat.format(order.calculateSubtotal()));
            taxLabel.setText("Tax (10%): " + currencyFormat.format(order.calculateTax()));
            tipLabel.setText("Tip: " + currencyFormat.format(tipAmount));
            
            double total = order.calculateSubtotal() + order.calculateTax() + tipAmount;
            totalLabel.setText("Total: " + currencyFormat.format(total));
        }
    }
    
    /**
     * Set the tip amount
     */
    public void setTipAmount(double tipAmount) {
        this.tipAmount = tipAmount;
        logger.info("Tip amount set to: " + currencyFormat.format(tipAmount));
        
        // Update tip label
        if (tipLabel != null && currentOrder != null) {
            tipLabel.setText("Tip: " + currencyFormat.format(tipAmount));
            
            // Update total
            double total = currentOrder.calculateSubtotal() + currentOrder.calculateTax() + tipAmount;
            totalLabel.setText("Total: " + currencyFormat.format(total));
        }
    }
    
    /**
     * Check if payment has been completed
     */
    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }
    
    /**
     * Handle return button click
     */
    @FXML
    void onReturn(ActionEvent event) {
        logger.info("Return button clicked");
        // Close the window
        Stage stage = (Stage) returnButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Generate and display the receipt
     */
    private void showReceipt(String paymentMethod) {
        try {
            // Create the receipt
            Receipt receipt = new Receipt(currentOrder, tipAmount, paymentMethod);
            String receiptText = receipt.generateReceipt();
            
            // Create receipt preview window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/waiterViews/receiptView.fxml"));
            Parent root = loader.load();
            
            // Get controller and set receipt text
            ReceiptViewController controller = loader.getController();
            controller.setReceiptText(receiptText);
            
            // Show the receipt window
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Receipt - Order " + currentOrder.getOrderId());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            logger.info("Receipt displayed successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error showing receipt", ex);
            showAlert("Error", "Failed to display receipt: " + ex.getMessage());
        }
    }
    
    /**
     * Handle pay button click
     */
    @FXML
    void onProcessPayment(ActionEvent event) {
        try {
            if (currentOrder == null) {
                showAlert("Error", "No order to process");
                return;
            }
            
            // Get the total amount
            double total = currentOrder.calculateSubtotal() + currentOrder.calculateTax() + tipAmount;
            logger.info("Processing payment of " + currencyFormat.format(total) + " for order: " + currentOrder.getOrderId());
            
            // Set payment method based on active tab
            String paymentMethod = "CASH";  // Default to CASH
            boolean validated = false;
            
            // Check which tab is active
            Tab activeTab = paymentTabPane.getSelectionModel().getSelectedItem();
            
            if (activeTab.getText().equals("Card Payment")) {
                // Credit card tab is active
                paymentMethod = "CREDIT_CARD";
                logger.info("Payment method: Credit Card");
                
                // Validate card details
                if (cardNumberField.getText().isEmpty() || nameField.getText().isEmpty() || 
                    expirationField.getText().isEmpty() || ccvField.getText().isEmpty()) {
                    showAlert("Error", "Please complete all card fields");
                    return;
                }
                
                // Do basic validation for the card number (real apps will do more checks)
                if (cardNumberField.getText().length() < 13 || cardNumberField.getText().length() > 19) {
                    showAlert("Error", "Invalid card number");
                    return;
                }
                
                validated = true;
            } else if (activeTab.getText().equals("Cash Payment")) {
                // Cash tab is active
                paymentMethod = "CASH";
                logger.info("Payment method: Cash");
                
                // Validate cash amount
                try {
                    double cashAmount = Double.parseDouble(cashAmountField.getText());
                    if (cashAmount < total) {
                        showAlert("Error", "Cash amount is less than total");
                        return;
                    }
                    validated = true;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter a valid cash amount");
                    return;
                }
            }
            
            if (validated) {
                // Process the payment
                boolean success = service.processPayment(currentOrder.getOrderId(), paymentMethod, tipAmount);
                
                if (success) {
                    logger.info("Payment processed successfully");
                    showAlert("Success", "Payment processed successfully");
                    paymentCompleted = true;
                    
                    // Show the receipt
                    showReceipt(paymentMethod);
                    
                    // Mark table as dirty after payment
                    service.updateTableStatus(currentOrder.getTableId(), "DIRTY", currentUser.getUserId());
                    logger.info("Table marked as dirty after payment");
                    
                    // Close the window
                    Stage stage = (Stage) payButton.getScene().getWindow();
                    stage.close();
                } else {
                    logger.warning("Failed to process payment");
                    showAlert("Error", "Failed to process payment");
                }
            }
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error processing payment", ex);
            showAlert("Error", "Failed to process payment: " + ex.getMessage());
        }
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