package com.restaurant.service;

import com.restaurant.dao.*;
import com.restaurant.model.*;

import java.util.Date;
import java.util.List;

/**
 * RestaurantService
 * 
 * Central Business Logic Layer for the Restaurant Management System.
 * Mediates all operations between the Swing UI Layer and the Data Access (DAO) Layer.
 * The Swing UI never issues SQL queries directly; it interacts solely with this service.
 * 
 * Demonstrates:
 * 1. ENCAPSULATION of business rules, validation, and session state.
 * 2. DYNAMIC POLYMORPHISM via the checkoutOrder method, which processes payments
 *    strictly through a Payment reference (resolving to CashPayment, CardPayment,
 *    or UPIPayment at runtime).
 * 3. ROLE-BASED ACCESS CONTROL and salary privacy enforcement.
 */
public class RestaurantService {

    private final CategoryDAO categoryDAO;
    private final MenuDAO menuDAO;
    private final TableDAO tableDAO;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final BillDAO billDAO;
    private final EmployeeDAO employeeDAO;
    private final InventoryDAO inventoryDAO;

    // Currently logged-in employee session
    private Employee currentUser;

    public RestaurantService() {
        this.categoryDAO = new CategoryDAO();
        this.menuDAO = new MenuDAO();
        this.tableDAO = new TableDAO();
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.billDAO = new BillDAO();
        this.employeeDAO = new EmployeeDAO();
        this.inventoryDAO = new InventoryDAO();
    }

    // =========================================================================
    // Authentication & Session Management
    // =========================================================================

    public Employee login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        Employee emp = employeeDAO.authenticate(username, password);
        if (emp != null) {
            this.currentUser = emp;
        }
        return emp;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Employee user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    /**
     * Role-protected salary retrieval:
     * Non-admin roles (KITCHEN, WAITER) can ONLY fetch their own salary,
     * fetched fresh from the database using their session employee ID.
     */
    public double getFreshSalaryForCurrentEmployee() {
        if (currentUser == null) {
            throw new IllegalStateException("No active user session.");
        }
        Employee fresh = employeeDAO.getEmployeeById(currentUser.getId());
        if (fresh != null) {
            currentUser.setSalary(fresh.getSalary());
            return fresh.getSalary();
        }
        return currentUser.getSalary();
    }

    // =========================================================================
    // Category & Menu Management
    // =========================================================================

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public int addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be blank.");
        }
        if (categoryDAO.categoryExists(name)) {
            throw new IllegalArgumentException("Category '" + name.trim() + "' already exists.");
        }
        return categoryDAO.addCategory(new Category(name.trim()));
    }

    public List<MenuItem> getAllMenuItems() {
        return menuDAO.getAllMenuItems();
    }

    public List<MenuItem> searchMenuItems(String keyword) {
        return menuDAO.searchMenuItems(keyword);
    }

    public int addMenuItem(String name, int categoryId, double price, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Dish name cannot be blank.");
        }
        if (categoryId <= 0) {
            throw new IllegalArgumentException("A valid category must be selected.");
        }
        if (price <= 0.0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        MenuItem item = new MenuItem(name.trim(), categoryId, price, description != null ? description.trim() : "");
        return menuDAO.addMenuItem(item);
    }

    // =========================================================================
    // Table Management
    // =========================================================================

    public List<RestaurantTable> getAllTables() {
        return tableDAO.getAllTables();
    }

    public boolean updateTableStatus(int tableId, String status) {
        return tableDAO.updateTableStatus(tableId, status);
    }

    // =========================================================================
    // Customer Management
    // =========================================================================

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public int registerCustomer(String name, String contact) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer contact cannot be empty.");
        }
        return customerDAO.addCustomer(new Customer(name.trim(), contact.trim()));
    }

    // =========================================================================
    // Order Workflow
    // =========================================================================

    public int placeOrder(int tableId, int customerId, List<OrderItem> items) {
        if (tableId <= 0) {
            throw new IllegalArgumentException("Please select a valid table.");
        }
        if (customerId <= 0) {
            throw new IllegalArgumentException("Please select a registered customer.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty. Please add at least one dish.");
        }

        double subtotal = 0.0;
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be at least 1.");
            }
            subtotal += item.getSubtotal();
        }

        Order order = new Order();
        order.setTableId(tableId);
        order.setCustomerId(customerId);
        order.setStatus("Placed");
        order.setTotalAmount(subtotal);

        return orderDAO.createOrder(order, items);
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public List<Order> getActiveOrders() {
        return orderDAO.getActiveOrders();
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (updated && "Completed".equalsIgnoreCase(newStatus)) {
            Order o = orderDAO.getOrderById(orderId);
            if (o != null) {
                tableDAO.updateTableStatus(o.getTableId(), "AVAILABLE");
            }
        }
        return updated;
    }

    public Order getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }

    public int getTotalOrderCount() {
        return orderDAO.getTotalOrderCount();
    }

    // =========================================================================
    // Polymorphic Billing & Checkout
    // =========================================================================

    /**
     * Executes the order checkout process demonstrating DYNAMIC POLYMORPHISM.
     * The payment parameter is typed as the abstract base class Payment.
     * At runtime, payment.processPayment() dynamically dispatches to the concrete
     * implementation (CashPayment, CardPayment, or UPIPayment).
     *
     * @param orderId ID of order to check out
     * @param subtotal Cart subtotal before discounts and taxes
     * @param discountPercent Waiter-specified discount percentage
     * @param taxPercent Tax percentage
     * @param payment Polymorphic Payment instance
     * @return Generated complete Receipt object
     */
    public Receipt checkoutOrder(int orderId, double subtotal, double discountPercent,
                                  double taxPercent, Payment payment) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID.");
        }
        if (payment == null) {
            throw new IllegalArgumentException("Payment details cannot be null.");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if (taxPercent < 0) {
            throw new IllegalArgumentException("Tax percentage cannot be negative.");
        }

        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order #" + orderId + " not found.");
        }

        // Financial Calculations:
        // Grand total = (subtotal - discount) + tax on the discounted amount
        double discountAmount = subtotal * (discountPercent / 100.0);
        double discountedSubtotal = subtotal - discountAmount;
        double taxAmount = discountedSubtotal * (taxPercent / 100.0);
        double grandTotal = discountedSubtotal + taxAmount;

        // Configure payment amount
        payment.setAmount(grandTotal);
        payment.setOrderId(orderId);

        // =====================================================================
        // CORE OOP POLYMORPHIC CALL:
        // Dynamic method resolution executes CashPayment.processPayment(),
        // CardPayment.processPayment(), or UPIPayment.processPayment()
        // =====================================================================
        String confirmationMessage = payment.processPayment();

        // Persist Bill
        Bill bill = new Bill(
            orderId,
            subtotal,
            discountPercent,
            taxPercent,
            grandTotal,
            payment.getPaymentMethod(),
            confirmationMessage
        );
        billDAO.createBill(bill);

        // Update Order to Completed and free table
        orderDAO.updateOrderStatus(orderId, "Completed");
        tableDAO.updateTableStatus(order.getTableId(), "AVAILABLE");

        // Construct complete Receipt
        return new Receipt(
            orderId,
            order.getTableNumber(),
            order.getCustomerName(),
            new Date(),
            order.getItems(),
            subtotal,
            discountPercent,
            discountAmount,
            taxPercent,
            taxAmount,
            grandTotal,
            payment.getPaymentMethod(),
            confirmationMessage
        );
    }

    // =========================================================================
    // Inventory Management & Alerts
    // =========================================================================

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryDAO.getAllInventoryItems();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryDAO.getLowStockItems();
    }

    public boolean hasLowStockAlert() {
        return !inventoryDAO.getLowStockItems().isEmpty();
    }

    public int addInventoryItem(String name, String unit, double quantity, double threshold, double costPerUnit) {
        InventoryItem item = new InventoryItem(name, unit, quantity, threshold, costPerUnit);
        return inventoryDAO.addInventoryItem(item);
    }

    public boolean restockItem(int itemId, double additionalQuantity) {
        return inventoryDAO.restockItem(itemId, additionalQuantity);
    }

    // =========================================================================
    // Salary Management (Admin only)
    // =========================================================================

    public List<Employee> getAllEmployees() {
        assertAdminAccess();
        return employeeDAO.getAllEmployees();
    }

    public boolean updateEmployeeSalary(int employeeId, double newSalary) {
        assertAdminAccess();
        return employeeDAO.updateSalary(employeeId, newSalary);
    }

    public int addEmployee(String name, String role, String username, String password, double salary, String contact) {
        assertAdminAccess();
        Employee emp = new Employee(name, role, username, password, salary, contact);
        return employeeDAO.addEmployee(emp);
    }

    private void assertAdminAccess() {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Access Denied: Only Administrator can access staff salary records.");
        }
    }

    // =========================================================================
    // Profit & Loss Reporting (Admin only)
    // =========================================================================

    public ProfitLossReport getProfitLossReport() {
        assertAdminAccess();
        double revenue = billDAO.getTotalRevenue();
        double salaries = employeeDAO.getTotalSalaryExpense();
        double inventoryCost = inventoryDAO.getTotalInventoryCost();
        int orderCount = orderDAO.getTotalOrderCount();
        return new ProfitLossReport(revenue, salaries, inventoryCost, orderCount);
    }

    /**
     * Generates a unified Consolidated Report aggregating revenue by payment method,
     * operating expenses, order pipeline breakdown, inventory alerts, and payroll overview.
     */
    public ConsolidatedReport getConsolidatedReport() {
        assertAdminAccess();
        ConsolidatedReport report = new ConsolidatedReport();

        // 1. Revenue by Payment Method from Bills
        List<Bill> bills = billDAO.getAllBills();
        double totalRev = 0.0, cashRev = 0.0, cardRev = 0.0, upiRev = 0.0;
        int cashCount = 0, cardCount = 0, upiCount = 0;

        for (Bill b : bills) {
            totalRev += b.getGrandTotal();
            String pm = b.getPaymentMethod() != null ? b.getPaymentMethod().toUpperCase() : "";
            if (pm.contains("CASH")) {
                cashRev += b.getGrandTotal();
                cashCount++;
            } else if (pm.contains("CARD")) {
                cardRev += b.getGrandTotal();
                cardCount++;
            } else if (pm.contains("UPI")) {
                upiRev += b.getGrandTotal();
                upiCount++;
            }
        }
        report.setTotalRevenue(totalRev);
        report.setCashRevenue(cashRev);
        report.setCardRevenue(cardRev);
        report.setUpiRevenue(upiRev);
        report.setTotalBillsCount(bills.size());
        report.setCashBillsCount(cashCount);
        report.setCardBillsCount(cardCount);
        report.setUpiBillsCount(upiCount);

        // 2. Operating Expenses & Net Valuation
        double salaries = employeeDAO.getTotalSalaryExpense();
        double inventoryCost = inventoryDAO.getTotalInventoryCost();
        report.setTotalSalaryExpense(salaries);
        report.setTotalInventoryCost(inventoryCost);
        report.setTotalOperatingExpenses(salaries + inventoryCost);
        report.setNetProfitLoss(totalRev - (salaries + inventoryCost));

        // 3. Order Pipeline Status Breakdown & Daily Velocity
        List<Order> orders = orderDAO.getAllOrders();
        report.setTotalOrdersCount(orders.size());
        int placed = 0, preparing = 0, served = 0, completed = 0, today = 0;

        java.time.LocalDate todayDate = java.time.LocalDate.now();
        for (Order o : orders) {
            String st = o.getStatus() != null ? o.getStatus().trim() : "";
            if ("Placed".equalsIgnoreCase(st)) placed++;
            else if ("Preparing".equalsIgnoreCase(st)) preparing++;
            else if ("Served".equalsIgnoreCase(st)) served++;
            else if ("Completed".equalsIgnoreCase(st)) completed++;

            if (o.getCreatedAt() != null) {
                java.time.LocalDate orderDate = o.getCreatedAt().toLocalDateTime().toLocalDate();
                if (orderDate.equals(todayDate)) {
                    today++;
                }
            }
        }
        report.setPlacedOrdersCount(placed);
        report.setPreparingOrdersCount(preparing);
        report.setServedOrdersCount(served);
        report.setCompletedOrdersCount(completed);
        report.setTodayOrdersCount(today);

        // 4. Pantry Stock & Low Stock Items Count
        List<InventoryItem> invItems = inventoryDAO.getAllInventoryItems();
        report.setTotalInventoryItemsCount(invItems.size());
        int lowCount = 0;
        for (InventoryItem it : invItems) {
            if (it.isLowStock()) lowCount++;
        }
        report.setLowStockItemsCount(lowCount);

        // 5. Staff Payroll Snapshot
        report.setEmployeeList(employeeDAO.getAllEmployees());

        return report;
    }
}
