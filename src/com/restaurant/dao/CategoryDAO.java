package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO
 * 
 * Data Access Object managing persistence operations for Category entities in MySQL.
 * Strictly uses plain JDBC PreparedStatement and validates all required fields
 * before database mutation as a second line of defensive programming.
 */
public class CategoryDAO {

    /**
     * Inserts a new category into the database.
     * Rejects null or blank category names defensively.
     *
     * @param category the Category object to persist
     * @return generated primary key ID, or -1 on failure
     */
    public int addCategory(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            System.err.println("[CategoryDAO] Validation failure: Category name cannot be null or empty.");
            throw new IllegalArgumentException("Category name cannot be null or empty.");
        }

        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName().trim());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        category.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Error inserting category: " + e.getMessage());
            throw new RuntimeException("Database error adding category: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Retrieves all categories sorted alphabetically.
     * Filters out any corrupted database records missing a valid name.
     *
     * @return List of valid Category objects
     */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name FROM categories ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                // Skip invalid/blank rows defensively
                if (name == null || name.trim().isEmpty()) {
                    continue;
                }
                list.add(new Category(id, name.trim()));
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Error fetching categories: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a category by its primary key ID.
     */
    public Category getCategoryById(int id) {
        String sql = "SELECT id, name FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    if (name != null && !name.trim().isEmpty()) {
                        return new Category(id, name.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Error fetching category by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a category with the specified name already exists (case-insensitive).
     */
    public boolean categoryExists(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Error checking category existence: " + e.getMessage());
        }
        return false;
    }
}
