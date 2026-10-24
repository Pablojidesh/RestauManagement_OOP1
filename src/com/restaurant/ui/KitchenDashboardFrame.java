package com.restaurant.ui;

import com.restaurant.model.Employee;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.service.RestaurantService;
import com.restaurant.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * KitchenDashboardFrame
 * 
 * High-performance Kitchen Display System (KDS) for chef staff.
 * Features:
 * 1. Interactive Visual Ticket Board: Card-based real-time layout resembling modern restaurant KDS.
 * 2. Background Auto-Sync: javax.swing.Timer automatically polls and updates the queue every 3.5 seconds.
 * 3. Immediate manual refresh and action buttons (Start Preparing -> Mark Served).
 * 4. Classical Table Queue view with colored status badge renderers and empty-state placeholders.
 * 5. Private, read-only personal salary card fetched fresh from DB by session employee ID.
 */
public class KitchenDashboardFrame extends JFrame {

    private final RestaurantService service;
    private final Timer autoRefreshTimer;

    // View Components
    private JPanel ticketCardsContainer;
    private JScrollPane ticketScrollPane;
    private JLabel queueStatsLabel;

    // Table view components
    private DefaultTableModel ordersTableModel;
    private DefaultTableModel itemsTableModel;
    private JTable ordersTable;
    private List<Order> currentOrders;

    // Salary view
    private JLabel chefSalaryLabel;

    public KitchenDashboardFrame(RestaurantService service) {
        super("The Royal Spice - Kitchen Display System (KDS)");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        initUI();

        // Setup background auto-refresh timer (polls active orders every 3.5 seconds)
        autoRefreshTimer = new Timer(3500, e -> refreshOrdersQueue());
        autoRefreshTimer.start();

        // Stop timer on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
                    autoRefreshTimer.stop();
                }
            }
        });

        // Initial fetch
        refreshOrdersQueue();
        refreshSalaryView();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIUtils.COLOR_BG);

        // Branded Royal Maroon Header
        JPanel header = UIUtils.createHeaderPanel(
            "Kitchen Display System (KDS)",
            "Chef on Duty",
            service.getCurrentUser().getName(),
            () -> {
                if (autoRefreshTimer != null) autoRefreshTimer.stop();
                service.logout();
                new LoginFrame(service).setVisible(true);
                dispose();
            }
        );
        rootPanel.add(header, BorderLayout.NORTH);

        // Tabbed Navigation: 1. Visual KDS Tickets, 2. Queue Table View, 3. My Salary
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.addTab("Visual KDS Ticket Board", buildTicketBoardTab());
        tabbedPane.addTab("Queue Table View", buildQueueTableTab());
        tabbedPane.addTab("My Salary & Profile", buildSalaryTab());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    // =========================================================================
    // Tab 1: Card-Based Kitchen Display System (KDS)
    // =========================================================================
    private JPanel buildTicketBoardTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Subheader Toolbar
        JPanel toolbar = UIUtils.createCardPanel();
        toolbar.setLayout(new BorderLayout());

        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        leftStatus.setOpaque(false);
        queueStatsLabel = new JLabel("Active Tickets: Loading...");
        queueStatsLabel.setFont(UIUtils.FONT_SUBTITLE);
        queueStatsLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel syncBadge = new JLabel("● Auto-Sync Active (3.5s)");
        syncBadge.setFont(UIUtils.FONT_BADGE);
        syncBadge.setForeground(UIUtils.COLOR_SUCCESS);

        leftStatus.add(queueStatsLabel);
        leftStatus.add(syncBadge);
        toolbar.add(leftStatus, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightActions.setOpaque(false);

        JButton refreshBtn = UIUtils.createButton("↻ Refresh Queue Now", UIUtils.COLOR_ACCENT, Color.WHITE);
        refreshBtn.addActionListener(e -> {
            refreshOrdersQueue();
            JOptionPane.showMessageDialog(this, "Kitchen orders queue refreshed!", "Queue Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });
        rightActions.add(refreshBtn);
        toolbar.add(rightActions, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Ticket Cards Scroll Container
        ticketCardsContainer = new JPanel();
        ticketCardsContainer.setBackground(UIUtils.COLOR_BG);
        ticketCardsContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));

        ticketScrollPane = new JScrollPane(ticketCardsContainer);
        ticketScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ticketScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(ticketScrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // Tab 2: Classical Queue Table View
    // =========================================================================
    private JPanel buildQueueTableTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Controls bar
        JPanel bar = UIUtils.createCardPanel();
        bar.setLayout(new BorderLayout());
        JLabel tTitle = new JLabel("Tabular Orders Queue (Placed / Preparing / Served)");
        tTitle.setFont(UIUtils.FONT_SUBTITLE);
        bar.add(tTitle, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        JButton prepBtn = UIUtils.createButton("Set Status: Preparing", UIUtils.COLOR_ACCENT, Color.WHITE);
        prepBtn.addActionListener(e -> updateSelectedOrderStatus("Preparing"));

        JButton servedBtn = UIUtils.createButton("Set Status: Served", UIUtils.COLOR_SUCCESS, Color.WHITE);
        servedBtn.addActionListener(e -> updateSelectedOrderStatus("Served"));

        JButton refBtn = UIUtils.createButton("Refresh Table", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refBtn.addActionListener(e -> refreshOrdersQueue());

        btnGroup.add(prepBtn);
        btnGroup.add(servedBtn);
        btnGroup.add(refBtn);
        bar.add(btnGroup, BorderLayout.EAST);
        panel.add(bar, BorderLayout.NORTH);

        // Split view: Orders Table on top, Items Table on bottom
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        String[] orderCols = {"Order ID", "Dining Table", "Guest Name", "Order Status", "Placed At"};
        ordersTableModel = new DefaultTableModel(orderCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = new JTable(ordersTableModel);
        UIUtils.styleTable(ordersTable);
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.BadgeCellRenderer());

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedOrderItems();
            }
        });
        split.setTopComponent(new JScrollPane(ordersTable));

        // Line Items Table
        String[] itemCols = {"Dish Item Name", "Quantity", "Preparation Note"};
        itemsTableModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable itemsTable = new JTable(itemsTableModel);
        UIUtils.styleTable(itemsTable);

        JPanel itemsPanel = UIUtils.createCardPanel();
        itemsPanel.setLayout(new BorderLayout(5, 5));
        JLabel itemsTitle = new JLabel("Selected Order Dishes & Cooking Checklist:");
        itemsTitle.setFont(UIUtils.FONT_BOLD);
        itemsPanel.add(itemsTitle, BorderLayout.NORTH);
        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        split.setBottomComponent(itemsPanel);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // Tab 3: My Salary View
    // =========================================================================
    private JPanel buildSalaryTab() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        panel.setBackground(UIUtils.COLOR_BG);

        JPanel card = UIUtils.createCardPanel();
        card.setPreferredSize(new Dimension(520, 340));
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
        JLabel roleVal = new JLabel("Kitchen Staff (" + user.getRole() + ")");
        roleVal.setFont(UIUtils.FONT_BOLD);
        details.add(roleVal);

        details.add(new JLabel("Staff Username:"));
        details.add(new JLabel(user.getUsername()));

        details.add(new JLabel("Contact Number:"));
        details.add(new JLabel(user.getContact()));

        details.add(new JLabel("Monthly Salary:"));
        chefSalaryLabel = new JLabel("Rs.0.00");
        chefSalaryLabel.setFont(UIUtils.FONT_TITLE);
        chefSalaryLabel.setForeground(UIUtils.COLOR_SUCCESS);
        details.add(chefSalaryLabel);

        card.add(details, BorderLayout.CENTER);

        JButton refreshSalBtn = UIUtils.createButton("Check Fresh Salary from DB", UIUtils.COLOR_PRIMARY, Color.WHITE);
        refreshSalBtn.addActionListener(e -> {
            refreshSalaryView();
            JOptionPane.showMessageDialog(this, "Salary verified fresh from database: " + chefSalaryLabel.getText(),
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
    // Core Refresh & Ticket Rendering Logic
    // =========================================================================
    public void refreshOrdersQueue() {
        currentOrders = service.getActiveOrders();
        queueStatsLabel.setText("Active Tickets: " + currentOrders.size() + " orders in kitchen pipeline");

        // 1. Render Visual KDS Ticket Cards
        ticketCardsContainer.removeAll();
        if (currentOrders.isEmpty()) {
            JPanel empty = UIUtils.createEmptyStatePanel(
                "Kitchen Queue is Clear!",
                "All caught up! Orders placed by waitstaff will appear here automatically."
            );
            empty.setPreferredSize(new Dimension(800, 350));
            ticketCardsContainer.add(empty);
        } else {
            for (Order order : currentOrders) {
                ticketCardsContainer.add(createTicketCard(order));
            }
        }
        ticketCardsContainer.revalidate();
        ticketCardsContainer.repaint();

        // 2. Render Tabular View
        ordersTableModel.setRowCount(0);
        if (currentOrders.isEmpty()) {
            ordersTableModel.addRow(new Object[]{"-", "No active orders", "-", "-", "-"});
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            for (Order o : currentOrders) {
                String timeStr = o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "Just now";
                ordersTableModel.addRow(new Object[]{
                    "#" + o.getId(),
                    o.getTableNumber(),
                    o.getCustomerName(),
                    o.getStatus(),
                    timeStr
                });
            }
        }
    }

    /**
     * Builds a single Kitchen Order Ticket Card.
     */
    private JPanel createTicketCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(310, 360));
        card.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_BORDER, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // Card Top: Table #, Order #, Status Badge
        JPanel cardTop = new JPanel(new BorderLayout(6, 4));
        cardTop.setOpaque(false);

        JPanel tableBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tableBadgePanel.setOpaque(false);
        JLabel tableBadge = new JLabel(order.getTableNumber());
        tableBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableBadge.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel orderNum = new JLabel("  Order #" + order.getId());
        orderNum.setFont(UIUtils.FONT_SMALL);
        orderNum.setForeground(UIUtils.COLOR_TEXT_MUTED);

        tableBadgePanel.add(tableBadge);
        tableBadgePanel.add(orderNum);
        cardTop.add(tableBadgePanel, BorderLayout.WEST);

        // Status pill
        JLabel statusPill = new JLabel(" " + order.getStatus() + " ");
        statusPill.setFont(UIUtils.FONT_BADGE);
        statusPill.setOpaque(true);
        statusPill.setBorder(new EmptyBorder(3, 8, 3, 8));
        if ("Placed".equalsIgnoreCase(order.getStatus())) {
            statusPill.setBackground(UIUtils.STATUS_PLACED_BG);
            statusPill.setForeground(UIUtils.STATUS_PLACED_FG);
        } else if ("Preparing".equalsIgnoreCase(order.getStatus())) {
            statusPill.setBackground(UIUtils.STATUS_PREP_BG);
            statusPill.setForeground(UIUtils.STATUS_PREP_FG);
        } else if ("Served".equalsIgnoreCase(order.getStatus())) {
            statusPill.setBackground(UIUtils.STATUS_SERVED_BG);
            statusPill.setForeground(UIUtils.STATUS_SERVED_FG);
        }
        cardTop.add(statusPill, BorderLayout.EAST);

        JLabel guestLabel = new JLabel("Guest: " + order.getCustomerName());
        guestLabel.setFont(UIUtils.FONT_SMALL);
        guestLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        cardTop.add(guestLabel, BorderLayout.SOUTH);

        card.add(cardTop, BorderLayout.NORTH);

        // Card Center: Dishes checklist
        JPanel itemsListPanel = new JPanel();
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));
        itemsListPanel.setOpaque(false);
        itemsListPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, UIUtils.COLOR_BORDER),
            new EmptyBorder(8, 0, 8, 0)
        ));

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem oi : order.getItems()) {
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setOpaque(false);
                itemRow.setBorder(new EmptyBorder(3, 0, 3, 0));

                JLabel qtyLabel = new JLabel("[" + oi.getQuantity() + "x] ");
                qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                qtyLabel.setForeground(UIUtils.COLOR_ACCENT);

                JLabel nameLabel = new JLabel(oi.getMenuItemName());
                nameLabel.setFont(UIUtils.FONT_REGULAR);
                nameLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

                itemRow.add(qtyLabel, BorderLayout.WEST);
                itemRow.add(nameLabel, BorderLayout.CENTER);
                itemsListPanel.add(itemRow);
            }
        } else {
            JLabel emptyItems = new JLabel("No item details loaded");
            emptyItems.setFont(UIUtils.FONT_SMALL);
            itemsListPanel.add(emptyItems);
        }

        JScrollPane itemsScroll = new JScrollPane(itemsListPanel);
        itemsScroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(itemsScroll, BorderLayout.CENTER);

        // Card Bottom: Status Action Button
        JPanel actionPanel = new JPanel(new GridLayout(1, 1, 6, 6));
        actionPanel.setOpaque(false);

        if ("Placed".equalsIgnoreCase(order.getStatus())) {
            JButton startBtn = UIUtils.createButton("👨‍🍳 Start Preparing", UIUtils.COLOR_ACCENT, Color.WHITE);
            startBtn.addActionListener(e -> {
                service.updateOrderStatus(order.getId(), "Preparing");
                refreshOrdersQueue();
            });
            actionPanel.add(startBtn);
        } else if ("Preparing".equalsIgnoreCase(order.getStatus())) {
            JButton servedBtn = UIUtils.createButton("✅ Mark as Served", UIUtils.COLOR_SUCCESS, Color.WHITE);
            servedBtn.addActionListener(e -> {
                service.updateOrderStatus(order.getId(), "Served");
                refreshOrdersQueue();
            });
            actionPanel.add(servedBtn);
        } else {
            JLabel doneLabel = new JLabel("Ready / Served to Table", SwingConstants.CENTER);
            doneLabel.setFont(UIUtils.FONT_BOLD);
            doneLabel.setForeground(UIUtils.COLOR_SUCCESS);
            actionPanel.add(doneLabel);
        }

        card.add(actionPanel, BorderLayout.SOUTH);
        return card;
    }

    private void updateSelectedOrderStatus(String newStatus) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0 || currentOrders == null || selectedRow >= currentOrders.size()) {
            JOptionPane.showMessageDialog(this, "Please select an active order first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Order order = currentOrders.get(selectedRow);
        try {
            service.updateOrderStatus(order.getId(), newStatus);
            JOptionPane.showMessageDialog(this, "Order #" + order.getId() + " updated to '" + newStatus + "'!", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshOrdersQueue();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Updating Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedOrderItems() {
        itemsTableModel.setRowCount(0);
        int row = ordersTable.getSelectedRow();
        if (row >= 0 && currentOrders != null && row < currentOrders.size()) {
            Order o = currentOrders.get(row);
            if (o.getItems() != null && !o.getItems().isEmpty()) {
                for (OrderItem item : o.getItems()) {
                    itemsTableModel.addRow(new Object[]{
                        item.getMenuItemName(),
                        item.getQuantity(),
                        "Standard Kitchen Prep"
                    });
                }
            } else {
                itemsTableModel.addRow(new Object[]{"No items recorded", "-", "-"});
            }
        }
    }

    private void refreshSalaryView() {
        try {
            double freshSalary = service.getFreshSalaryForCurrentEmployee();
            chefSalaryLabel.setText(UIUtils.formatCurrency(freshSalary));
        } catch (Exception ex) {
            System.err.println("[KitchenDashboard] Error fetching fresh salary: " + ex.getMessage());
        }
    }
}
