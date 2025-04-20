package Controllers.Manager;

import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import java.io.IOException;
public class man_KitchenPerformanceController {
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
    private Button topSellerButton;
    @FXML
    private Button inventoryButton;
    @FXML
    private Button kitchenPerformanceButton;

    @FXML
    private TableView prepTimeChart;
    @FXML
    private TableColumn itemNameColumn;
    @FXML
    private TableColumn categoryColumn;
    @FXML
    private TableColumn avgTimeColumn;
    @FXML
    private TableColumn targetTimeColumn;
}
