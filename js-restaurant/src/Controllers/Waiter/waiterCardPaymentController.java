package Controllers.Waiter;

import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
public class waiterCardPaymentController {
    @FXML
    private Text tableIdentifier;
    @FXML
    private AnchorPane tableStatusIndicator;

    @FXML
    private TextArea chequeArea;

    @FXML
    private Button cardPaymentButton;
    @FXML
    private Button cashPaymentButton;
    @FXML
    private Button returnButton;
    @FXML
    private Button payButton;

    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField expDateField;
    @FXML
    private TextField ccvField;
}
