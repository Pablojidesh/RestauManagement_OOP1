package com.restaurant.model;

/**
 * InventoryItem
 * 
 * Model class representing stock/raw materials in the restaurant pantry
 * (e.g. Paneer, Rice, Oil, Spices, Dairy).
 * Tracks current quantity, safety low-stock threshold, and per-unit purchase cost.
 */
public class InventoryItem {
    private int id;
    private String itemName;
    private String unit;
    private double quantity;
    private double lowStockThreshold;
    private double costPerUnit;

    public InventoryItem() {
    }

    public InventoryItem(int id, String itemName, String unit, double quantity, double lowStockThreshold, double costPerUnit) {
        this.id = id;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.costPerUnit = costPerUnit;
    }

    public InventoryItem(String itemName, String unit, double quantity, double lowStockThreshold, double costPerUnit) {
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.costPerUnit = costPerUnit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(double lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    /**
     * Checks if current quantity is at or below the safety threshold.
     */
    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    /**
     * Calculates total inventory holding valuation for this item.
     */
    public double getTotalCost() {
        return quantity * costPerUnit;
    }

    @Override
    public String toString() {
        return itemName + " (" + quantity + " " + unit + ")";
    }
}
