package com.restaurant.model;

/**
 * RestaurantTable
 * 
 * Model class representing physical seating inside the dining hall.
 * Tracks table number (e.g. "Table 1"), seating capacity, and occupancy status
 * ('AVAILABLE', 'OCCUPIED', 'RESERVED').
 */
public class RestaurantTable {
    private int id;
    private String tableNumber;
    private int capacity;
    private String status;

    public RestaurantTable() {
    }

    public RestaurantTable(int id, String tableNumber, int capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public RestaurantTable(String tableNumber, int capacity, String status) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return tableNumber + " (" + capacity + " seats - " + status + ")";
    }
}
