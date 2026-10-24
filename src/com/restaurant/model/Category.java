package com.restaurant.model;

/**
 * Category
 * 
 * Model class representing a food or beverage category in the restaurant menu
 * (e.g., Starters, Main Course, Breads, Desserts, Beverages).
 * Demonstrates encapsulation with private fields, constructor overloading,
 * and standard accessor/mutator methods.
 */
public class Category {
    private int id;
    private String name;

    public Category() {
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
