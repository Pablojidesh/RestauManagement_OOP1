package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BillDAO
 * 
 * Data Access Object for finalized bills and invoice records.
 * Records financial transactions upon checkout completion and computes aggregate revenue.
 */
public class BillDAO {

    /**
     * Persists a finalized bill invoice.
     *
     * @param bill the Bill model entity
     * @return generated bill ID
     */
    public int createBill(Bill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("Bill cannot be null.");
        }
        if (bill.getOrderId() <= 0) {
            throw new IllegalArgumentException("Bill must be associated with a valid order ID.");
        }
        if (bill.getGrandTotal() <= 0) {
            throw new IllegalArgumentException("Bill grand total must be greater than 0.");
        }
        if (bill.getPaymentMethod() == null || bill.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty.");
        }

        String sql = "INSERT INTO bills (order_id, subtotal, discount_percent, tax_percent, grand_total, payment_method, payment_reference) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, bill.getOrderId());
            stmt.setDouble(2, bill.getSubtotal());
            stmt.setDouble(3, bill.getDiscountPercent());
            stmt.setDouble(4, bill.getTaxPercent());
            stmt.setDouble(5, bill.getGrandTotal());
            stmt.setString(6, bill.getPaymentMethod().trim());
            stmt.setString(7, bill.getPaymentReference() != null ? bill.getPaymentReference().trim() : "");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        bill.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Error creating bill: " + e.getMessage());
            throw new RuntimeException("Database error creating bill: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Retrieves all recorded bills.
     */
    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT id, order_id, subtotal, discount_percent, tax_percent, grand_total, " +
                     "payment_method, payment_reference, created_at FROM bills ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int orderId = rs.getInt("order_id");
                double subtotal = rs.getDouble("subtotal");
                double discount = rs.getDouble("discount_percent");
                double tax = rs.getDouble("tax_percent");
                double grandTotal = rs.getDouble("grand_total");
                String method = rs.getString("payment_method");
                String ref = rs.getString("payment_reference");
                Timestamp createdAt = rs.getTimestamp("created_at");

                if (orderId <= 0 || grandTotal <= 0 || method == null) {
                    continue; // Skip corrupted rows
                }

                list.add(new Bill(id, orderId, subtotal, discount, tax, grandTotal, method, ref, createdAt));
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Error fetching bills: " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates total revenue across all completed bills.
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM bills";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Error calculating total revenue: " + e.getMessage());
        }
        return 0.0;
    }
}
