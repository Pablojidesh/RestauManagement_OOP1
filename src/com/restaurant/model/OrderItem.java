package com.restaurant.model;

/**
 * OrderItem
 * 
 * Model class representing a line item in an active or completed restaurant order.
 * Tracks foreign keys to the parent order and selected menu item, alongside quantity,
 * snapshot unit price at order time, and calculated item subtotal.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String menuItemName;
    private int quantity;
    private double price;
    private double subtotal;

    public OrderItem() {
    }

    public OrderItem(int id, int orderId, int menuItemId, String menuItemName, int quantity, double price, double subtotal) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public OrderItem(int menuItemId, String menuItemName, int quantity, double price) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = quantity * price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.quantity * this.price;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.subtotal = this.quantity * this.price;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return menuItemName + " x" + quantity + " = Rs." + String.format("%.2f", subtotal);
    }
}
