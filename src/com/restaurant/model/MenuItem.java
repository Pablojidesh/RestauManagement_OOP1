package com.restaurant.model;

/**
 * MenuItem
 * 
 * Model class representing an individual culinary dish or beverage on the restaurant menu.
 * Encapsulates item metadata including ID, name, foreign category key, category name,
 * unit price, and item description.
 */
public class MenuItem {
    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private double price;
    private String description;

    public MenuItem() {
    }

    public MenuItem(int id, String name, int categoryId, String categoryName, double price, String description) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.description = description;
    }

    public MenuItem(String name, int categoryId, double price, String description) {
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " (Rs." + String.format("%.2f", price) + ")";
    }
}
