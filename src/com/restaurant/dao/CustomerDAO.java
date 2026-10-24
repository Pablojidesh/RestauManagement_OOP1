package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO
 * 
 * Data Access Object for customer registration and querying.
 * Strictly checks that customer name and contact phone number are non-empty.
 */
public class CustomerDAO {

    /**
     * Registers a new customer into the database.
     *
     * @param customer Customer to register
     * @return Generated customer ID, or -1 on failure
     */
    public int addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            System.err.println("[CustomerDAO] Validation failure: Customer name cannot be empty.");
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (customer.getContact() == null || customer.getContact().trim().isEmpty()) {
            System.err.println("[CustomerDAO] Validation failure: Customer contact cannot be empty.");
            throw new IllegalArgumentException("Customer contact cannot be empty.");
        }

        String sql = "INSERT INTO customers (name, contact) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName().trim());
            stmt.setString(2, customer.getContact().trim());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        customer.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error inserting customer: " + e.getMessage());
            throw new RuntimeException("Database error registering customer: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Retrieves all registered customers.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, contact FROM customers ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                if (name == null || name.trim().isEmpty() || contact == null || contact.trim().isEmpty()) {
                    continue; // Skip corrupted rows
                }

                list.add(new Customer(id, name.trim(), contact.trim()));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error fetching customers: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a customer by ID.
     */
    public Customer getCustomerById(int id) {
        String sql = "SELECT id, name, contact FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String contact = rs.getString("contact");
                    if (name != null && !name.trim().isEmpty()) {
                        return new Customer(id, name.trim(), contact != null ? contact.trim() : "");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error fetching customer by id: " + e.getMessage());
        }
        return null;
    }
}
