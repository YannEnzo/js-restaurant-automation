package Controllers.Manager;

import Model.User;
import Model.TimeRecord;
import DAO.TimeRecordDAO;
import DAO.UserDAO;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.TouchEvent;

/**
 * Controller for employee timesheet view
 * @author yanne
 */
public class manager_EmployeeTimeController implements Initializable {
    private static final Logger logger = Logger.getLogger(manager_EmployeeTimeController.class.getName());
    private User selectedEmployee;
    private TimeRecordDAO timeRecordDAO;
    private LocalDate startOfWeek;
    private LocalDate endOfWeek;
    private ObservableList<TimeRecord> timeRecords = FXCollections.observableArrayList();
    
    @FXML
    private TableView<TimeRecord> nodeTable;
    
    @FXML
    private TableColumn<TimeRecord, String> dateCol;
    
    @FXML
    private TableColumn<TimeRecord, String> clockInCol;
    
    @FXML
    private TableColumn<TimeRecord, String> clockOutCol;
    
    @FXML
    private TableColumn<TimeRecord, Double> hoursWorkedCol;
    
    @FXML
    private Label weekDisplay;
    
    @FXML
    private Label empName;
    
    @FXML
    private Label empId;
    
    @FXML
    private Label totalHours;
    
    @FXML
    private Button prevWeekBtn;
    
    @FXML
    private Button nextWeekBtn;
    
    @FXML
    private Button currentWeekBtn;
    
    /**
     * Initializes the controller class.
     * @param url The location used to resolve relative paths
     * @param rb The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the TimeRecordDAO
        timeRecordDAO = TimeRecordDAO.getInstance();
        
        // Set up table columns
        setupTableColumns();
        
        // Set up current week
        setupCurrentWeek();
        
        // Set up event handlers for week navigation buttons
        if (prevWeekBtn != null) {
            prevWeekBtn.setOnAction(this::onPreviousWeek);
        }
        
        if (nextWeekBtn != null) {
            nextWeekBtn.setOnAction(this::onNextWeek);
        }
        
        if (currentWeekBtn != null) {
            currentWeekBtn.setOnAction(this::onCurrentWeek);
        }
        
        logger.info("Employee Time Controller initialized");
    }
    
    /**
     * Set up the table columns with cell value factories
     */
    private void setupTableColumns() {
        // Date column displays the date of the time record
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime clockIn = cellData.getValue().getClockInTime();
            if (clockIn != null) {
                return new SimpleStringProperty(
                        clockIn.toLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        
        // Clock in column displays the clock in time
        clockInCol.setCellValueFactory(cellData -> {
            LocalDateTime clockIn = cellData.getValue().getClockInTime();
            if (clockIn != null) {
                return new SimpleStringProperty(
                        clockIn.format(DateTimeFormatter.ofPattern("hh:mm a")));
            }
            return new SimpleStringProperty("");
        });
        
        // Clock out column displays the clock out time
        clockOutCol.setCellValueFactory(cellData -> {
            LocalDateTime clockOut = cellData.getValue().getClockOutTime();
            if (clockOut != null) {
                return new SimpleStringProperty(
                        clockOut.format(DateTimeFormatter.ofPattern("hh:mm a")));
            }
            return new SimpleStringProperty("Not clocked out");
        });
        
        // Hours worked column displays the hours worked
        hoursWorkedCol.setCellValueFactory(cellData -> {
            Double hours = cellData.getValue().getTotalHours();
            if (hours != null) {
                return new SimpleDoubleProperty(hours).asObject();
            }
            return new SimpleDoubleProperty(0).asObject();
        });
    }
    
    /**
     * Set up the current week view
     */
    private void setupCurrentWeek() {
        // Get the current date
        LocalDate today = LocalDate.now();
        
        // Calculate the start of the week (Monday of current week)
        startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Calculate the end of the week (Sunday of current week)
        endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        // Update the week display
        updateWeekDisplay();
    }
    
    /**
     * Update the week display label
     */
    private void updateWeekDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        weekDisplay.setText(startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter));
    }
    
    /**
     * Set the employee for the timesheet
     * @param employee The employee to show timesheet for
     */
    public void setEmployee(User employee) {
        this.selectedEmployee = employee;
        
        // Update employee info
        empName.setText(employee.getFullName());
        empId.setText(employee.getUserId());
        
        // Load timesheet data
        loadTimesheetData();
    }
    
    /**
     * Load timesheet data for the selected week and employee
     */
    private void loadTimesheetData() {
        if (selectedEmployee == null) {
            logger.warning("No employee selected for timesheet");
            return;
        }
        
        try {
            // Clear existing records
            timeRecords.clear();
            
            // Get time records for the selected week
            List<TimeRecord> records = timeRecordDAO.getRecordsForEmployeeAndDateRange(
                    selectedEmployee.getUserId(), startOfWeek, endOfWeek);
            
            timeRecords.addAll(records);
            nodeTable.setItems(timeRecords);
            
            // Update total hours
            updateTotalHours();
            
            logger.info("Loaded " + records.size() + " time records for employee " + 
                    selectedEmployee.getUserId() + " for week of " + startOfWeek);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading timesheet data", ex);
        }
    }
    
    /**
     * Calculate and update the total hours worked
     */
    private void updateTotalHours() {
        double total = 0.0;
        
        for (TimeRecord record : timeRecords) {
            if (record.getTotalHours() != null) {
                total += record.getTotalHours();
            }
        }
        
        totalHours.setText(String.format("%.1f hours", total));
    }
    
    /**
     * Handle previous week button click
     * @param event The action event
     */
    @FXML
    private void onPreviousWeek(ActionEvent event) {
        // Move to previous week
        startOfWeek = startOfWeek.minusWeeks(1);
        endOfWeek = endOfWeek.minusWeeks(1);
        
        // Update week display
        updateWeekDisplay();
        
        // Reload timesheet data
        loadTimesheetData();
    }
    
    /**
     * Handle next week button click
     * @param event The action event
     */
    @FXML
    private void onNextWeek(ActionEvent event) {
        // Move to next week
        startOfWeek = startOfWeek.plusWeeks(1);
        endOfWeek = endOfWeek.plusWeeks(1);
        
        // Update week display
        updateWeekDisplay();
        
        // Reload timesheet data
        loadTimesheetData();
    }
    
    /**
     * Handle current week button click
     * @param event The action event
     */
    @FXML
    private void onCurrentWeek(ActionEvent event) {
        // Reset to current week
        setupCurrentWeek();
        
        // Reload timesheet data
        loadTimesheetData();
    }
    
    /**
     * Handle search touch event
     * @param event The touch event
     */
    @FXML
    void search(TouchEvent event) {
        // This method is currently empty, but could be used for filtering or searching
        logger.info("Search touch event triggered");
    }
}