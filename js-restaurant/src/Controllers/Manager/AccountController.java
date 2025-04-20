/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Manager;

import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 *
 * @author yanne
 */
public class AccountController {
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
    private Button clockInButton;

    @FXML
    private Text totalHourLine;
    @FXML
    private Text scheduledLine;
    @FXML
    private Button logoutButton;
    
}
