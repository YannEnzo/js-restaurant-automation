/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;
 

/**
 * Observer interface for table status changes
 */
public interface TableStatusObserver {
    void onTableStatusChanged(String tableId, String newStatus);
}