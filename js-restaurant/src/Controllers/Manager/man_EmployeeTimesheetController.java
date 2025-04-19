package Controllers.Manager;

import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import java.io.IOException;
public class man_EmployeeTimesheetController {
    @FXML
    private Button dashboardButton;
    @FXML
    private Button employeeButton;
    @FXML
    private Button floorButton;
    @FXML
    private Button menuButton;
    @FXML
    private Button reportButton;
    @FXML
    private Button accountButton;

    @FXML
    private Text employeeName;
    @FXML
    private Text employeeID;
    @FXML
    private Text employeeRole;
    @FXML
    private TableView employeeTimesheetTable;
    @FXML
    private TableColumn dateColumn;
    @FXML
    private TableColumn clockInColumn;
    @FXML
    private TableColumn clockOutColumn;
    @FXML
    private TableColumn breakColumn;
    @FXML
    private TableColumn hoursColumn;
}
