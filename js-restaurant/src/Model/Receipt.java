/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Class representing a customer receipt
 */
public class Receipt {
    private Order order;
    private double tipAmount;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    /**
     * Constructor
     * @param order The order to generate receipt for
     * @param tipAmount Tip amount
     * @param paymentMethod Payment method used
     */
    public Receipt(Order order, double tipAmount, String paymentMethod) {
        this.order = order;
        this.tipAmount = tipAmount;
        this.paymentMethod = paymentMethod;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Generate a formatted receipt string
     * @return Formatted receipt text
     */
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        // Header
        sb.append("==============================================\n");
        sb.append("                J's RESTAURANT               \n");
        sb.append("          123 Main Street, Anytown           \n");
        sb.append("              Tel: (555) 123-4567            \n");
        sb.append("==============================================\n\n");
        
        // Order information
        sb.append("Order #: ").append(order.getOrderId()).append("\n");
        sb.append("Date: ").append(timestamp.format(dateFormatter)).append("\n");
        sb.append("Time: ").append(timestamp.format(timeFormatter)).append("\n");
        sb.append("Table: ").append(order.getTableId()).append("\n");
        sb.append("Server: ").append(order.getWaiterId()).append("\n\n");
        
        // Order items
        sb.append("Items:\n");
        sb.append("----------------------------------------------\n");
        
        List<OrderItem> items = order.getOrderItems();
        for (OrderItem item : items) {
            String itemLine = String.format("%-4d %-30s %8s", 
                    item.getQuantity(),
                    item.getMenuItemName(),
                    currencyFormat.format(item.getPrice() * item.getQuantity()));
            sb.append(itemLine).append("\n");
            
            // Add seat number if specified
            if (item.getSeatNumber() > 0) {
                sb.append("     * Seat #").append(item.getSeatNumber()).append("\n");
            }
            
            // Add special instructions if any
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
                sb.append("     * ").append(item.getSpecialInstructions()).append("\n");
            }
        }
        
        // Totals
        sb.append("----------------------------------------------\n");
        sb.append(String.format("%-35s %8s", "Subtotal:", 
                currencyFormat.format(order.calculateSubtotal()))).append("\n");
        sb.append(String.format("%-35s %8s", "Tax (10%):", 
                currencyFormat.format(order.calculateTax()))).append("\n");
        sb.append(String.format("%-35s %8s", "Tip:", 
                currencyFormat.format(tipAmount))).append("\n");
        
        double totalWithTip = order.calculateSubtotal() + order.calculateTax() + tipAmount;
        sb.append(String.format("%-35s %8s", "TOTAL:", 
                currencyFormat.format(totalWithTip))).append("\n\n");
        
        // Payment method
        sb.append("Payment Method: ").append(paymentMethod).append("\n\n");
        
        // Footer
        sb.append("==============================================\n");
        sb.append("          Thank you for your business!        \n");
        sb.append("==============================================\n");
        
        return sb.toString();
    }
    
    /**
     * Print the receipt
     */
    public void printReceipt() {
        String receiptText = generateReceipt();
        
        // In a real implementation, this would send to a printer
        // For demo purposes, we'll just print to console
        System.out.println("Printing receipt...");
        System.out.println(receiptText);
    }
}
