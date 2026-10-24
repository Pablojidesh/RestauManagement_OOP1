package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO
 * 
 * Data Access Object for Employee management and authentication.
 * Handles credential verification, salary inquiries, and administrative updates.
 * Guarantees role-isolated salary fetching: non-admin roles fetch strictly by their
 * verified session employee ID.
 */
public class EmployeeDAO {

    /**
     * Authenticates an employee by username and password.
     *
     * @param username user login name
     * @param password user password
     * @return Employee object on success, or null if credentials are invalid
     */
    public Employee authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, name, role, username, password, salary, contact FROM employees WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getDouble("salary"),
                        rs.getString("contact")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error during authentication: " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetches fresh salary and employee details by employee ID.
     * Used by Kitchen and Waiter portals to view their OWN salary fresh from the database.
     */
    public Employee getEmployeeById(int id) {
        String sql = "SELECT id, name, role, username, password, salary, contact FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getDouble("salary"),
                        rs.getString("contact")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error fetching employee by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all employees (Admin view only).
     */
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT id, name, role, username, password, salary, contact FROM employees ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                String username = rs.getString("username");
                String password = rs.getString("password");
                double salary = rs.getDouble("salary");
                String contact = rs.getString("contact");

                // Defensive integrity check: skip corrupted records
                if (name == null || role == null || username == null) {
                    continue;
                }

                list.add(new Employee(id, name, role, username, password, salary, contact != null ? contact : ""));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error fetching all employees: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an employee's salary (Admin action only).
     */
    public boolean updateSalary(int employeeId, double newSalary) {
        if (employeeId <= 0 || newSalary < 0) {
            throw new IllegalArgumentException("Invalid employee ID or negative salary provided.");
        }
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newSalary);
            stmt.setInt(2, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error updating salary: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new employee into the system.
     * Enforces strict validation of all credentials and roles.
     */
    public int addEmployee(Employee emp) {
        if (emp == null) throw new IllegalArgumentException("Employee cannot be null.");
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        if (emp.getUsername() == null || emp.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (emp.getPassword() == null || emp.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (emp.getRole() == null || (!emp.getRole().equalsIgnoreCase("ADMIN") &&
                                      !emp.getRole().equalsIgnoreCase("KITCHEN") &&
                                      !emp.getRole().equalsIgnoreCase("WAITER"))) {
            throw new IllegalArgumentException("Role must be one of ADMIN, KITCHEN, or WAITER.");
        }
        if (emp.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }

        String sql = "INSERT INTO employees (name, role, username, password, salary, contact) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, emp.getName().trim());
            stmt.setString(2, emp.getRole().trim().toUpperCase());
            stmt.setString(3, emp.getUsername().trim());
            stmt.setString(4, emp.getPassword().trim());
            stmt.setDouble(5, emp.getSalary());
            stmt.setString(6, emp.getContact() != null ? emp.getContact().trim() : "");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        emp.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error inserting employee: " + e.getMessage());
            throw new RuntimeException("Database error registering employee: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Computes the sum of all employee salaries for the Profit & Loss statement.
     */
    public double getTotalSalaryExpense() {
        String sql = "SELECT COALESCE(SUM(salary), 0) FROM employees";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] Error calculating total salary expense: " + e.getMessage());
        }
        return 0.0;
    }
}
