package Controllers.Waiter;

import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

import java.io.IOException;
public class waiterAccountController {
    @FXML
    private Button returnButton;
    @FXML
    private Button clockInButton;
    @FXML
    private Button logoutButton;

    @FXML
    private Text nameLine;
    @FXML
    private Text idLine;
    @FXML
    private Text roleLine;
    @FXML
    private Text contactLine;

    @FXML
    private Text statusLine;
    @FXML
    private Text todayHoursLine;
    @FXML
    private Text lastClockLine;

    @FXML
    private Text totalHourLine;
    @FXML
    private Text scheduledLine;

}
