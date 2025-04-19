package Controllers.Manager;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

import java.io.IOException;
public class man_MenuItemController {
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
    private Button allItemsButton;
    @FXML
    private Button appetizerButton;
    @FXML
    private Button saladButton;
    @FXML
    private Button entreeButton;
    @FXML
    private Button sandwichButton;
    @FXML
    private Button sideButton;
    @FXML
    private Button burgerButton;
    @FXML
    private Button drinkButton;
    @FXML
    private Button editButton;
    @FXML
    private TextField searchMenuItems;

    @FXML
    private TableView menuItemTable;
    @FXML
    private TableColumn itemNameColumn;
    @FXML
    private TableColumn categoryColumn;
    @FXML
    private TableColumn priceColumn;
    @FXML
    private TableColumn actionColumn;
}
