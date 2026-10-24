package com.restaurant.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Order
 * 
 * Model class representing a dining order placed for a table.
 * Maintains references to table and customer, current order status
 * ('Placed', 'Preparing', 'Served', 'Completed'), total billable amount,
 * timestamp, and list of associated line items.
 */
public class Order {
    private int id;
    private int tableId;
    private String tableNumber;
    private int customerId;
    private String customerName;
    private String status;
    private double totalAmount;
    private Timestamp createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(int id, int tableId, String tableNumber, int customerId, String customerName,
                 String status, double totalAmount, Timestamp createdAt) {
        this.id = id;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}
