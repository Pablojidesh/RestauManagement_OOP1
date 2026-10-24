package com.restaurant;

import com.restaurant.model.*;
import com.restaurant.model.MenuItem;
import com.restaurant.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;

/**
 * SystemVerificationTest
 * 
 * End-to-end automated verification suite testing all acceptance criteria:
 * 1. Role-based Authentication for ADMIN, KITCHEN, WAITER, and invalid credentials.
 * 2. Category creation and dynamic menu item registration with validation.
 * 3. Case-insensitive live menu search (verifying seeded "Paneer" dishes).
 * 4. Customer registration and table management.
 * 5. Order cart placement with multiple dishes and quantities.
 * 6. Dynamic Polymorphic Payments (Cash, Card, UPI) verifying distinct confirmations
 *    and exact financial calculation: (subtotal - discount) + tax on discounted amount.
 * 7. Kitchen order status lifecycle progression (Placed -> Preparing -> Served).
 * 8. Inventory management, restocking, and proactive low-stock alert detection.
 * 9. Role-based salary privacy, fresh database salary polling, and admin access control.
 * 10. Admin Profit & Loss reporting and strict data consistency with bills.
 */
public class SystemVerificationTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("           RESTAURANT MANAGEMENT SYSTEM - END-TO-END VERIFICATION               ");
        System.out.println("================================================================================");

        RestaurantService service = new RestaurantService();

        try {
            testAuthentication(service);
            testAdminCategoryAndMenu(service);
            testValidationRules(service);
            testWaiterCustomerAndPolymorphicOrders(service);
            testKitchenWorkflow(service);
            testInventoryAndLowStockAlerts(service);
            testSalaryIsolationAndUpdates(service);
            testProfitAndLossConsistency(service);
            testConsolidatedReport(service);

            System.out.println("\n================================================================================");
            System.out.printf("  VERIFICATION RESULT: %d / %d TESTS PASSED SUCCESSFULLY!%n", testsPassed, testsTotal);
            System.out.println("================================================================================");
            if (testsPassed != testsTotal) {
                System.exit(1);
            }
        } catch (Throwable t) {
            System.err.println("\nFATAL TEST FAILURE:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %-60s%n", testName);
        } else {
            System.err.printf("  [FAIL] %-60s -> %s%n", testName, message);
        }
    }

    // =========================================================================
    // 1. Authentication Tests
    // =========================================================================
    private static void testAuthentication(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 1: Role-Based Authentication & Access Control ---");

        // Admin login
        Employee admin = service.login("admin", "admin123");
        assertTrue("Admin login with admin/admin123", admin != null && admin.isAdmin(), "Admin should authenticate");

        // Kitchen login
        Employee chef = service.login("chef1", "chef123");
        assertTrue("Kitchen login with chef1/chef123", chef != null && chef.isKitchen(), "Chef should authenticate");

        // Waiter login
        Employee waiter = service.login("waiter1", "waiter123");
        assertTrue("Waiter login with waiter1/waiter123", waiter != null && waiter.isWaiter(), "Waiter should authenticate");

        // Invalid login rejection
        Employee invalid = service.login("admin", "wrongpassword");
        assertTrue("Invalid password rejection", invalid == null, "Invalid password should return null");
    }

    // =========================================================================
    // 2. Category & Menu Management
    // =========================================================================
    private static void testAdminCategoryAndMenu(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 2: Category & Menu Management ---");

        service.login("admin", "admin123");

        // Seeded categories check
        List<Category> categories = service.getAllCategories();
        assertTrue("Seeded categories count >= 5", categories.size() >= 5, "Categories should be >= 5");

        // Add new Category
        String newCatName = "Chef Specials " + System.currentTimeMillis();
        int catId = service.addCategory(newCatName);
        assertTrue("Add new category", catId > 0, "New category ID should be > 0");

        // Add dish under new category
        int dishId = service.addMenuItem("Royal Saffron Platter", catId, 450.00, "Chef signature dish");
        assertTrue("Add dish under new category", dishId > 0, "New dish ID should be > 0");

        // Verify dish appears in menu
        List<MenuItem> allDishes = service.getAllMenuItems();
        boolean foundNewDish = false;
        for (MenuItem m : allDishes) {
            if ("Royal Saffron Platter".equals(m.getName())) {
                foundNewDish = true;
                break;
            }
        }
        assertTrue("New dish appears in menu catalog", foundNewDish, "Dish should exist in catalog");

        // Live Search Test (Case-insensitive matching "paneer")
        List<MenuItem> paneerMatches = service.searchMenuItems("paneer");
        assertTrue("Live search finds 'Paneer' dishes", !paneerMatches.isEmpty(), "Should find dishes with 'Paneer'");
        boolean hasPaneer = false;
        for (MenuItem p : paneerMatches) {
            if (p.getName().toLowerCase().contains("paneer")) {
                hasPaneer = true;
            }
        }
        assertTrue("Search results genuinely contain 'Paneer'", hasPaneer, "Search results must match keyword");
    }

    // =========================================================================
    // 3. Defensive Validation Rules
    // =========================================================================
    private static void testValidationRules(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 3: Data Integrity & Defensive Validation Rules ---");
        service.login("admin", "admin123");

        // Empty dish name rejection
        boolean caughtEmptyName = false;
        try {
            service.addMenuItem("", 1, 100.0, "desc");
        } catch (IllegalArgumentException e) {
            caughtEmptyName = true;
        }
        assertTrue("Reject dish with blank name", caughtEmptyName, "Should reject empty dish name");

        // Negative or zero price rejection
        boolean caughtNegativePrice = false;
        try {
            service.addMenuItem("Invalid Item", 1, -50.0, "desc");
        } catch (IllegalArgumentException e) {
            caughtNegativePrice = true;
        }
        assertTrue("Reject dish with negative price", caughtNegativePrice, "Should reject negative price");

        // Invalid category rejection
        boolean caughtInvalidCat = false;
        try {
            service.addMenuItem("Invalid Cat Item", -1, 100.0, "desc");
        } catch (IllegalArgumentException e) {
            caughtInvalidCat = true;
        }
        assertTrue("Reject dish with invalid category ID", caughtInvalidCat, "Should reject invalid category");
    }

    // =========================================================================
    // 4. Waiter Orders & Dynamic Polymorphic Payments (Cash, Card, UPI)
    // =========================================================================
    private static void testWaiterCustomerAndPolymorphicOrders(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 4: Customer Registration & Polymorphic Payments ---");
        service.login("waiter1", "waiter123");

        // Customer Registration
        String custName = "Vikram Malhotra";
        String contact = "9876501234";
        int custId = service.registerCustomer(custName, contact);
        assertTrue("Customer registration", custId > 0, "Customer ID should be positive");

        // Tables Check (Must loop over and display ALL tables)
        List<RestaurantTable> tables = service.getAllTables();
        assertTrue("All tables returned (>= 5 tables)", tables.size() >= 5, "Tables count should be >= 5");
        int tableId = tables.get(0).getId();

        // Dishes selection for cart (at least 2 different dishes)
        List<MenuItem> menu = service.getAllMenuItems();
        assertTrue("Menu has at least 2 dishes", menu.size() >= 2, "Menu items must be >= 2");
        MenuItem dish1 = menu.get(0);
        MenuItem dish2 = menu.get(1);

        // --- ORDER 1: CASH PAYMENT ---
        List<OrderItem> cart1 = new ArrayList<>();
        cart1.add(new OrderItem(dish1.getId(), dish1.getName(), 2, dish1.getPrice()));
        cart1.add(new OrderItem(dish2.getId(), dish2.getName(), 1, dish2.getPrice()));
        double subtotal1 = (2 * dish1.getPrice()) + (1 * dish2.getPrice());

        int order1Id = service.placeOrder(tableId, custId, cart1);
        assertTrue("Place Order 1 with multiple dishes", order1Id > 0, "Order 1 ID should be > 0");

        double discount1 = 10.0; // 10%
        double tax1 = 5.0;       // 5%
        // Expected grand total = (subtotal - discount) + tax on discounted amount
        double discAmt1 = subtotal1 * 0.10;
        double discSub1 = subtotal1 - discAmt1;
        double taxAmt1 = discSub1 * 0.05;
        double expectedGrandTotal1 = discSub1 + taxAmt1;

        // Polymorphic Cash Payment instance
        double tendered1 = expectedGrandTotal1 + 100.0; // Extra cash
        Payment cashPayment = new CashPayment(order1Id, expectedGrandTotal1, tendered1);

        // Polymorphic call via Payment reference
        Receipt receipt1 = service.checkoutOrder(order1Id, subtotal1, discount1, tax1, cashPayment);

        assertTrue("Cash Checkout Grand Total Matches Formula",
            Math.abs(receipt1.getGrandTotal() - expectedGrandTotal1) < 0.01, "Grand total formula check");
        assertTrue("Cash Receipt contains Cashier Change Info",
            receipt1.getPaymentConfirmation().contains("CASH PAYMENT PROCESSED") &&
            receipt1.getPaymentConfirmation().contains("Change Returned:"), "Cash confirmation text check");

        // --- ORDER 2: CARD PAYMENT ---
        List<OrderItem> cart2 = new ArrayList<>();
        cart2.add(new OrderItem(dish1.getId(), dish1.getName(), 1, dish1.getPrice()));
        cart2.add(new OrderItem(dish2.getId(), dish2.getName(), 2, dish2.getPrice()));
        double subtotal2 = (1 * dish1.getPrice()) + (2 * dish2.getPrice());

        int order2Id = service.placeOrder(tables.get(1).getId(), custId, cart2);
        assertTrue("Place Order 2 with multiple dishes", order2Id > 0, "Order 2 ID should be > 0");

        double discount2 = 5.0; // 5%
        double tax2 = 5.0;      // 5%
        double discAmt2 = subtotal2 * 0.05;
        double discSub2 = subtotal2 - discAmt2;
        double expectedGrandTotal2 = discSub2 + (discSub2 * 0.05);

        // Polymorphic Card Payment instance
        Payment cardPayment = new CardPayment(order2Id, expectedGrandTotal2, "4111-2222-3333-9876", "Vikram Malhotra", "Visa");
        Receipt receipt2 = service.checkoutOrder(order2Id, subtotal2, discount2, tax2, cardPayment);

        assertTrue("Card Checkout Grand Total Matches Formula",
            Math.abs(receipt2.getGrandTotal() - expectedGrandTotal2) < 0.01, "Grand total formula check");
        assertTrue("Card Receipt contains Masked PAN and Auth Code",
            receipt2.getPaymentConfirmation().contains("CARD PAYMENT AUTHORIZED") &&
            receipt2.getPaymentConfirmation().contains("**** **** **** 9876") &&
            receipt2.getPaymentConfirmation().contains("AUTH-"), "Card confirmation text check");

        // --- ORDER 3: UPI PAYMENT ---
        List<OrderItem> cart3 = new ArrayList<>();
        cart3.add(new OrderItem(dish1.getId(), dish1.getName(), 3, dish1.getPrice()));
        double subtotal3 = (3 * dish1.getPrice());

        int order3Id = service.placeOrder(tables.get(2).getId(), custId, cart3);
        assertTrue("Place Order 3", order3Id > 0, "Order 3 ID should be > 0");

        double discount3 = 0.0;
        double tax3 = 5.0;
        double expectedGrandTotal3 = subtotal3 + (subtotal3 * 0.05);

        // Polymorphic UPI Payment instance
        Payment upiPayment = new UPIPayment(order3Id, expectedGrandTotal3, "vikram@okhdfcbank");
        Receipt receipt3 = service.checkoutOrder(order3Id, subtotal3, discount3, tax3, upiPayment);

        assertTrue("UPI Checkout Grand Total Matches Formula",
            Math.abs(receipt3.getGrandTotal() - expectedGrandTotal3) < 0.01, "Grand total formula check");
        assertTrue("UPI Receipt contains UPI VPA ID and Bank Reference",
            receipt3.getPaymentConfirmation().contains("UPI PAYMENT COMPLETED") &&
            receipt3.getPaymentConfirmation().contains("vikram@okhdfcbank") &&
            receipt3.getPaymentConfirmation().contains("UPI-REF-"), "UPI confirmation text check");

        // Distinct Confirmation Texts Verification
        boolean distinct = !receipt1.getPaymentConfirmation().equals(receipt2.getPaymentConfirmation()) &&
                           !receipt2.getPaymentConfirmation().equals(receipt3.getPaymentConfirmation());
        assertTrue("Receipt confirmation messages are genuinely different across payment methods",
            distinct, "Each payment subclass must generate unique audit output");
    }

    // =========================================================================
    // 5. Kitchen Workflow
    // =========================================================================
    private static void testKitchenWorkflow(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 5: Kitchen Order Status Transitions ---");

        // Waiter places an order
        service.login("waiter1", "waiter123");
        List<MenuItem> menu = service.getAllMenuItems();
        List<OrderItem> cart = new ArrayList<>();
        cart.add(new OrderItem(menu.get(0).getId(), menu.get(0).getName(), 1, menu.get(0).getPrice()));
        int tableId = service.getAllTables().get(3).getId();
        int customerId = service.getAllCustomers().get(0).getId();
        int orderId = service.placeOrder(tableId, customerId, cart);

        // Kitchen logs in
        service.login("chef1", "chef123");
        Order placedOrder = service.getOrderById(orderId);
        assertTrue("Kitchen views newly placed order (Status: Placed)",
            placedOrder != null && "Placed".equals(placedOrder.getStatus()), "Order initial status Placed");

        // Placed -> Preparing
        service.updateOrderStatus(orderId, "Preparing");
        Order prepOrder = service.getOrderById(orderId);
        assertTrue("Order status updated: Preparing",
            prepOrder != null && "Preparing".equals(prepOrder.getStatus()), "Order status Preparing");

        // Preparing -> Served
        service.updateOrderStatus(orderId, "Served");
        Order servedOrder = service.getOrderById(orderId);
        assertTrue("Order status updated: Served",
            servedOrder != null && "Served".equals(servedOrder.getStatus()), "Order status Served");
    }

    // =========================================================================
    // 6. Inventory & Low Stock Alerts
    // =========================================================================
    private static void testInventoryAndLowStockAlerts(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 6: Inventory Management & Stock-Out Alerts ---");
        service.login("admin", "admin123");

        // Check seeded low stock item (Fresh Paneer qty 2.50 <= threshold 5.00)
        List<InventoryItem> lowItems = service.getLowStockItems();
        assertTrue("Low-stock item detected immediately on startup",
            !lowItems.isEmpty(), "At least one item must be low stock");
        assertTrue("Proactive low-stock alert triggered",
            service.hasLowStockAlert(), "Admin alert banner condition met");

        // Restock item
        InventoryItem lowItem = lowItems.get(0);
        double oldQty = lowItem.getQuantity();
        service.restockItem(lowItem.getId(), 10.0);

        List<InventoryItem> allItems = service.getAllInventoryItems();
        double newQty = 0.0;
        for (InventoryItem it : allItems) {
            if (it.getId() == lowItem.getId()) {
                newQty = it.getQuantity();
                break;
            }
        }
        assertTrue("Inventory restock successfully increases quantity",
            newQty >= oldQty + 10.0, "Quantity should increase by 10");
    }

    // =========================================================================
    // 7. Salary Privacy, Fresh Database Fetching, & Admin Security
    // =========================================================================
    private static void testSalaryIsolationAndUpdates(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 7: Salary Privacy & Role Security ---");

        // 1. Waiter views own salary
        service.login("waiter1", "waiter123");
        double waiterInitialSal = service.getFreshSalaryForCurrentEmployee();
        assertTrue("Waiter can view own fresh salary (> 0)", waiterInitialSal > 0, "Waiter salary should be positive");

        // Waiter blocked from admin methods
        boolean waiterBlocked = false;
        try {
            service.getAllEmployees();
        } catch (SecurityException se) {
            waiterBlocked = true;
        }
        assertTrue("Non-admin (Waiter) blocked from viewing all employee salaries",
            waiterBlocked, "SecurityException expected");

        // 2. Chef views own salary
        service.login("chef1", "chef123");
        double chefInitialSal = service.getFreshSalaryForCurrentEmployee();
        assertTrue("Kitchen chef can view own fresh salary (> 0)", chefInitialSal > 0, "Chef salary should be positive");

        // Chef blocked from admin methods
        boolean chefBlocked = false;
        try {
            service.updateEmployeeSalary(1, 99999);
        } catch (SecurityException se) {
            chefBlocked = true;
        }
        assertTrue("Non-admin (Chef) blocked from modifying salaries",
            chefBlocked, "SecurityException expected");

        // 3. Admin updates Waiter salary
        service.login("admin", "admin123");
        int waiterEmpId = -1;
        for (Employee emp : service.getAllEmployees()) {
            if ("waiter1".equals(emp.getUsername())) {
                waiterEmpId = emp.getId();
                break;
            }
        }
        assertTrue("Admin located waiter employee record", waiterEmpId > 0, "Waiter ID found");

        double updatedSalary = 29500.00;
        service.updateEmployeeSalary(waiterEmpId, updatedSalary);

        // 4. Waiter logs back in and fetches fresh salary from DB
        service.login("waiter1", "waiter123");
        double waiterFreshSal = service.getFreshSalaryForCurrentEmployee();
        assertTrue("Waiter fetches updated salary fresh from DB via employee ID",
            Math.abs(waiterFreshSal - updatedSalary) < 0.01, "Fresh salary must reflect DB update");
    }

    // =========================================================================
    // 8. Profit & Loss Consistency
    // =========================================================================
    private static void testProfitAndLossConsistency(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 8: Admin Profit & Loss and Order Consistency ---");
        service.login("admin", "admin123");

        ProfitLossReport pnl = service.getProfitLossReport();
        int dbOrderCount = service.getTotalOrderCount();
        List<Order> ordersList = service.getAllOrders();

        assertTrue("Admin dashboard order count equals All Orders table count",
            pnl.getTotalOrders() == ordersList.size(), "Order counts must match exactly");
        assertTrue("Total Revenue is non-negative and derived from bills",
            pnl.getTotalRevenue() > 0, "Revenue should be positive after checkouts");
        assertTrue("Total Salary Expense is positive",
            pnl.getTotalSalaryExpense() > 0, "Salaries expense should be > 0");
        assertTrue("Total Inventory Valuation is positive",
            pnl.getTotalInventoryCost() > 0, "Inventory cost should be > 0");

        double expectedNet = pnl.getTotalRevenue() - pnl.getTotalSalaryExpense() - pnl.getTotalInventoryCost();
        assertTrue("Net Profit/Loss equals (Revenue - Salaries - Inventory Cost)",
            Math.abs(pnl.getNetAmount() - expectedNet) < 0.01, "Net formula check");
        assertTrue("P&L status label matches sign",
            (pnl.isProfit() && pnl.getStatusLabel().equals("Net Profit")) ||
            (!pnl.isProfit() && pnl.getStatusLabel().equals("Net Loss")), "Label matches net sign");
    }

    // =========================================================================
    // 9. Consolidated Reports Dashboard Verification
    // =========================================================================
    private static void testConsolidatedReport(RestaurantService service) {
        System.out.println("\n--- TEST SUITE 9: Consolidated Reports Dashboard & Data Agreement ---");
        
        // 1. Role Security Check: Non-Admin blocked
        service.login("chef1", "chef123");
        boolean chefBlocked = false;
        try {
            service.getConsolidatedReport();
        } catch (SecurityException se) {
            chefBlocked = true;
        }
        assertTrue("Non-admin (Chef) strictly forbidden from accessing Consolidated Report",
            chefBlocked, "SecurityException must be raised for non-admin");

        // 2. Admin retrieves Consolidated Report
        service.login("admin", "admin123");
        ConsolidatedReport rep = service.getConsolidatedReport();
        assertTrue("Admin successfully generates ConsolidatedReport", rep != null, "Report must not be null");

        // 3. Mathematical agreement: Revenue breakdown matches total revenue
        double calculatedRev = rep.getCashRevenue() + rep.getCardRevenue() + rep.getUpiRevenue();
        assertTrue("Revenue Breakdown (Cash + Card + UPI) exactly equals Total Revenue",
            Math.abs(rep.getTotalRevenue() - calculatedRev) < 0.01,
            String.format("Expected %.2f, Got %.2f", calculatedRev, rep.getTotalRevenue()));

        // 4. Mathematical agreement: Bills count breakdown matches total bills
        int calculatedBills = rep.getCashBillsCount() + rep.getCardBillsCount() + rep.getUpiBillsCount();
        assertTrue("Bill count breakdown (Cash + Card + UPI) exactly equals Total Bills Count",
            rep.getTotalBillsCount() == calculatedBills,
            String.format("Expected %d, Got %d", calculatedBills, rep.getTotalBillsCount()));

        // 5. Operating expenses calculation
        double calculatedExpenses = rep.getTotalSalaryExpense() + rep.getTotalInventoryCost();
        assertTrue("Operating Expenses exactly equal (Salaries + Inventory Asset)",
            Math.abs(rep.getTotalOperatingExpenses() - calculatedExpenses) < 0.01, "Expenses formula check");

        // 6. Net profit/loss bottom line calculation
        double expectedNet = rep.getTotalRevenue() - rep.getTotalOperatingExpenses();
        assertTrue("Net Profit/Loss equals (Total Revenue - Total Operating Expenses)",
            Math.abs(rep.getNetProfitLoss() - expectedNet) < 0.01, "Net Profit/Loss formula check");
        assertTrue("Report isProfit boolean matches net amount sign",
            rep.isProfit() == (rep.getNetProfitLoss() >= 0), "isProfit flag consistency");

        // 7. Orders pipeline agreement with DB
        List<Order> allOrders = service.getAllOrders();
        assertTrue("Report Total Orders equals Database Orders Count",
            rep.getTotalOrdersCount() == allOrders.size(), "Order count matches database");
        int pipelineSum = rep.getPlacedOrdersCount() + rep.getPreparingOrdersCount() + 
                          rep.getServedOrdersCount() + rep.getCompletedOrdersCount();
        assertTrue("Pipeline Status breakdown accounts for all orders",
            pipelineSum == rep.getTotalOrdersCount(), "All orders mapped to valid statuses");

        // 8. Inventory & Low Stock Agreement
        List<InventoryItem> allItems = service.getAllInventoryItems();
        List<InventoryItem> lowItems = service.getLowStockItems();
        assertTrue("Report Total Inventory Items count equals DB inventory items",
            rep.getTotalInventoryItemsCount() == allItems.size(), "Inventory item count matches");
        assertTrue("Report Low Stock count equals DB low stock items count",
            rep.getLowStockItemsCount() == lowItems.size(), "Low stock count matches");

        // 9. Staff Payroll Table Agreement
        List<Employee> allEmployees = service.getAllEmployees();
        assertTrue("Report Employee list contains all registered staff",
            rep.getEmployeeList() != null && rep.getEmployeeList().size() == allEmployees.size(),
            "Employee payroll list size matches");
    }
}
