package com.restaurant.model;

/**
 * Employee
 * 
 * Model class representing restaurant staff members (Admin, Kitchen, Waiter).
 * Encapsulates credentials, authorization role, contact info, and current monthly salary.
 */
public class Employee {
    private int id;
    private String name;
    private String role; // 'ADMIN', 'KITCHEN', 'WAITER'
    private String username;
    private String password;
    private double salary;
    private String contact;

    public Employee() {
    }

    public Employee(int id, String name, String role, String username, String password, double salary, String contact) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.username = username;
        this.password = password;
        this.salary = salary;
        this.contact = contact;
    }

    public Employee(String name, String role, String username, String password, double salary, String contact) {
        this.name = name;
        this.role = role;
        this.username = username;
        this.password = password;
        this.salary = salary;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isKitchen() {
        return "KITCHEN".equalsIgnoreCase(role);
    }

    public boolean isWaiter() {
        return "WAITER".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
