package com.restaurant.model;

/**
 * Customer
 * 
 * Model class representing a registered restaurant customer.
 * Stores personal identifiers including full name and contact telephone number.
 */
public class Customer {
    private int id;
    private String name;
    private String contact;

    public Customer() {
    }

    public Customer(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public Customer(String name, String contact) {
        this.name = name;
        this.contact = contact;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return name + " (" + contact + ")";
    }
}
