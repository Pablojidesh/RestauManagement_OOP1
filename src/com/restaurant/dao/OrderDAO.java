package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO
 * 
 * Data Access Object for Order and OrderItem entities.
 * Enforces atomic database transactions when persisting orders and their line items.
 * Ensures the order counts/totals shown across Admin and Kitchen screens remain
 * synchronized from the identical underlying data source.
 */
public class OrderDAO {

    /**
     * Creates an order and its associated line items atomically.
     *
     * @param order the Order parent entity
     * @param items list of line items
     * @return generated order ID
     */
    public int createOrder(Order order, List<OrderItem> items) {
        if (order == null || order.getTableId() <= 0 || order.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Valid table and customer are required to place an order.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        String orderSql = "INSERT INTO orders (table_id, customer_id, status, total_amount) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int orderId = -1;
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getTableId());
                orderStmt.setInt(2, order.getCustomerId());
                orderStmt.setString(3, order.getStatus() != null ? order.getStatus() : "Placed");
                orderStmt.setDouble(4, order.getTotalAmount());

                orderStmt.executeUpdate();
                try (ResultSet rs = orderStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        order.setId(orderId);
                    }
                }
            }

            if (orderId <= 0) {
                conn.rollback();
                throw new SQLException("Failed to obtain generated order ID.");
            }

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (OrderItem item : items) {
                    if (item.getMenuItemId() <= 0 || item.getQuantity() <= 0 || item.getPrice() <= 0) {
                        conn.rollback();
                        throw new IllegalArgumentException("Invalid order line item details.");
                    }
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getMenuItemId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getPrice());
                    itemStmt.setDouble(5, item.getSubtotal());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            // Mark table occupied
            String tableSql = "UPDATE restaurant_tables SET status = 'OCCUPIED' WHERE id = ?";
            try (PreparedStatement tableStmt = conn.prepareStatement(tableSql)) {
                tableStmt.setInt(1, order.getTableId());
                tableStmt.executeUpdate();
            }

            conn.commit();
            DBConnection.logQuery("OrderDAO.createOrder", "INSERT INTO orders (id=" + orderId + ", items=" + items.size() + ")", 1);
            return orderId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[OrderDAO] Error creating order: " + e.getMessage());
            throw new RuntimeException("Database error creating order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Retrieves all orders joined with table number and customer name.
     */
    public List<Order> getAllOrders() {
        return queryOrders(null);
    }

    /**
     * Retrieves active orders (Placed, Preparing, Served) for Kitchen display.
     */
    public List<Order> getActiveOrders() {
        String filter = "WHERE o.status IN ('Placed', 'Preparing', 'Served')";
        return queryOrders(filter);
    }

    private List<Order> queryOrders(String whereClause) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.id, o.table_id, t.table_number, o.customer_id, c.name AS customer_name, ")
           .append("o.status, o.total_amount, o.created_at ")
           .append("FROM orders o ")
           .append("JOIN restaurant_tables t ON o.table_id = t.id ")
           .append("JOIN customers c ON o.customer_id = c.id ");

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(whereClause).append(" ");
        }
        sql.append("ORDER BY o.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int tableId = rs.getInt("table_id");
                String tableNumber = rs.getString("table_number");
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                String status = rs.getString("status");
                double totalAmount = rs.getDouble("total_amount");
                Timestamp createdAt = rs.getTimestamp("created_at");

                if (tableNumber == null || customerName == null || status == null) {
                    continue; // Skip corrupted rows
                }

                Order o = new Order(id, tableId, tableNumber, customerId, customerName, status, totalAmount, createdAt);
                o.setItems(getOrderItems(id));
                list.add(o);
            }
            DBConnection.logQuery("OrderDAO.queryOrders", sql.toString(), list.size());
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error querying orders: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all line items belonging to an order.
     */
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.menu_item_id, m.name AS item_name, oi.quantity, oi.price, oi.subtotal " +
                     "FROM order_items oi " +
                     "JOIN menu_items m ON oi.menu_item_id = m.id " +
                     "WHERE oi.order_id = ? ORDER BY oi.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int mId = rs.getInt("menu_item_id");
                    String itemName = rs.getString("item_name");
                    int qty = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    double subtotal = rs.getDouble("subtotal");

                    if (itemName == null || qty <= 0) continue;
                    items.add(new OrderItem(id, orderId, mId, itemName, qty, price, subtotal));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error fetching order items for order #" + orderId + ": " + e.getMessage());
        }
        return items;
    }

    /**
     * Updates order status (e.g. Placed -> Preparing -> Served -> Completed).
     */
    public boolean updateOrderStatus(int orderId, String status) {
        if (orderId <= 0 || status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid order ID or status.");
        }
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.trim());
            stmt.setInt(2, orderId);
            int rows = stmt.executeUpdate();
            DBConnection.logQuery("OrderDAO.updateOrderStatus", sql + " [status=" + status + ", id=" + orderId + "]", rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a single order by ID.
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT o.id, o.table_id, t.table_number, o.customer_id, c.name AS customer_name, " +
                     "o.status, o.total_amount, o.created_at " +
                     "FROM orders o " +
                     "JOIN restaurant_tables t ON o.table_id = t.id " +
                     "JOIN customers c ON o.customer_id = c.id WHERE o.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order o = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_id"),
                        rs.getString("table_number"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("status"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("created_at")
                    );
                    o.setItems(getOrderItems(orderId));
                    return o;
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error fetching order by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Counts total orders in the database for consistency verification.
     */
    public int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] Error counting orders: " + e.getMessage());
        }
        return 0;
    }
}
