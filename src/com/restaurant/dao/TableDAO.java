package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.RestaurantTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TableDAO
 * 
 * Data Access Object for dining tables.
 * Loops over and returns all active tables from the database.
 * Supports status transitions (AVAILABLE, OCCUPIED, RESERVED).
 */
public class TableDAO {

    /**
     * Retrieves all restaurant tables.
     * Guarantees returning the full list of tables from the database,
     * skipping any records with corrupted/null table identifiers.
     *
     * @return List of all valid RestaurantTable entities
     */
    public List<RestaurantTable> getAllTables() {
        List<RestaurantTable> list = new ArrayList<>();
        String sql = "SELECT id, table_number, capacity, status FROM restaurant_tables ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String tableNumber = rs.getString("table_number");
                int capacity = rs.getInt("capacity");
                String status = rs.getString("status");

                // Defensive validation: skip corrupted rows
                if (tableNumber == null || tableNumber.trim().isEmpty()) {
                    continue;
                }

                list.add(new RestaurantTable(id, tableNumber.trim(), capacity, status != null ? status.trim() : "AVAILABLE"));
            }
        } catch (SQLException e) {
            System.err.println("[TableDAO] Error fetching tables: " + e.getMessage());
        }
        return list;
    }

    /**
     * Fetches a specific table by its ID.
     */
    public RestaurantTable getTableById(int id) {
        String sql = "SELECT id, table_number, capacity, status FROM restaurant_tables WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String tableNum = rs.getString("table_number");
                    if (tableNum != null && !tableNum.trim().isEmpty()) {
                        return new RestaurantTable(id, tableNum.trim(), rs.getInt("capacity"), rs.getString("status"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[TableDAO] Error fetching table by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates occupancy status for a given table.
     */
    public boolean updateTableStatus(int id, String status) {
        if (id <= 0 || status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid table ID or status.");
        }
        String sql = "UPDATE restaurant_tables SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.trim().toUpperCase());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TableDAO] Error updating table status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new table to the restaurant.
     */
    public int addTable(RestaurantTable table) {
        if (table == null || table.getTableNumber() == null || table.getTableNumber().trim().isEmpty() || table.getCapacity() <= 0) {
            throw new IllegalArgumentException("Table number and positive capacity are required.");
        }
        String sql = "INSERT INTO restaurant_tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, table.getTableNumber().trim());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus() != null ? table.getStatus().trim().toUpperCase() : "AVAILABLE");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        table.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[TableDAO] Error adding table: " + e.getMessage());
            throw new RuntimeException("Database error adding table: " + e.getMessage(), e);
        }
        return -1;
    }
}
