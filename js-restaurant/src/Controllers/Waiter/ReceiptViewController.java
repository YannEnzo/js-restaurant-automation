/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Waiter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Controller for the receipt view screen
 */
public class ReceiptViewController implements Initializable {
    private static final Logger logger = Logger.getLogger(ReceiptViewController.class.getName());
    
    @FXML private TextArea receiptTextArea;
    @FXML private Button printButton;
    @FXML private Button closeButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set monospaced font for better receipt formatting
        receiptTextArea.setFont(Font.font("Courier New", 12));
        receiptTextArea.setEditable(false);
    }
    
    /**
     * Set the receipt text to display
     * @param text Receipt text content
     */
    public void setReceiptText(String text) {
        receiptTextArea.setText(text);
    }
    
    /**
     * Handle print button click
     */
    @FXML
    void onPrint(ActionEvent event) {
        try {
            // Create a printer job
            PrinterJob job = PrinterJob.createPrinterJob();
            
            if (job != null) {
                // Show the printer dialog
                boolean proceed = job.showPrintDialog(receiptTextArea.getScene().getWindow());
                
                if (proceed) {
                    // Print the receipt
                    boolean printed = job.printPage(receiptTextArea);
                    
                    if (printed) {
                        job.endJob();
                        logger.info("Receipt printed successfully");
                    } else {
                        logger.warning("Printing failed");
                    }
                }
            }
        } catch (Exception ex) {
            logger.severe("Error printing receipt: " + ex.getMessage());
        }
    }
    
    /**
     * Handle close button click
     */
    @FXML
    void onClose(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
