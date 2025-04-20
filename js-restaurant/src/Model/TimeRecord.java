package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a time clock record for an employee
 */
public class TimeRecord {
    private int recordId;
    private String userId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private Double totalHours;
    
    /**
     * Default constructor
     */
    public TimeRecord() {
    }
    
    /**
     * Constructor with userId and clockInTime
     * @param userId User ID
     * @param clockInTime Clock in time
     */
    public TimeRecord(String userId, LocalDateTime clockInTime) {
        this.userId = userId;
        this.clockInTime = clockInTime;
    }
    
    /**
     * Constructor with all fields
     * @param recordId Record ID
     * @param userId User ID
     * @param clockInTime Clock in time
     * @param clockOutTime Clock out time
     * @param totalHours Total hours worked
     */
    public TimeRecord(int recordId, String userId, LocalDateTime clockInTime, 
                       LocalDateTime clockOutTime, Double totalHours) {
        this.recordId = recordId;
        this.userId = userId;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.totalHours = totalHours;
    }

    /**
     * Get the record ID
     * @return the recordId
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * Set the record ID
     * @param recordId the recordId to set
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * Get the user ID
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user ID
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the clock in time
     * @return the clockInTime
     */
    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    /**
     * Set the clock in time
     * @param clockInTime the clockInTime to set
     */
    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    /**
     * Get the clock out time
     * @return the clockOutTime
     */
    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    /**
     * Set the clock out time
     * @param clockOutTime the clockOutTime to set
     */
    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    /**
     * Get the total hours worked
     * @return the totalHours
     */
    public Double getTotalHours() {
        return totalHours;
    }

    /**
     * Set the total hours worked
     * @param totalHours the totalHours to set
     */
    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }
    
    /**
     * Get formatted date string for the clock in time
     * @return Formatted date string
     */
    public String getFormattedDate() {
        if (clockInTime == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return clockInTime.format(formatter);
    }
    
    /**
     * Get formatted time string for the clock in time
     * @return Formatted time string
     */
    public String getFormattedClockInTime() {
        if (clockInTime == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return clockInTime.format(formatter);
    }
    
    /**
     * Get formatted time string for the clock out time
     * @return Formatted time string or "Not clocked out" if null
     */
    public String getFormattedClockOutTime() {
        if (clockOutTime == null) {
            return "Not clocked out";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return clockOutTime.format(formatter);
    }
    
    /**
     * Get formatted hours string
     * @return Formatted hours string
     */
    public String getFormattedHours() {
        if (totalHours == null) {
            return "0.0";
        }
        
        return String.format("%.1f", totalHours);
    }
    
    @Override
    public String toString() {
        return "TimeRecord{" + "recordId=" + recordId + 
                ", userId=" + userId + 
                ", clockInTime=" + (clockInTime != null ? clockInTime.toString() : "null") + 
                ", clockOutTime=" + (clockOutTime != null ? clockOutTime.toString() : "null") + 
                ", totalHours=" + totalHours + '}';
    }
}