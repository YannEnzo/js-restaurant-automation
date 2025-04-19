/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * Represents a table in the restaurant
 */
public class RestaurantTable {
    private String tableId;
    private String tableNumber;
    private String status; // AVAILABLE, OCCUPIED, DIRTY
    private int capacity;
    private int locationX;
    private int locationY;
    private String assignedWaiterId;
    
    // Constructors
    public RestaurantTable() {
    }
    
    public RestaurantTable(String tableId, String tableNumber, String status, 
                         int capacity, String assignedWaiterId) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.status = status;
        this.capacity = capacity;
        this.assignedWaiterId = assignedWaiterId;
    }
    
    public RestaurantTable(String tableId, String tableNumber, String status, 
                         int capacity, int locationX, int locationY, 
                         String assignedWaiterId) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.status = status;
        this.capacity = capacity;
        this.locationX = locationX;
        this.locationY = locationY;
        this.assignedWaiterId = assignedWaiterId;
    }
    
    // Getters and Setters
    public String getTableId() {
        return tableId;
    }
    
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
    
    public String getTableNumber() {
        return tableNumber;
    }
    
    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public int getLocationX() {
        return locationX;
    }
    
    public void setLocationX(int locationX) {
        this.locationX = locationX;
    }
    
    public int getLocationY() {
        return locationY;
    }
    
    public void setLocationY(int locationY) {
        this.locationY = locationY;
    }
    
    public String getAssignedWaiterId() {
        return assignedWaiterId;
    }
    
    public void setAssignedWaiterId(String assignedWaiterId) {
        this.assignedWaiterId = assignedWaiterId;
    }
    
    @Override
    public String toString() {
        return "RestaurantTable{" +
                "tableId='" + tableId + '\'' +
                ", tableNumber='" + tableNumber + '\'' +
                ", status='" + status + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}