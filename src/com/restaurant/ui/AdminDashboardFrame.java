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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminDashboardFrame
 * 
 * Comprehensive Administrative Control Center for the Restaurant Management System.
 * Encapsulates management modules for:
 * 1. Profit & Loss Reporting & Real-time Metrics
 * 2. Menu Category Management
 * 3. Menu Items with Live Filter Search & Dynamic Category Dropdown
 * 4. Inventory Tracking with Restocking and Low-Stock Highlighting
 * 5. Staff Salary Administration
 * 6. Historical Orders Tracking
 * 
 * Also houses the prominent proactive Low-Stock Alert Banner at the top.
 */
public class AdminDashboardFrame extends JFrame {

    private final RestaurantService service;

    // Top Alert Banner
    private JPanel lowStockBanner;
    private JLabel lowStockBannerLabel;

    // Tabs
    private JTabbedPane tabbedPane;

    // P&L Metric Labels
    private JLabel revenueMetricLabel;
    private JLabel salaryMetricLabel;
    private JLabel inventoryMetricLabel;
    private JLabel netPnLMetricLabel;
    private JLabel orderCountMetricLabel;

    // Category Components
    private DefaultTableModel categoryTableModel;
    private JTextField categoryNameField;

    // Menu Components
    private DefaultTableModel menuTableModel;
    private JComboBox<Category> dishCategoryCombo;
    private JTextField dishNameField;
    private JTextField dishPriceField;
    private JTextField dishDescField;
    private JTextField menuSearchField;

    // Inventory Components
    private DefaultTableModel inventoryTableModel;
    private JTextField invNameField;
    private JTextField invUnitField;
    private JTextField invQtyField;
    private JTextField invThresholdField;
    private JTextField invCostField;
    private JComboBox<InventoryItem> invRestockCombo;
    private JTextField invRestockQtyField;

    // Salary Components
    private DefaultTableModel employeeTableModel;
    private JComboBox<Employee> salaryEmployeeCombo;
    private JTextField newSalaryField;
    private JTextField newEmpNameField;
    private JComboBox<String> newEmpRoleCombo;
    private JTextField newEmpUserField;
    private JPasswordField newEmpPassField;
    private JTextField newEmpSalaryField;
    private JTextField newEmpContactField;

    // Orders Components
    private DefaultTableModel ordersTableModel;
    private Timer autoRefreshTimer;

    // Consolidated Reports Components
    private JPanel repNetBanner;
    private JLabel repNetTitle;
    private JLabel repNetValue;
    private JLabel repNetFormula;
    private JLabel repTotalRev;
    private JLabel repCashRev;
    private JLabel repCardRev;
    private JLabel repUpiRev;
    private JLabel repSalaryExp;
    private JLabel repInventoryVal;
    private JLabel repTotalExp;
    private JLabel repTotalOrders;
    private JLabel repTodayOrders;
    private JLabel repPlacedOrders;
    private JLabel repPrepOrders;
    private JLabel repServedOrders;
    private JLabel repDoneOrders;
    private JLabel repInvValSummary;
    private JLabel repLowStockSummary;
    private DefaultTableModel repEmployeeTableModel;
    private JLabel repPayrollFooter;

    public AdminDashboardFrame(RestaurantService service) {
        super("The Royal Spice - Administrator Management Suite");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        initUI();
        refreshAllData();

        // Background auto-refresh timer (polls every 4 seconds)
        autoRefreshTimer = new Timer(4000, e -> {
            refreshDashboardMetrics();
            refreshOrders();
            refreshConsolidatedReport();
        });
        autoRefreshTimer.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
                    autoRefreshTimer.stop();
                }
            }
        });
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIUtils.COLOR_BG);

        // Top Navigation Header with Royal Spice Logo
        JPanel headerPanel = UIUtils.createHeaderPanel(
            "Administrator Management Suite",
            "Administrator",
            service.getCurrentUser().getName(),
            () -> {
                if (autoRefreshTimer != null) autoRefreshTimer.stop();
                service.logout();
                new LoginFrame(service).setVisible(true);
                dispose();
            }
        );

        // Prominent Low Stock Alert Banner
        lowStockBanner = new JPanel(new BorderLayout());
        lowStockBanner.setBackground(new Color(254, 226, 226)); // Soft light red
        lowStockBanner.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_DANGER, 1),
            new EmptyBorder(10, 20, 10, 20)
        ));
        lowStockBannerLabel = new JLabel("CRITICAL ALERT: Raw material inventory levels are low!");
        lowStockBannerLabel.setFont(UIUtils.FONT_BOLD);
        lowStockBannerLabel.setForeground(UIUtils.COLOR_DANGER);
        lowStockBanner.add(lowStockBannerLabel, BorderLayout.CENTER);
        lowStockBanner.setVisible(false); // Shown conditionally in refreshAllData()

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(lowStockBanner, BorderLayout.SOUTH);
        rootPanel.add(topContainer, BorderLayout.NORTH);

        // Main Tabbed Navigation
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.addTab("Dashboard / P&L", buildDashboardTab());
        tabbedPane.addTab("Categories", buildCategoryTab());
        tabbedPane.addTab("Menu Items", buildMenuTab());
        tabbedPane.addTab("Inventory", buildInventoryTab());
        tabbedPane.addTab("Salary Management", buildSalaryTab());
        tabbedPane.addTab("All Orders", buildOrdersTab());
        tabbedPane.addTab("Reports", buildReportsTab());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    public void setSelectedTab(int index) {
        if (tabbedPane != null && index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    // =========================================================================
    // Tab 1: Dashboard & Profit/Loss Overview
    // =========================================================================
    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Metric Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 5, 12, 12));
        cardsGrid.setOpaque(false);

        revenueMetricLabel = new JLabel("Rs.0.00");
        revenueMetricLabel.setFont(UIUtils.FONT_TITLE);
        revenueMetricLabel.setForeground(UIUtils.COLOR_SUCCESS);

        salaryMetricLabel = new JLabel("Rs.0.00");
        salaryMetricLabel.setFont(UIUtils.FONT_TITLE);
        salaryMetricLabel.setForeground(UIUtils.COLOR_WARNING);

        inventoryMetricLabel = new JLabel("Rs.0.00");
        inventoryMetricLabel.setFont(UIUtils.FONT_TITLE);
        inventoryMetricLabel.setForeground(UIUtils.COLOR_ACCENT);

        netPnLMetricLabel = new JLabel("Rs.0.00");
        netPnLMetricLabel.setFont(UIUtils.FONT_TITLE);

        orderCountMetricLabel = new JLabel("0");
        orderCountMetricLabel.setFont(UIUtils.FONT_TITLE);
        orderCountMetricLabel.setForeground(UIUtils.COLOR_PRIMARY);

        cardsGrid.add(createStatCard("Total Revenue (Sales)", revenueMetricLabel, UIUtils.COLOR_SUCCESS));
        cardsGrid.add(createStatCard("Salary Expense", salaryMetricLabel, UIUtils.COLOR_WARNING));
        cardsGrid.add(createStatCard("Inventory Holding Cost", inventoryMetricLabel, UIUtils.COLOR_ACCENT));
        cardsGrid.add(createStatCard("Net P&L Status", netPnLMetricLabel, UIUtils.COLOR_TEXT_MAIN));
        cardsGrid.add(createStatCard("Total Orders Count", orderCountMetricLabel, UIUtils.COLOR_PRIMARY));

        panel.add(cardsGrid, BorderLayout.NORTH);

        // Report Summary Body
        JPanel reportCard = UIUtils.createCardPanel();
        reportCard.setLayout(new BorderLayout(10, 10));

        JLabel repTitle = new JLabel("Executive Financial Summary & Methodology");
        repTitle.setFont(UIUtils.FONT_SUBTITLE);
        repTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        reportCard.add(repTitle, BorderLayout.NORTH);

        JTextArea explanation = new JTextArea();
        explanation.setEditable(false);
        explanation.setFont(UIUtils.FONT_REGULAR);
        explanation.setBackground(Color.WHITE);
        explanation.setText(
            "FINANCIAL AUDIT STATEMENT RULES:\n\n" +
            "1. Total Revenue: Direct summation of all finalized customer checkout bills in the database.\n" +
            "2. Total Salary Expense: Sum of current salaries across all registered staff members (Admin, Kitchen, Waiter).\n" +
            "3. Total Inventory Cost: Current inventory asset valuation = SUM(Quantity * Cost Per Unit).\n" +
            "4. Net Profit / Loss: Total Revenue - Total Salary Expense - Total Inventory Cost.\n" +
            "   - If positive, clearly identified as Net Profit in green.\n" +
            "   - If negative, clearly identified as Net Loss in red.\n\n" +
            "5. Data Consistency Guarantee: The Total Orders Count and revenue metrics displayed above\n" +
            "   are derived from the identical underlying database tables (orders, bills) that drive the All Orders tab."
        );
        reportCard.add(explanation, BorderLayout.CENTER);

        JButton refreshBtn = UIUtils.createButton("Refresh Financial Metrics", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAllData());
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(refreshBtn);
        reportCard.add(btm, BorderLayout.SOUTH);

        panel.add(reportCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color barColor) {
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout(5, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIUtils.FONT_SMALL);
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel indicator = new JPanel();
        indicator.setBackground(barColor);
        indicator.setPreferredSize(new Dimension(4, 25));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(indicator, BorderLayout.WEST);
        return card;
    }

    // =========================================================================
    // Tab 2: Category Management
    // =========================================================================
    private JPanel buildCategoryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Add Category Card
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JLabel formLabel = new JLabel("New Category Name:");
        formLabel.setFont(UIUtils.FONT_BOLD);
        categoryNameField = UIUtils.createTextField(18);

        JButton addCatBtn = UIUtils.createButton("Add Category", UIUtils.COLOR_ACCENT, Color.WHITE);
        addCatBtn.addActionListener(e -> {
            String name = categoryNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                service.addCategory(name);
                categoryNameField.setText("");
                JOptionPane.showMessageDialog(this, "Category '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCategories();
                refreshMenuCategoriesDropdown();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Adding Category", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(formLabel);
        formCard.add(categoryNameField);
        formCard.add(addCatBtn);

        panel.add(formCard, BorderLayout.NORTH);

        // Categories Table
        String[] columns = {"Category ID", "Category Name"};
        categoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(categoryTableModel);
        UIUtils.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // Tab 3: Menu Items with Live Filter Search & Dropdown
    // =========================================================================
    private JPanel buildMenuTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form: Add Dish
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formHeader = new JLabel("Add New Menu Item (All fields required)");
        formHeader.setFont(UIUtils.FONT_SUBTITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        formCard.add(formHeader, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        formCard.add(new JLabel("Dish Name:"), gbc);
        gbc.gridx = 1;
        dishNameField = UIUtils.createTextField(14);
        formCard.add(dishNameField, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        dishCategoryCombo = new JComboBox<>();
        dishCategoryCombo.setFont(UIUtils.FONT_REGULAR);
        formCard.add(dishCategoryCombo, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        formCard.add(new JLabel("Price (Rs.):"), gbc);
        gbc.gridx = 1;
        dishPriceField = UIUtils.createTextField(14);
        formCard.add(dishPriceField, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Description:"), gbc);
        gbc.gridx = 3;
        dishDescField = UIUtils.createTextField(14);
        formCard.add(dishDescField, gbc);

        gbc.gridy = 3; gbc.gridx = 3;
        JButton addDishBtn = UIUtils.createButton("Add Dish to Menu", UIUtils.COLOR_ACCENT, Color.WHITE);
        addDishBtn.addActionListener(e -> handleAddDish());
        formCard.add(addDishBtn, gbc);

        panel.add(formCard, BorderLayout.NORTH);

        // Center Panel: Live Search + Table
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);

        // Live Search Bar
        JPanel searchBar = UIUtils.createCardPanel();
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLbl = new JLabel("Live Search Dish (case-insensitive):");
        searchLbl.setFont(UIUtils.FONT_BOLD);
        menuSearchField = UIUtils.createTextField(22);
        menuSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMenu(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMenu(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMenu(); }
        });
        searchBar.add(searchLbl);
        searchBar.add(menuSearchField);
        centerPanel.add(searchBar, BorderLayout.NORTH);

        // Menu Table
        String[] cols = {"ID", "Dish Name", "Category", "Price", "Description"};
        menuTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable menuTable = new JTable(menuTableModel);
        UIUtils.styleTable(menuTable);
        centerPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void handleAddDish() {
        String name = dishNameField.getText().trim();
        Category cat = (Category) dishCategoryCombo.getSelectedItem();
        String priceText = dishPriceField.getText().trim();
        String desc = dishDescField.getText().trim();

        // Strict Form Validation
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dish name cannot be blank!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cat == null || cat.getId() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid category!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0.0) {
                JOptionPane.showMessageDialog(this, "Price must be strictly greater than 0!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid price format! Please enter a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            service.addMenuItem(name, cat.getId(), price, desc);
            dishNameField.setText("");
            dishPriceField.setText("");
            dishDescField.setText("");
            JOptionPane.showMessageDialog(this, "Dish '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshMenuItems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Adding Dish", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterMenu() {
        String keyword = menuSearchField.getText().trim();
        List<MenuItem> items = service.searchMenuItems(keyword);
        menuTableModel.setRowCount(0);
        for (MenuItem item : items) {
            // Defensive: skip any corrupted or blank row
            if (item.getName() == null || item.getName().trim().isEmpty() || item.getPrice() <= 0) {
                continue;
            }
            menuTableModel.addRow(new Object[]{
                item.getId(),
                item.getName(),
                item.getCategoryName(),
                UIUtils.formatCurrency(item.getPrice()),
                item.getDescription()
            });
        }
    }

    // =========================================================================
    // Tab 4: Inventory Management & Restocking
    // =========================================================================
    private JPanel buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form Cards (Add item & Restock)
        JPanel formsContainer = new JPanel(new GridLayout(1, 2, 12, 12));
        formsContainer.setOpaque(false);

        // Left Form: Add New Inventory Item
        JPanel addCard = UIUtils.createCardPanel();
        addCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel addTitle = new JLabel("Register New Pantry Item");
        addTitle.setFont(UIUtils.FONT_SUBTITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        addCard.add(addTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; addCard.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; invNameField = UIUtils.createTextField(10); addCard.add(invNameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; addCard.add(new JLabel("Unit (kg/l/pkts):"), gbc);
        gbc.gridx = 1; invUnitField = UIUtils.createTextField(10); addCard.add(invUnitField, gbc);

        gbc.gridy = 3; gbc.gridx = 0; addCard.add(new JLabel("Initial Quantity:"), gbc);
        gbc.gridx = 1; invQtyField = UIUtils.createTextField(10); addCard.add(invQtyField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; addCard.add(new JLabel("Low Threshold:"), gbc);
        gbc.gridx = 1; invThresholdField = UIUtils.createTextField(10); addCard.add(invThresholdField, gbc);

        gbc.gridy = 5; gbc.gridx = 0; addCard.add(new JLabel("Cost Per Unit (Rs.):"), gbc);
        gbc.gridx = 1; invCostField = UIUtils.createTextField(10); addCard.add(invCostField, gbc);

        gbc.gridy = 6; gbc.gridx = 1;
        JButton addInvBtn = UIUtils.createButton("Add Item", UIUtils.COLOR_ACCENT, Color.WHITE);
        addInvBtn.addActionListener(e -> handleAddInventoryItem());
        addCard.add(addInvBtn, gbc);

        formsContainer.add(addCard);

        // Right Form: Restock Existing Item
        JPanel restockCard = UIUtils.createCardPanel();
        restockCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(6, 4, 6, 4);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JLabel restockTitle = new JLabel("Restock Inventory Stock");
        restockTitle.setFont(UIUtils.FONT_SUBTITLE);
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.gridwidth = 2;
        restockCard.add(restockTitle, gbc2);

        gbc2.gridwidth = 1;
        gbc2.gridy = 1; gbc2.gridx = 0; restockCard.add(new JLabel("Select Item:"), gbc2);
        gbc2.gridx = 1; invRestockCombo = new JComboBox<>(); restockCard.add(invRestockCombo, gbc2);

        gbc2.gridy = 2; gbc2.gridx = 0; restockCard.add(new JLabel("Quantity to Add:"), gbc2);
        gbc2.gridx = 1; invRestockQtyField = UIUtils.createTextField(10); restockCard.add(invRestockQtyField, gbc2);

        gbc2.gridy = 3; gbc2.gridx = 1;
        JButton restockBtn = UIUtils.createButton("Confirm Restock", UIUtils.COLOR_SUCCESS, Color.WHITE);
        restockBtn.addActionListener(e -> handleRestock());
        restockCard.add(restockBtn, gbc2);

        formsContainer.add(restockCard);
        panel.add(formsContainer, BorderLayout.NORTH);

        // Inventory Table with Low Stock Highlighting
        String[] cols = {"ID", "Item Name", "Unit", "Quantity", "Low Threshold", "Cost/Unit", "Total Valuation", "Stock Status"};
        inventoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(inventoryTableModel);
        UIUtils.styleTable(table);

        // Modern Status Badge Renderer (LOW STOCK vs NORMAL)
        table.getColumnModel().getColumn(7).setCellRenderer(new UIUtils.BadgeCellRenderer());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void handleAddInventoryItem() {
        String name = invNameField.getText().trim();
        String unit = invUnitField.getText().trim();
        String qtyStr = invQtyField.getText().trim();
        String thStr = invThresholdField.getText().trim();
        String costStr = invCostField.getText().trim();

        if (name.isEmpty() || unit.isEmpty() || qtyStr.isEmpty() || thStr.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All inventory fields are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double qty = Double.parseDouble(qtyStr);
            double th = Double.parseDouble(thStr);
            double cost = Double.parseDouble(costStr);

            if (qty < 0 || th < 0 || cost < 0) {
                JOptionPane.showMessageDialog(this, "Quantities, threshold, and cost must be non-negative!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            service.addInventoryItem(name, unit, qty, th, cost);
            invNameField.setText("");
            invUnitField.setText("");
            invQtyField.setText("");
            invThresholdField.setText("");
            invCostField.setText("");

            JOptionPane.showMessageDialog(this, "Item '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshInventory();
            refreshDashboardMetrics();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for quantity, threshold, and cost.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Adding Item", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRestock() {
        InventoryItem item = (InventoryItem) invRestockCombo.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to restock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = invRestockQtyField.getText().trim();
        try {
            double addQty = Double.parseDouble(qtyStr);
            if (addQty <= 0) {
                JOptionPane.showMessageDialog(this, "Restock quantity must be greater than 0!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            service.restockItem(item.getId(), addQty);
            invRestockQtyField.setText("");
            JOptionPane.showMessageDialog(this, "Restocked " + addQty + " " + item.getUnit() + " of " + item.getItemName() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshInventory();
            refreshDashboardMetrics();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric restock quantity.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Restocking", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Tab 5: Salary Management
    // =========================================================================
    private JPanel buildSalaryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Forms Row (Update Salary & Add Employee)
        JPanel formsGrid = new JPanel(new GridLayout(1, 2, 12, 12));
        formsGrid.setOpaque(false);

        // Update Salary Card
        JPanel updateCard = UIUtils.createCardPanel();
        updateCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel uTitle = new JLabel("Adjust Employee Salary");
        uTitle.setFont(UIUtils.FONT_SUBTITLE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        updateCard.add(uTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; updateCard.add(new JLabel("Select Employee:"), gbc);
        gbc.gridx = 1; salaryEmployeeCombo = new JComboBox<>(); updateCard.add(salaryEmployeeCombo, gbc);

        gbc.gridy = 2; gbc.gridx = 0; updateCard.add(new JLabel("New Salary (Rs.):"), gbc);
        gbc.gridx = 1; newSalaryField = UIUtils.createTextField(10); updateCard.add(newSalaryField, gbc);

        gbc.gridy = 3; gbc.gridx = 1;
        JButton updateBtn = UIUtils.createButton("Update Salary", UIUtils.COLOR_PRIMARY, Color.WHITE);
        updateBtn.addActionListener(e -> handleUpdateSalary());
        updateCard.add(updateBtn, gbc);

        formsGrid.add(updateCard);

        // Add Employee Card
        JPanel addEmpCard = UIUtils.createCardPanel();
        addEmpCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(4, 4, 4, 4);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JLabel addEmpTitle = new JLabel("Register New Staff Member");
        addEmpTitle.setFont(UIUtils.FONT_SUBTITLE);
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.gridwidth = 2;
        addEmpCard.add(addEmpTitle, gbc2);

        gbc2.gridwidth = 1;
        gbc2.gridy = 1; gbc2.gridx = 0; addEmpCard.add(new JLabel("Name:"), gbc2);
        gbc2.gridx = 1; newEmpNameField = UIUtils.createTextField(10); addEmpCard.add(newEmpNameField, gbc2);

        gbc2.gridy = 2; gbc2.gridx = 0; addEmpCard.add(new JLabel("Role:"), gbc2);
        gbc2.gridx = 1; newEmpRoleCombo = new JComboBox<>(new String[]{"WAITER", "KITCHEN", "ADMIN"}); addEmpCard.add(newEmpRoleCombo, gbc2);

        gbc2.gridy = 3; gbc2.gridx = 0; addEmpCard.add(new JLabel("Username:"), gbc2);
        gbc2.gridx = 1; newEmpUserField = UIUtils.createTextField(10); addEmpCard.add(newEmpUserField, gbc2);

        gbc2.gridy = 4; gbc2.gridx = 0; addEmpCard.add(new JLabel("Password:"), gbc2);
        gbc2.gridx = 1; newEmpPassField = new JPasswordField(10); addEmpCard.add(newEmpPassField, gbc2);

        gbc2.gridy = 5; gbc2.gridx = 0; addEmpCard.add(new JLabel("Salary (Rs.):"), gbc2);
        gbc2.gridx = 1; newEmpSalaryField = UIUtils.createTextField(10); addEmpCard.add(newEmpSalaryField, gbc2);

        gbc2.gridy = 6; gbc2.gridx = 0; addEmpCard.add(new JLabel("Contact:"), gbc2);
        gbc2.gridx = 1; newEmpContactField = UIUtils.createTextField(10); addEmpCard.add(newEmpContactField, gbc2);

        gbc2.gridy = 7; gbc2.gridx = 1;
        JButton addEmpBtn = UIUtils.createButton("Add Employee", UIUtils.COLOR_ACCENT, Color.WHITE);
        addEmpBtn.addActionListener(e -> handleAddEmployee());
        addEmpCard.add(addEmpBtn, gbc2);

        formsGrid.add(addEmpCard);
        panel.add(formsGrid, BorderLayout.NORTH);

        // Employees Table
        String[] cols = {"ID", "Staff Name", "Role", "Username", "Monthly Salary", "Contact"};
        employeeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(employeeTableModel);
        UIUtils.styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void handleUpdateSalary() {
        Employee emp = (Employee) salaryEmployeeCombo.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String salStr = newSalaryField.getText().trim();
        try {
            double newSal = Double.parseDouble(salStr);
            if (newSal < 0) {
                JOptionPane.showMessageDialog(this, "Salary cannot be negative!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            service.updateEmployeeSalary(emp.getId(), newSal);
            newSalaryField.setText("");
            JOptionPane.showMessageDialog(this, "Salary updated for " + emp.getName() + " to " + UIUtils.formatCurrency(newSal) + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshSalaries();
            refreshDashboardMetrics();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric salary.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Updating Salary", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddEmployee() {
        String name = newEmpNameField.getText().trim();
        String role = (String) newEmpRoleCombo.getSelectedItem();
        String user = newEmpUserField.getText().trim();
        String pass = new String(newEmpPassField.getPassword()).trim();
        String salStr = newEmpSalaryField.getText().trim();
        String contact = newEmpContactField.getText().trim();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || salStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Username, Password, and Salary are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double sal = Double.parseDouble(salStr);
            if (sal < 0) {
                JOptionPane.showMessageDialog(this, "Salary cannot be negative!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            service.addEmployee(name, role, user, pass, sal, contact);
            newEmpNameField.setText("");
            newEmpUserField.setText("");
            newEmpPassField.setText("");
            newEmpSalaryField.setText("");
            newEmpContactField.setText("");

            JOptionPane.showMessageDialog(this, "Employee " + name + " (" + role + ") registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshSalaries();
            refreshDashboardMetrics();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric salary.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Adding Employee", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Tab 6: All Orders
    // =========================================================================
    private JPanel buildOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = UIUtils.createCardPanel();
        topBar.setLayout(new BorderLayout());
        JLabel ordersTitle = new JLabel("Audit Log of All Orders (Direct from Database)");
        ordersTitle.setFont(UIUtils.FONT_SUBTITLE);
        topBar.add(ordersTitle, BorderLayout.WEST);

        JButton refreshOrdersBtn = UIUtils.createButton("Refresh Orders", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refreshOrdersBtn.addActionListener(e -> refreshOrders());
        topBar.add(refreshOrdersBtn, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Table", "Customer", "Status", "Total Amount", "Timestamp", "Items Breakdown"};
        ordersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(ordersTableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.BadgeCellRenderer());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // Tab 7: Consolidated Reports Dashboard
    // =========================================================================
    private JScrollPane buildReportsTab() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UIUtils.COLOR_BG);
        main.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Toolbar
        JPanel toolbar = UIUtils.createCardPanel();
        toolbar.setLayout(new BorderLayout());
        JPanel leftText = new JPanel(new GridLayout(2, 1, 0, 2));
        leftText.setOpaque(false);
        JLabel title = new JLabel("Executive Consolidated Operations & Financial Audit Report");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_PRIMARY);
        JLabel sub = new JLabel("Live aggregated intelligence combining revenue, operating expenses, payroll, and dining velocity");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.COLOR_TEXT_MUTED);
        leftText.add(title);
        leftText.add(sub);
        toolbar.add(leftText, BorderLayout.WEST);

        JButton refreshBtn = UIUtils.createButton("Refresh Report", UIUtils.COLOR_ACCENT, Color.WHITE);
        refreshBtn.addActionListener(e -> {
            refreshConsolidatedReport();
            JOptionPane.showMessageDialog(this, "Consolidated Report refreshed with live data!", "Report Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
        toolbar.add(refreshBtn, BorderLayout.EAST);
        main.add(toolbar);
        main.add(Box.createVerticalStrut(14));

        // 1. Prominent Net Profit / Loss Hero Banner
        repNetBanner = new JPanel(new BorderLayout(10, 8));
        repNetBanner.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_BORDER, 1),
            new EmptyBorder(16, 20, 16, 20)
        ));
        repNetTitle = new JLabel("EXECUTIVE BOTTOM LINE: NET PROFIT / LOSS");
        repNetTitle.setFont(UIUtils.FONT_BOLD);
        repNetValue = new JLabel("Rs.0.00");
        repNetValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        repNetFormula = new JLabel("Calculated as: Total Revenue - (Total Salary Expense + Total Inventory Asset Valuation)");
        repNetFormula.setFont(UIUtils.FONT_SMALL);

        repNetBanner.add(repNetTitle, BorderLayout.NORTH);
        repNetBanner.add(repNetValue, BorderLayout.CENTER);
        repNetBanner.add(repNetFormula, BorderLayout.SOUTH);
        main.add(repNetBanner);
        main.add(Box.createVerticalStrut(14));

        // 2. REVENUE SECTION
        JPanel revCard = UIUtils.createCardPanel();
        revCard.setLayout(new BorderLayout(8, 10));
        JLabel revHeader = new JLabel("1. REVENUE BREAKDOWN BY PAYMENT MODALITY");
        revHeader.setFont(UIUtils.FONT_SUBTITLE);
        revHeader.setForeground(UIUtils.COLOR_PRIMARY);
        revCard.add(revHeader, BorderLayout.NORTH);

        JPanel revGrid = new JPanel(new GridLayout(1, 4, 12, 10));
        revGrid.setOpaque(false);
        repTotalRev = new JLabel("Rs.0.00");
        repCashRev = new JLabel("Rs.0.00");
        repCardRev = new JLabel("Rs.0.00");
        repUpiRev = new JLabel("Rs.0.00");

        revGrid.add(createReportMetricCard("TOTAL REVENUE (ALL BILLS)", repTotalRev, UIUtils.COLOR_SUCCESS));
        revGrid.add(createReportMetricCard("CASH SETTLEMENTS", repCashRev, new Color(16, 185, 129)));
        revGrid.add(createReportMetricCard("CARD CHARGES (POS)", repCardRev, new Color(59, 130, 246)));
        revGrid.add(createReportMetricCard("UPI INSTANT TRANSFERS", repUpiRev, new Color(147, 51, 234)));
        revCard.add(revGrid, BorderLayout.CENTER);
        main.add(revCard);
        main.add(Box.createVerticalStrut(14));

        // 3. EXPENSES SECTION
        JPanel expCard = UIUtils.createCardPanel();
        expCard.setLayout(new BorderLayout(8, 10));
        JLabel expHeader = new JLabel("2. OPERATING EXPENSES & INVENTORY VALUATION");
        expHeader.setFont(UIUtils.FONT_SUBTITLE);
        expHeader.setForeground(UIUtils.COLOR_PRIMARY);
        expCard.add(expHeader, BorderLayout.NORTH);

        JPanel expGrid = new JPanel(new GridLayout(1, 3, 12, 10));
        expGrid.setOpaque(false);
        repSalaryExp = new JLabel("Rs.0.00");
        repInventoryVal = new JLabel("Rs.0.00");
        repTotalExp = new JLabel("Rs.0.00");

        expGrid.add(createReportMetricCard("TOTAL PAYROLL EXPENSE", repSalaryExp, UIUtils.COLOR_WARNING));
        expGrid.add(createReportMetricCard("INVENTORY HOLDING ASSET", repInventoryVal, UIUtils.COLOR_ACCENT));
        expGrid.add(createReportMetricCard("TOTAL COMBINED EXPENSES", repTotalExp, UIUtils.COLOR_DANGER));
        expCard.add(expGrid, BorderLayout.CENTER);
        main.add(expCard);
        main.add(Box.createVerticalStrut(14));

        // 4. ORDERS OVERVIEW & PIPELINE
        JPanel ordCard = UIUtils.createCardPanel();
        ordCard.setLayout(new BorderLayout(8, 10));
        JLabel ordHeader = new JLabel("3. DINING ORDERS PIPELINE & VELOCITY");
        ordHeader.setFont(UIUtils.FONT_SUBTITLE);
        ordHeader.setForeground(UIUtils.COLOR_PRIMARY);
        ordCard.add(ordHeader, BorderLayout.NORTH);

        JPanel ordGrid = new JPanel(new GridLayout(1, 6, 10, 10));
        ordGrid.setOpaque(false);
        repTotalOrders = new JLabel("0");
        repTodayOrders = new JLabel("0");
        repPlacedOrders = new JLabel("0");
        repPrepOrders = new JLabel("0");
        repServedOrders = new JLabel("0");
        repDoneOrders = new JLabel("0");

        ordGrid.add(createReportMetricCard("ALL ORDERS", repTotalOrders, UIUtils.COLOR_PRIMARY));
        ordGrid.add(createReportMetricCard("TODAY'S ORDERS", repTodayOrders, UIUtils.COLOR_ACCENT));
        ordGrid.add(createReportMetricCard("PLACED", repPlacedOrders, UIUtils.STATUS_PLACED_FG));
        ordGrid.add(createReportMetricCard("PREPARING", repPrepOrders, UIUtils.STATUS_PREP_FG));
        ordGrid.add(createReportMetricCard("SERVED", repServedOrders, UIUtils.STATUS_SERVED_FG));
        ordGrid.add(createReportMetricCard("COMPLETED / BILLED", repDoneOrders, UIUtils.STATUS_DONE_FG));
        ordCard.add(ordGrid, BorderLayout.CENTER);
        main.add(ordCard);
        main.add(Box.createVerticalStrut(14));

        // 5. INVENTORY & STOCK-OUT SUMMARY
        JPanel invSummaryCard = UIUtils.createCardPanel();
        invSummaryCard.setLayout(new BorderLayout(10, 8));
        JLabel invHeader = new JLabel("4. PANTRY INVENTORY & LOW-STOCK ALERT STATUS");
        invHeader.setFont(UIUtils.FONT_SUBTITLE);
        invHeader.setForeground(UIUtils.COLOR_PRIMARY);
        invSummaryCard.add(invHeader, BorderLayout.NORTH);

        JPanel invContent = new JPanel(new BorderLayout(12, 6));
        invContent.setOpaque(false);
        repInvValSummary = new JLabel("Pantry Raw Material Holdings: Rs.0.00 across 0 registered items.");
        repInvValSummary.setFont(UIUtils.FONT_REGULAR);
        repLowStockSummary = new JLabel("Low Stock Items: 0 items requiring restocking.");
        repLowStockSummary.setFont(UIUtils.FONT_BOLD);
        repLowStockSummary.setForeground(UIUtils.COLOR_DANGER);

        JPanel invText = new JPanel(new GridLayout(2, 1, 0, 4));
        invText.setOpaque(false);
        invText.add(repInvValSummary);
        invText.add(repLowStockSummary);
        invContent.add(invText, BorderLayout.WEST);

        JButton jumpInvBtn = UIUtils.createButton("Open Dedicated Inventory Tab →", UIUtils.COLOR_PRIMARY_DARK, UIUtils.COLOR_CREAM);
        jumpInvBtn.addActionListener(e -> tabbedPane.setSelectedIndex(3)); // Jump to Inventory tab
        invContent.add(jumpInvBtn, BorderLayout.EAST);
        invSummaryCard.add(invContent, BorderLayout.CENTER);
        main.add(invSummaryCard);
        main.add(Box.createVerticalStrut(14));

        // 6. SALARY OVERVIEW (Read-only Table)
        JPanel salCard = UIUtils.createCardPanel();
        salCard.setLayout(new BorderLayout(8, 8));
        JLabel salHeader = new JLabel("5. STAFF PAYROLL REGISTER (READ-ONLY REPORT)");
        salHeader.setFont(UIUtils.FONT_SUBTITLE);
        salHeader.setForeground(UIUtils.COLOR_PRIMARY);
        salCard.add(salHeader, BorderLayout.NORTH);

        String[] cols = {"Staff ID", "Employee Name", "Role / Designation", "Username", "Contact", "Monthly Salary"};
        repEmployeeTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable repTable = new JTable(repEmployeeTableModel);
        UIUtils.styleTable(repTable);
        salCard.add(new JScrollPane(repTable), BorderLayout.CENTER);

        JPanel salFooter = new JPanel(new BorderLayout());
        salFooter.setOpaque(false);
        repPayrollFooter = new JLabel("TOTAL MONTHLY PAYROLL: Rs.0.00");
        repPayrollFooter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        repPayrollFooter.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel salNote = new JLabel("Note: To adjust salaries or onboard staff, switch to the dedicated 'Salary Management' tab.");
        salNote.setFont(UIUtils.FONT_SMALL);
        salNote.setForeground(UIUtils.COLOR_TEXT_MUTED);

        salFooter.add(repPayrollFooter, BorderLayout.WEST);
        salFooter.add(salNote, BorderLayout.EAST);
        salCard.add(salFooter, BorderLayout.SOUTH);

        main.add(salCard);

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createReportMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 6));
        card.setBackground(new Color(254, 252, 247));
        card.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_BORDER, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_SMALL);
        t.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(accentColor);

        JPanel bar = new JPanel();
        bar.setBackground(accentColor);
        bar.setPreferredSize(new Dimension(3, 20));

        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(bar, BorderLayout.WEST);
        return card;
    }

    // =========================================================================
    // Refresh Routines
    // =========================================================================
    public void refreshAllData() {
        refreshDashboardMetrics();
        refreshCategories();
        refreshMenuCategoriesDropdown();
        refreshMenuItems();
        refreshInventory();
        refreshSalaries();
        refreshOrders();
        refreshConsolidatedReport();
    }

    private void refreshDashboardMetrics() {
        try {
            ProfitLossReport rep = service.getProfitLossReport();
            revenueMetricLabel.setText(UIUtils.formatCurrency(rep.getTotalRevenue()));
            salaryMetricLabel.setText(UIUtils.formatCurrency(rep.getTotalSalaryExpense()));
            inventoryMetricLabel.setText(UIUtils.formatCurrency(rep.getTotalInventoryCost()));
            orderCountMetricLabel.setText(String.valueOf(rep.getTotalOrders()));

            if (rep.isProfit()) {
                netPnLMetricLabel.setText("Profit: " + UIUtils.formatCurrency(rep.getNetAmount()));
                netPnLMetricLabel.setForeground(UIUtils.COLOR_SUCCESS);
            } else {
                netPnLMetricLabel.setText("Loss: -" + UIUtils.formatCurrency(Math.abs(rep.getNetAmount())));
                netPnLMetricLabel.setForeground(UIUtils.COLOR_DANGER);
            }

            // Proactive Low-Stock Banner check
            List<InventoryItem> lowItems = service.getLowStockItems();
            if (!lowItems.isEmpty()) {
                lowStockBannerLabel.setText("CRITICAL STOCK ALERT: " + lowItems.size() +
                    " pantry item(s) are at or below low-stock threshold! (" + lowItems.get(0).getItemName() + ")");
                lowStockBanner.setVisible(true);
            } else {
                lowStockBanner.setVisible(false);
            }
        } catch (Exception ex) {
            System.err.println("[AdminDashboard] Error refreshing metrics: " + ex.getMessage());
        }
    }

    private void refreshCategories() {
        categoryTableModel.setRowCount(0);
        List<Category> cats = service.getAllCategories();
        for (Category c : cats) {
            if (c.getName() == null || c.getName().trim().isEmpty()) continue;
            categoryTableModel.addRow(new Object[]{c.getId(), c.getName()});
        }
    }

    private void refreshMenuCategoriesDropdown() {
        dishCategoryCombo.removeAllItems();
        List<Category> cats = service.getAllCategories();
        for (Category c : cats) {
            if (c.getName() != null && !c.getName().trim().isEmpty()) {
                dishCategoryCombo.addItem(c);
            }
        }
    }

    private void refreshMenuItems() {
        filterMenu();
    }

    private void refreshInventory() {
        inventoryTableModel.setRowCount(0);
        invRestockCombo.removeAllItems();
        List<InventoryItem> items = service.getAllInventoryItems();
        for (InventoryItem it : items) {
            if (it.getItemName() == null || it.getItemName().trim().isEmpty()) continue;
            invRestockCombo.addItem(it);

            String status = it.isLowStock() ? "LOW STOCK" : "NORMAL";
            inventoryTableModel.addRow(new Object[]{
                it.getId(),
                it.getItemName(),
                it.getUnit(),
                String.format("%.2f", it.getQuantity()),
                String.format("%.2f", it.getLowStockThreshold()),
                UIUtils.formatCurrency(it.getCostPerUnit()),
                UIUtils.formatCurrency(it.getTotalCost()),
                status
            });
        }
    }

    private void refreshSalaries() {
        employeeTableModel.setRowCount(0);
        salaryEmployeeCombo.removeAllItems();
        List<Employee> emps = service.getAllEmployees();
        for (Employee emp : emps) {
            if (emp.getName() == null || emp.getUsername() == null) continue;
            salaryEmployeeCombo.addItem(emp);
            employeeTableModel.addRow(new Object[]{
                emp.getId(),
                emp.getName(),
                emp.getRole(),
                emp.getUsername(),
                UIUtils.formatCurrency(emp.getSalary()),
                emp.getContact()
            });
        }
    }

    private void refreshOrders() {
        ordersTableModel.setRowCount(0);
        List<Order> orders = service.getAllOrders();
        for (Order o : orders) {
            if (o.getTableNumber() == null || o.getCustomerName() == null) continue;

            StringBuilder itemsSummary = new StringBuilder();
            if (o.getItems() != null) {
                for (OrderItem oi : o.getItems()) {
                    if (itemsSummary.length() > 0) itemsSummary.append(", ");
                    itemsSummary.append(oi.getMenuItemName()).append(" x").append(oi.getQuantity());
                }
            }

            ordersTableModel.addRow(new Object[]{
                "#" + o.getId(),
                o.getTableNumber(),
                o.getCustomerName(),
                o.getStatus(),
                UIUtils.formatCurrency(o.getTotalAmount()),
                o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A",
                itemsSummary.toString()
            });
        }
        if (ordersTableModel.getRowCount() == 0) {
            ordersTableModel.addRow(new Object[]{"-", "No orders recorded yet", "-", "-", "-", "-", "-"});
        }
    }

    private void refreshConsolidatedReport() {
        if (repNetBanner == null) return;
        try {
            ConsolidatedReport rep = service.getConsolidatedReport();
            if (rep == null) return;

            // 1. Executive Bottom Line Banner
            if (rep.isProfit()) {
                repNetBanner.setBackground(new Color(240, 253, 244));
                repNetBanner.setBorder(new CompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 2),
                    new EmptyBorder(16, 20, 16, 20)
                ));
                repNetTitle.setText("EXECUTIVE BOTTOM LINE: NET PROFIT (SURPLUS)");
                repNetTitle.setForeground(new Color(22, 101, 52));
                repNetValue.setText("Profit: +" + UIUtils.formatCurrency(rep.getNetProfitLoss()));
                repNetValue.setForeground(UIUtils.COLOR_SUCCESS);
                repNetFormula.setForeground(new Color(21, 128, 61));
            } else {
                repNetBanner.setBackground(new Color(254, 242, 242));
                repNetBanner.setBorder(new CompoundBorder(
                    new LineBorder(new Color(254, 202, 202), 2),
                    new EmptyBorder(16, 20, 16, 20)
                ));
                repNetTitle.setText("EXECUTIVE BOTTOM LINE: NET LOSS (DEFICIT)");
                repNetTitle.setForeground(new Color(153, 27, 27));
                repNetValue.setText("Loss: -" + UIUtils.formatCurrency(Math.abs(rep.getNetProfitLoss())));
                repNetValue.setForeground(UIUtils.COLOR_DANGER);
                repNetFormula.setForeground(new Color(185, 28, 28));
            }

            // 2. Revenue Breakdown
            repTotalRev.setText(UIUtils.formatCurrency(rep.getTotalRevenue()) + " (" + rep.getTotalBillsCount() + " bills)");
            repCashRev.setText(UIUtils.formatCurrency(rep.getCashRevenue()) + " (" + rep.getCashBillsCount() + " bills)");
            repCardRev.setText(UIUtils.formatCurrency(rep.getCardRevenue()) + " (" + rep.getCardBillsCount() + " bills)");
            repUpiRev.setText(UIUtils.formatCurrency(rep.getUpiRevenue()) + " (" + rep.getUpiBillsCount() + " bills)");

            // 3. Operating Expenses & Valuation
            repSalaryExp.setText(UIUtils.formatCurrency(rep.getTotalSalaryExpense()));
            repInventoryVal.setText(UIUtils.formatCurrency(rep.getTotalInventoryCost()));
            repTotalExp.setText(UIUtils.formatCurrency(rep.getTotalOperatingExpenses()));

            // 4. Order Pipeline & Velocity
            repTotalOrders.setText(rep.getTotalOrdersCount() + " orders");
            repTodayOrders.setText(rep.getTodayOrdersCount() + " today");
            repPlacedOrders.setText(String.valueOf(rep.getPlacedOrdersCount()));
            repPrepOrders.setText(String.valueOf(rep.getPreparingOrdersCount()));
            repServedOrders.setText(String.valueOf(rep.getServedOrdersCount()));
            repDoneOrders.setText(String.valueOf(rep.getCompletedOrdersCount()));

            // 5. Inventory Summary
            repInvValSummary.setText("Pantry Raw Material Holdings: " + UIUtils.formatCurrency(rep.getTotalInventoryCost()) + " across " + rep.getTotalInventoryItemsCount() + " registered items.");
            if (rep.getLowStockItemsCount() > 0) {
                repLowStockSummary.setText("Low Stock Items: " + rep.getLowStockItemsCount() + " items currently requiring restocking!");
                repLowStockSummary.setForeground(UIUtils.COLOR_DANGER);
            } else {
                repLowStockSummary.setText("Low Stock Items: All pantry items are adequately stocked.");
                repLowStockSummary.setForeground(UIUtils.COLOR_SUCCESS);
            }

            // 6. Salary Overview Table
            repEmployeeTableModel.setRowCount(0);
            List<Employee> emps = rep.getEmployeeList();
            if (emps != null) {
                for (Employee emp : emps) {
                    if (emp.getName() == null) continue;
                    repEmployeeTableModel.addRow(new Object[]{
                        "#" + emp.getId(),
                        emp.getName(),
                        emp.getRole(),
                        emp.getUsername(),
                        emp.getContact() != null ? emp.getContact() : "-",
                        UIUtils.formatCurrency(emp.getSalary())
                    });
                }
            }
            int empCount = emps != null ? emps.size() : 0;
            repPayrollFooter.setText("TOTAL MONTHLY PAYROLL: " + UIUtils.formatCurrency(rep.getTotalSalaryExpense()) + " (" + empCount + " Staff Members)");

        } catch (Exception ex) {
            System.err.println("[AdminDashboard] Error refreshing consolidated report: " + ex.getMessage());
        }
    }
}
