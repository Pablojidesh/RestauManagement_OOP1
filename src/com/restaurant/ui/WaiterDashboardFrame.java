package com.restaurant.ui;

import com.restaurant.model.*;
import com.restaurant.model.MenuItem;
import com.restaurant.service.RestaurantService;
import com.restaurant.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WaiterDashboardFrame
 * 
 * Waiter workspace managing Customer Registration, Live Menu Search, Cart Assembly,
 * and Polymorphic Order Checkout & Receipt Generation.
 * 
 * Dynamic Polymorphism Showcase:
 * When the user checks out with Cash, Card, or UPI, the respective concrete subclass
 * (CashPayment, CardPayment, UPIPayment) is instantiated and passed to the service layer
 * as a Payment reference, executing processPayment() via runtime dynamic binding.
 */
public class WaiterDashboardFrame extends JFrame {

    private final RestaurantService service;

    // Customer Registration Components
    private JTextField custNameField;
    private JTextField custContactField;
    private DefaultTableModel customerTableModel;

    // Order Placement & Cart Components
    private JComboBox<RestaurantTable> tableCombo;
    private JComboBox<Customer> customerCombo;
    private JTextField dishSearchField;
    private DefaultTableModel menuCatalogTableModel;
    private JTable menuCatalogTable;
    private List<MenuItem> currentCatalogItems;
    private JSpinner quantitySpinner;

    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private final List<OrderItem> cartItems = new ArrayList<>();

    // Billing Summary Labels & Fields
    private JLabel subtotalLabel;
    private JTextField discountField;
    private JTextField taxField;
    private JLabel grandTotalEstimatedLabel;

    // Payment Selection & Dynamic Inputs
    private JComboBox<String> paymentMethodCombo;
    private JPanel paymentDetailsCard;
    private CardLayout paymentCardLayout;

    // Cash Fields
    private JTextField cashTenderedField;

    // Card Fields
    private JTextField cardNumberField;
    private JTextField cardHolderField;
    private JComboBox<String> cardTypeCombo;

    // UPI Fields
    private JTextField upiIdField;

    // Waiter Salary View
    private JLabel waiterSalaryLabel;

    public WaiterDashboardFrame(RestaurantService service) {
        super("The Royal Spice - Waiter Service Portal");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 780);
        setLocationRelativeTo(null);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIUtils.COLOR_BG);

        // Header Navigation
        JPanel headerPanel = UIUtils.createHeaderPanel(
            "Dining Hall & POS Station",
            "Serving Waiter",
            service.getCurrentUser().getName(),
            () -> {
                service.logout();
                new LoginFrame(service).setVisible(true);
                dispose();
            }
        );
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.addTab("Place Order & Checkout", buildOrderPOSPanel());
        tabbedPane.addTab("Register Customer", buildCustomerRegistrationPanel());
        tabbedPane.addTab("My Salary & Profile", buildSalaryPanel());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    // =========================================================================
    // Tab 1: Place Order & POS Checkout
    // =========================================================================
    private JPanel buildOrderPOSPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Bar: Table & Customer Selection
        JPanel topSelectionBar = UIUtils.createCardPanel();
        topSelectionBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JLabel tblLbl = new JLabel("Dining Table:");
        tblLbl.setFont(UIUtils.FONT_BOLD);
        tableCombo = new JComboBox<>();
        tableCombo.setFont(UIUtils.FONT_REGULAR);
        tableCombo.setPreferredSize(new Dimension(200, 30));

        JLabel custLbl = new JLabel("Guest / Customer:");
        custLbl.setFont(UIUtils.FONT_BOLD);
        customerCombo = new JComboBox<>();
        customerCombo.setFont(UIUtils.FONT_REGULAR);
        customerCombo.setPreferredSize(new Dimension(220, 30));

        JButton refreshOptionsBtn = UIUtils.createButton("Refresh Lists", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refreshOptionsBtn.addActionListener(e -> {
            refreshTablesList();
            refreshCustomersList();
        });

        topSelectionBar.add(tblLbl);
        topSelectionBar.add(tableCombo);
        topSelectionBar.add(custLbl);
        topSelectionBar.add(customerCombo);
        topSelectionBar.add(refreshOptionsBtn);
        panel.add(topSelectionBar, BorderLayout.NORTH);

        // Center Split: Menu Catalog on Left, Order Cart & Payment on Right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.52);
        split.setBorder(null);

        // ------------------ LEFT: Dish Catalog & Search ------------------
        JPanel leftCatalogPanel = UIUtils.createCardPanel();
        leftCatalogPanel.setLayout(new BorderLayout(8, 8));

        JPanel searchHeader = new JPanel(new BorderLayout(5, 5));
        searchHeader.setOpaque(false);
        JLabel searchTitle = new JLabel("Live Menu Search (Case-Insensitive):");
        searchTitle.setFont(UIUtils.FONT_BOLD);
        dishSearchField = UIUtils.createTextField(15);
        dishSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMenuCatalog(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMenuCatalog(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMenuCatalog(); }
        });
        searchHeader.add(searchTitle, BorderLayout.NORTH);
        searchHeader.add(dishSearchField, BorderLayout.CENTER);
        leftCatalogPanel.add(searchHeader, BorderLayout.NORTH);

        String[] catCols = {"Dish Name", "Category", "Price"};
        menuCatalogTableModel = new DefaultTableModel(catCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuCatalogTable = new JTable(menuCatalogTableModel);
        UIUtils.styleTable(menuCatalogTable);
        leftCatalogPanel.add(new JScrollPane(menuCatalogTable), BorderLayout.CENTER);

        JPanel addCartBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        addCartBar.setOpaque(false);
        addCartBar.add(new JLabel("Qty:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        quantitySpinner.setPreferredSize(new Dimension(55, 28));
        addCartBar.add(quantitySpinner);

        JButton addToCartBtn = UIUtils.createButton("Add to Cart", UIUtils.COLOR_ACCENT, Color.WHITE);
        addToCartBtn.addActionListener(e -> handleAddToCart());
        addCartBar.add(addToCartBtn);
        leftCatalogPanel.add(addCartBar, BorderLayout.SOUTH);

        split.setLeftComponent(leftCatalogPanel);

        // ------------------ RIGHT: Cart & Checkout ------------------
        JPanel rightCartPanel = UIUtils.createCardPanel();
        rightCartPanel.setLayout(new BorderLayout(8, 8));

        JLabel cartTitle = new JLabel("Active Order Cart");
        cartTitle.setFont(UIUtils.FONT_SUBTITLE);
        rightCartPanel.add(cartTitle, BorderLayout.NORTH);

        String[] cartCols = {"Dish", "Qty", "Price", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        UIUtils.styleTable(cartTable);
        rightCartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Bottom Billing & Payment Box
        JPanel billingBox = new JPanel(new BorderLayout(8, 8));
        billingBox.setOpaque(false);

        // Cart Actions (Remove / Clear)
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        cartActions.setOpaque(false);
        JButton removeCartItemBtn = new JButton("Remove Item");
        removeCartItemBtn.addActionListener(e -> handleRemoveCartItem());
        JButton clearCartBtn = new JButton("Clear Cart");
        clearCartBtn.addActionListener(e -> clearCart());
        cartActions.add(removeCartItemBtn);
        cartActions.add(clearCartBtn);
        billingBox.add(cartActions, BorderLayout.NORTH);

        // Calculations & Payment Form
        JPanel calcPanel = new JPanel(new GridBagLayout());
        calcPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0; calcPanel.add(new JLabel("Cart Subtotal:"), gbc);
        gbc.gridx = 1;
        subtotalLabel = new JLabel("Rs.0.00");
        subtotalLabel.setFont(UIUtils.FONT_BOLD);
        calcPanel.add(subtotalLabel, gbc);

        // Discount %
        gbc.gridx = 0; gbc.gridy = 1; calcPanel.add(new JLabel("Discount (%):"), gbc);
        gbc.gridx = 1;
        discountField = UIUtils.createTextField(6);
        discountField.setText("0.0");
        discountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateEstimatedTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { updateEstimatedTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { updateEstimatedTotal(); }
        });
        calcPanel.add(discountField, gbc);

        // Tax %
        gbc.gridx = 0; gbc.gridy = 2; calcPanel.add(new JLabel("Tax / GST (%):"), gbc);
        gbc.gridx = 1;
        taxField = UIUtils.createTextField(6);
        taxField.setText("5.0");
        taxField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateEstimatedTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { updateEstimatedTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { updateEstimatedTotal(); }
        });
        calcPanel.add(taxField, gbc);

        // Estimated Total
        gbc.gridx = 0; gbc.gridy = 3; calcPanel.add(new JLabel("Grand Total:"), gbc);
        gbc.gridx = 1;
        grandTotalEstimatedLabel = new JLabel("Rs.0.00");
        grandTotalEstimatedLabel.setFont(UIUtils.FONT_TITLE);
        grandTotalEstimatedLabel.setForeground(UIUtils.COLOR_SUCCESS);
        calcPanel.add(grandTotalEstimatedLabel, gbc);

        // Payment Method Dropdown
        gbc.gridx = 0; gbc.gridy = 4; calcPanel.add(new JLabel("Payment Type:"), gbc);
        gbc.gridx = 1;
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash Payment", "Card Payment", "UPI Payment"});
        paymentMethodCombo.addActionListener(e -> onPaymentMethodChanged());
        calcPanel.add(paymentMethodCombo, gbc);

        billingBox.add(calcPanel, BorderLayout.CENTER);

        // Dynamic Payment Card Details Panel
        paymentCardLayout = new CardLayout();
        paymentDetailsCard = new JPanel(paymentCardLayout);
        paymentDetailsCard.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_BORDER, 1),
            new EmptyBorder(6, 6, 6, 6)
        ));

        // Subcard 1: Cash
        JPanel cashCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cashCard.add(new JLabel("Tendered Cash (Rs.):"));
        cashTenderedField = UIUtils.createTextField(8);
        cashCard.add(cashTenderedField);
        paymentDetailsCard.add(cashCard, "Cash Payment");

        // Subcard 2: Card
        JPanel cardSub = new JPanel(new GridLayout(3, 2, 4, 4));
        cardSub.add(new JLabel("Card Number:"));
        cardNumberField = UIUtils.createTextField(14);
        cardNumberField.setText("4532-8901-2345-6789");
        cardSub.add(cardNumberField);
        cardSub.add(new JLabel("Cardholder Name:"));
        cardHolderField = UIUtils.createTextField(14);
        cardHolderField.setText("Guest Diner");
        cardSub.add(cardHolderField);
        cardSub.add(new JLabel("Network:"));
        cardTypeCombo = new JComboBox<>(new String[]{"Visa", "MasterCard", "RuPay", "Amex"});
        cardSub.add(cardTypeCombo);
        paymentDetailsCard.add(cardSub, "Card Payment");

        // Subcard 3: UPI
        JPanel upiCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        upiCard.add(new JLabel("UPI VPA ID:"));
        upiIdField = UIUtils.createTextField(14);
        upiIdField.setText("guest@okhdfcbank");
        upiCard.add(upiIdField);
        paymentDetailsCard.add(upiCard, "UPI Payment");

        // Checkout Button
        JPanel checkoutActionPanel = new JPanel(new BorderLayout(5, 5));
        checkoutActionPanel.setOpaque(false);
        checkoutActionPanel.add(paymentDetailsCard, BorderLayout.NORTH);

        JButton checkoutBtn = UIUtils.createButton("Complete Checkout & Print Receipt", UIUtils.COLOR_SUCCESS, Color.WHITE);
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> handleCheckout());
        checkoutActionPanel.add(checkoutBtn, BorderLayout.SOUTH);

        billingBox.add(checkoutActionPanel, BorderLayout.SOUTH);
        rightCartPanel.add(billingBox, BorderLayout.SOUTH);

        split.setRightComponent(rightCartPanel);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void onPaymentMethodChanged() {
        String selected = (String) paymentMethodCombo.getSelectedItem();
        if (selected != null) {
            paymentCardLayout.show(paymentDetailsCard, selected);
        }
    }

    // =========================================================================
    // Tab 2: Register Customer
    // =========================================================================
    private JPanel buildCustomerRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JLabel title = new JLabel("Register New Guest:");
        title.setFont(UIUtils.FONT_BOLD);
        formCard.add(title);

        formCard.add(new JLabel("Customer Name:"));
        custNameField = UIUtils.createTextField(14);
        formCard.add(custNameField);

        formCard.add(new JLabel("Contact Phone:"));
        custContactField = UIUtils.createTextField(12);
        formCard.add(custContactField);

        JButton registerBtn = UIUtils.createButton("Register Guest", UIUtils.COLOR_ACCENT, Color.WHITE);
        registerBtn.addActionListener(e -> handleRegisterCustomer());
        formCard.add(registerBtn);

        panel.add(formCard, BorderLayout.NORTH);

        String[] cols = {"Customer ID", "Customer Name", "Contact Phone"};
        customerTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(customerTableModel);
        UIUtils.styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void handleRegisterCustomer() {
        String name = custNameField.getText().trim();
        String contact = custContactField.getText().trim();

        if (name.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer name and contact cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            service.registerCustomer(name, contact);
            custNameField.setText("");
            custContactField.setText("");
            JOptionPane.showMessageDialog(this, "Customer " + name + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshCustomersList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Tab 3: My Salary (Read-Only)
    // =========================================================================
    private JPanel buildSalaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        panel.setBackground(UIUtils.COLOR_BG);

        JPanel card = UIUtils.createCardPanel();
        card.setPreferredSize(new Dimension(500, 320));
        card.setLayout(new BorderLayout(15, 15));

        JLabel title = new JLabel("Personal Compensation Record");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(5, 2, 10, 12));
        details.setOpaque(false);

        Employee user = service.getCurrentUser();
        details.add(new JLabel("Staff Name:"));
        JLabel nameVal = new JLabel(user.getName());
        nameVal.setFont(UIUtils.FONT_BOLD);
        details.add(nameVal);

        details.add(new JLabel("Designation / Role:"));
        JLabel roleVal = new JLabel("Floor Service (" + user.getRole() + ")");
        roleVal.setFont(UIUtils.FONT_BOLD);
        details.add(roleVal);

        details.add(new JLabel("Staff Username:"));
        details.add(new JLabel(user.getUsername()));

        details.add(new JLabel("Contact Number:"));
        details.add(new JLabel(user.getContact()));

        details.add(new JLabel("Monthly Salary:"));
        waiterSalaryLabel = new JLabel("Rs.0.00");
        waiterSalaryLabel.setFont(UIUtils.FONT_TITLE);
        waiterSalaryLabel.setForeground(UIUtils.COLOR_SUCCESS);
        details.add(waiterSalaryLabel);

        card.add(details, BorderLayout.CENTER);

        JButton refreshSalBtn = UIUtils.createButton("Check Fresh Salary from DB", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refreshSalBtn.addActionListener(e -> {
            refreshSalaryView();
            JOptionPane.showMessageDialog(this, "Salary verified fresh from database: " + waiterSalaryLabel.getText(),
                "Verified", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btm.setOpaque(false);
        btm.add(refreshSalBtn);
        card.add(btm, BorderLayout.SOUTH);

        panel.add(card);
        return panel;
    }

    // =========================================================================
    // Cart & Checkout Logic (Polymorphism Core)
    // =========================================================================
    private void handleAddToCart() {
        int selectedRow = menuCatalogTable.getSelectedRow();
        if (selectedRow < 0 || currentCatalogItems == null || selectedRow >= currentCatalogItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select a dish from the menu catalog.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MenuItem item = currentCatalogItems.get(selectedRow);
        int qty = (Integer) quantitySpinner.getValue();

        // Check if item already exists in cart, then increment quantity
        boolean found = false;
        for (OrderItem oi : cartItems) {
            if (oi.getMenuItemId() == item.getId()) {
                oi.setQuantity(oi.getQuantity() + qty);
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(new OrderItem(item.getId(), item.getName(), qty, item.getPrice()));
        }

        refreshCartTable();
    }

    private void handleRemoveCartItem() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            refreshCartTable();
        }
    }

    private void clearCart() {
        cartItems.clear();
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        double subtotal = 0.0;
        for (OrderItem item : cartItems) {
            subtotal += item.getSubtotal();
            cartTableModel.addRow(new Object[]{
                item.getMenuItemName(),
                item.getQuantity(),
                UIUtils.formatCurrency(item.getPrice()),
                UIUtils.formatCurrency(item.getSubtotal())
            });
        }
        if (cartItems.isEmpty()) {
            cartTableModel.addRow(new Object[]{"Cart is empty - Add dishes from menu", "-", "-", "-"});
        }
        subtotalLabel.setText(UIUtils.formatCurrency(subtotal));
        updateEstimatedTotal();
    }

    private void updateEstimatedTotal() {
        double subtotal = calculateCartSubtotal();
        double discountPercent = 0.0;
        double taxPercent = 5.0;

        try {
            discountPercent = Double.parseDouble(discountField.getText().trim());
        } catch (Exception ignored) {}

        try {
            taxPercent = Double.parseDouble(taxField.getText().trim());
        } catch (Exception ignored) {}

        double discountAmount = subtotal * (discountPercent / 100.0);
        double discountedSubtotal = subtotal - discountAmount;
        double taxAmount = discountedSubtotal * (taxPercent / 100.0);
        double grandTotal = discountedSubtotal + taxAmount;

        grandTotalEstimatedLabel.setText(UIUtils.formatCurrency(grandTotal));

        // Suggest tendered amount for cash
        if (cashTenderedField != null && (cashTenderedField.getText().isEmpty() || cashTenderedField.getText().equals("0.00"))) {
            cashTenderedField.setText(String.format("%.2f", grandTotal));
        }
    }

    private double calculateCartSubtotal() {
        double sub = 0.0;
        for (OrderItem it : cartItems) {
            sub += it.getSubtotal();
        }
        return sub;
    }

    /**
     * Executes Order Placement and Dynamic Polymorphic Payment Checkout.
     */
    private void handleCheckout() {
        RestaurantTable table = (RestaurantTable) tableCombo.getSelectedItem();
        Customer customer = (Customer) customerCombo.getSelectedItem();

        if (table == null) {
            JOptionPane.showMessageDialog(this, "Please select a dining table.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Please select a guest customer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The order cart is empty! Add dishes to proceed.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double discountPercent;
        double taxPercent;
        try {
            discountPercent = Double.parseDouble(discountField.getText().trim());
            taxPercent = Double.parseDouble(taxField.getText().trim());
            if (discountPercent < 0 || discountPercent > 100) {
                JOptionPane.showMessageDialog(this, "Discount % must be between 0 and 100.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (taxPercent < 0) {
                JOptionPane.showMessageDialog(this, "Tax % cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric discount and tax percentages.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Atomically place the order into the database
        int orderId;
        try {
            orderId = service.placeOrder(table.getId(), customer.getId(), cartItems);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to place order: " + ex.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Prepare Polymorphic Payment Object based on user selection
        Payment payment;
        String method = (String) paymentMethodCombo.getSelectedItem();

        try {
            double grandTotalEst = calculateEstimatedGrandTotal(calculateCartSubtotal(), discountPercent, taxPercent);

            if ("Cash Payment".equals(method)) {
                String tenderedStr = cashTenderedField.getText().trim();
                double tendered = Double.parseDouble(tenderedStr.isEmpty() ? String.format("%.2f", grandTotalEst) : tenderedStr);
                payment = new CashPayment(orderId, grandTotalEst, tendered);
            } else if ("Card Payment".equals(method)) {
                String cardNum = cardNumberField.getText().trim();
                String holder = cardHolderField.getText().trim();
                String type = (String) cardTypeCombo.getSelectedItem();
                payment = new CardPayment(orderId, grandTotalEst, cardNum, holder, type);
            } else if ("UPI Payment".equals(method)) {
                String upi = upiIdField.getText().trim();
                payment = new UPIPayment(orderId, grandTotalEst, upi);
            } else {
                JOptionPane.showMessageDialog(this, "Unknown payment method selected.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // =================================================================
            // DYNAMIC POLYMORPHISM DEMONSTRATION:
            // payment is passed as the abstract base type Payment.
            // service.checkoutOrder will dynamically resolve payment.processPayment()!
            // =================================================================
            Receipt receipt = service.checkoutOrder(orderId, calculateCartSubtotal(), discountPercent, taxPercent, payment);

            // Display Receipt Dialog
            new ReceiptDialog(this, receipt).setVisible(true);

            // Clear Cart and refresh state
            clearCart();
            refreshTablesList();
            JOptionPane.showMessageDialog(this, "Order #" + orderId + " successfully checked out and settled!", "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payment Processing Failed: " + ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateEstimatedGrandTotal(double subtotal, double discountPercent, double taxPercent) {
        double discountAmount = subtotal * (discountPercent / 100.0);
        double discountedSubtotal = subtotal - discountAmount;
        double taxAmount = discountedSubtotal * (taxPercent / 100.0);
        return discountedSubtotal + taxAmount;
    }

    // =========================================================================
    // Refresh Routines
    // =========================================================================
    public void refreshAllData() {
        refreshTablesList();
        refreshCustomersList();
        filterMenuCatalog();
        refreshSalaryView();
    }

    /**
     * Loops over and populates ALL tables returned by TableDAO.
     */
    private void refreshTablesList() {
        tableCombo.removeAllItems();
        List<RestaurantTable> tables = service.getAllTables();
        for (RestaurantTable t : tables) {
            if (t.getTableNumber() == null) continue;
            tableCombo.addItem(t);
        }
    }

    private void refreshCustomersList() {
        customerCombo.removeAllItems();
        customerTableModel.setRowCount(0);
        List<Customer> customers = service.getAllCustomers();
        for (Customer c : customers) {
            if (c.getName() == null) continue;
            customerCombo.addItem(c);
            customerTableModel.addRow(new Object[]{c.getId(), c.getName(), c.getContact()});
        }
    }

    private void filterMenuCatalog() {
        String keyword = dishSearchField.getText().trim();
        currentCatalogItems = service.searchMenuItems(keyword);
        menuCatalogTableModel.setRowCount(0);
        for (MenuItem item : currentCatalogItems) {
            if (item.getName() == null || item.getPrice() <= 0) continue;
            menuCatalogTableModel.addRow(new Object[]{
                item.getName(),
                item.getCategoryName(),
                UIUtils.formatCurrency(item.getPrice())
            });
        }
        if (menuCatalogTableModel.getRowCount() == 0) {
            menuCatalogTableModel.addRow(new Object[]{"No dishes found matching search", "-", "-"});
        }
    }

    private void refreshSalaryView() {
        try {
            double freshSalary = service.getFreshSalaryForCurrentEmployee();
            waiterSalaryLabel.setText(UIUtils.formatCurrency(freshSalary));
        } catch (Exception ex) {
            System.err.println("[WaiterDashboard] Error fetching fresh salary: " + ex.getMessage());
        }
    }
}
