package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuDAO
 * 
 * Data Access Object for MenuItem persistence.
 * Enforces strict validation rules: dish name cannot be empty, category ID must be valid,
 * and price must be strictly positive (> 0).
 * Implements live search queries using case-insensitive SQL matching.
 */
public class MenuDAO {

    /**
     * Inserts a new dish into the menu_items table.
     * Validates required fields before executing database insert.
     *
     * @param item MenuItem object to save
     * @return generated ID, or -1 on failure
     */
    public int addMenuItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            System.err.println("[MenuDAO] Validation failure: Dish name is empty.");
            throw new IllegalArgumentException("Dish name cannot be blank.");
        }
        if (item.getCategoryId() <= 0) {
            System.err.println("[MenuDAO] Validation failure: Invalid category ID: " + item.getCategoryId());
            throw new IllegalArgumentException("A valid category must be selected.");
        }
        if (item.getPrice() <= 0.0) {
            System.err.println("[MenuDAO] Validation failure: Price must be greater than 0: " + item.getPrice());
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        String sql = "INSERT INTO menu_items (name, category_id, price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName().trim());
            stmt.setInt(2, item.getCategoryId());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getDescription() != null ? item.getDescription().trim() : "");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        item.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MenuDAO] Error adding menu item: " + e.getMessage());
            throw new RuntimeException("Database error adding menu item: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Retrieves all menu items joined with their category names.
     * Skips invalid or corrupted rows.
     */
    public List<MenuItem> getAllMenuItems() {
        return searchMenuItems(null);
    }

    /**
     * Searches menu items by keyword (case-insensitive) using SQL LIKE matching.
     * If keyword is null or empty, returns all active dishes.
     *
     * @param keyword search substring (e.g., "Paneer")
     * @return List of matching MenuItem objects
     */
    public List<MenuItem> searchMenuItems(String keyword) {
        List<MenuItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.id, m.name, m.category_id, c.name AS category_name, m.price, m.description ")
           .append("FROM menu_items m ")
           .append("JOIN categories c ON m.category_id = c.id ");

        boolean hasFilter = (keyword != null && !keyword.trim().isEmpty());
        if (hasFilter) {
            sql.append("WHERE LOWER(m.name) LIKE LOWER(?) ");
        }
        sql.append("ORDER BY c.name ASC, m.name ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (hasFilter) {
                stmt.setString(1, "%" + keyword.trim() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int categoryId = rs.getInt("category_id");
                    String categoryName = rs.getString("category_name");
                    double price = rs.getDouble("price");
                    String desc = rs.getString("description");

                    // Defensive data integrity check: skip corrupted rows
                    if (name == null || name.trim().isEmpty() || price <= 0 || categoryName == null) {
                        continue;
                    }

                    list.add(new MenuItem(id, name.trim(), categoryId, categoryName.trim(), price, desc != null ? desc.trim() : ""));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MenuDAO] Error searching menu items: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a single MenuItem by its ID.
     */
    public MenuItem getMenuItemById(int id) {
        String sql = "SELECT m.id, m.name, m.category_id, c.name AS category_name, m.price, m.description " +
                     "FROM menu_items m JOIN categories c ON m.category_id = c.id WHERE m.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    if (name != null && !name.trim().isEmpty() && price > 0) {
                        return new MenuItem(
                            rs.getInt("id"),
                            name.trim(),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            price,
                            rs.getString("description")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[MenuDAO] Error fetching menu item by id: " + e.getMessage());
        }
        return null;
    }
}
