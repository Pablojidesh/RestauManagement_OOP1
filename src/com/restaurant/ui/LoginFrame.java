package com.restaurant.ui;

import com.restaurant.model.Employee;
import com.restaurant.service.RestaurantService;
import com.restaurant.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame
 * 
 * Desktop authentication portal for "The Royal Spice" Restaurant Management System.
 * Matched to the exact dimensions (1100x750) of post-login portals to ensure
 * a seamless, continuous, zero-resize visual transition upon successful login.
 * 
 * Features a split layout:
 * - Left Hero Panel: Royal Spice brand heritage, vector logo, and core feature pillars.
 * - Right Panel: Clean, centered staff login card with credentials guidance.
 */
public class LoginFrame extends JFrame {

    private final RestaurantService service;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame(RestaurantService service) {
        super("The Royal Spice - Staff Authentication Portal");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Match exact size to Admin, Kitchen, and Waiter portal frames
        setSize(1100, 750);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        rootPanel.setBackground(UIUtils.COLOR_BG);

        // 1. LEFT COLUMN: Royal Spice Brand Hero Panel
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, UIUtils.COLOR_PRIMARY,
                    0, getHeight(), UIUtils.COLOR_PRIMARY_DARK
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        heroPanel.setLayout(new GridBagLayout());
        heroPanel.setBorder(new EmptyBorder(40, 36, 40, 36));

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0; hgbc.gridy = 0;
        hgbc.insets = new Insets(8, 0, 8, 0);
        hgbc.anchor = GridBagConstraints.CENTER;

        // Large 64px Vector Logo
        JLabel logoLabel = new JLabel(new UIUtils.RoyalSpiceLogoIcon(64));
        heroPanel.add(logoLabel, hgbc);

        hgbc.gridy = 1;
        JLabel brandTitle = new JLabel("THE ROYAL SPICE");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandTitle.setForeground(UIUtils.COLOR_CREAM);
        heroPanel.add(brandTitle, hgbc);

        hgbc.gridy = 2;
        JLabel brandTagline = new JLabel("Fine Dining & Heritage Hospitality Management");
        brandTagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        brandTagline.setForeground(new Color(254, 243, 199));
        heroPanel.add(brandTagline, hgbc);

        // Decorative Gold Line
        hgbc.gridy = 3;
        hgbc.insets = new Insets(16, 0, 20, 0);
        JPanel divider = new JPanel();
        divider.setBackground(UIUtils.COLOR_GOLD_BRIGHT);
        divider.setPreferredSize(new Dimension(140, 2));
        heroPanel.add(divider, hgbc);

        // Pillar Highlights
        hgbc.gridy = 4;
        hgbc.insets = new Insets(4, 0, 4, 0);
        hgbc.anchor = GridBagConstraints.WEST;
        JPanel pillarsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        pillarsPanel.setOpaque(false);

        pillarsPanel.add(createPillarItem("Dining POS & Dynamic Polymorphic Billing", "Fast table checkout with Cash, Card, and UPI."));
        pillarsPanel.add(createPillarItem("Interactive Kitchen Display System (KDS)", "Real-time ticket board with auto-sync status."));
        pillarsPanel.add(createPillarItem("Proactive Pantry Stock & Restock Alerts", "Automated low-inventory threshold detection."));
        pillarsPanel.add(createPillarItem("Executive Financial Audits & Reports", "Unified P&L statement and payroll overview."));

        heroPanel.add(pillarsPanel, hgbc);

        // Version badge at bottom
        hgbc.gridy = 5;
        hgbc.insets = new Insets(24, 0, 0, 0);
        hgbc.anchor = GridBagConstraints.CENTER;
        JLabel versionBadge = new JLabel("Enterprise Edition • Core Java Desktop App");
        versionBadge.setFont(UIUtils.FONT_SMALL);
        versionBadge.setForeground(new Color(254, 243, 199, 180));
        heroPanel.add(versionBadge, hgbc);

        rootPanel.add(heroPanel);

        // 2. RIGHT COLUMN: Centered Login Card
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setBackground(UIUtils.COLOR_BG);

        JPanel loginCard = UIUtils.createCardPanel();
        loginCard.setPreferredSize(new Dimension(440, 520));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(new CompoundBorder(
            new LineBorder(UIUtils.COLOR_BORDER, 1),
            new EmptyBorder(28, 32, 28, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel formTitle = new JLabel("Staff Portal Sign-In");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setForeground(UIUtils.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginCard.add(formTitle, gbc);

        JLabel formSub = new JLabel("Authenticate with your assigned staff credentials");
        formSub.setFont(UIUtils.FONT_REGULAR);
        formSub.setForeground(UIUtils.COLOR_TEXT_MUTED);
        gbc.gridy = 1;
        loginCard.add(formSub, gbc);

        // Error message placeholder
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIUtils.FONT_SMALL);
        errorLabel.setForeground(UIUtils.COLOR_DANGER);
        gbc.gridy = 2;
        loginCard.add(errorLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UIUtils.FONT_BOLD);
        loginCard.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = UIUtils.createTextField(16);
        usernameField.setPreferredSize(new Dimension(200, 34));
        loginCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UIUtils.FONT_BOLD);
        loginCard.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(16);
        passwordField.setFont(UIUtils.FONT_REGULAR);
        passwordField.setBorder(usernameField.getBorder());
        passwordField.setPreferredSize(new Dimension(200, 34));
        loginCard.add(passwordField, gbc);

        // Enter key listeners
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        // Sign In Button
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 4, 12, 4);
        JButton loginBtn = UIUtils.createButton("Sign In to Portal", UIUtils.COLOR_ACCENT, Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(220, 38));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.addActionListener(e -> performLogin());
        loginCard.add(loginBtn, gbc);

        // Quick Role Credentials Hint Box
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 4, 4, 4);
        JPanel hintBox = new JPanel(new GridLayout(4, 1, 2, 4));
        hintBox.setBackground(new Color(254, 252, 247));
        hintBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(243, 230, 211), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel hintTitle = new JLabel("PRE-CONFIGURED DEMO LOGINS:");
        hintTitle.setFont(UIUtils.FONT_BADGE);
        hintTitle.setForeground(UIUtils.COLOR_PRIMARY);
        hintBox.add(hintTitle);

        hintBox.add(createCredPill("ADMIN", "admin", "admin123", "(Full Suite & Reports)"));
        hintBox.add(createCredPill("KITCHEN", "chef1", "chef123", "(KDS Orders Queue & Salary)"));
        hintBox.add(createCredPill("WAITER", "waiter1", "waiter123", "(POS Cart & Checkout)"));
        loginCard.add(hintBox, gbc);

        rightContainer.add(loginCard);
        rootPanel.add(rightContainer);

        add(rootPanel);
    }

    private JPanel createPillarItem(String title, String desc) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel t = new JLabel("•  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(UIUtils.COLOR_CREAM);

        JLabel d = new JLabel("    " + desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(new Color(254, 243, 199, 210));

        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        return p;
    }

    private JLabel createCredPill(String role, String user, String pass, String desc) {
        JLabel l = new JLabel("• " + role + ": " + user + " / " + pass + " " + desc);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.COLOR_TEXT_MAIN);
        return l;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            Employee emp = service.login(username, password);
            if (emp == null) {
                errorLabel.setText("Invalid username or password.");
                return;
            }

            errorLabel.setText(" ");

            // Seamless transition: matching 1100x750 frame dimensions with no jump
            if (emp.isAdmin()) {
                AdminDashboardFrame admin = new AdminDashboardFrame(service);
                admin.setVisible(true);
            } else if (emp.isKitchen()) {
                KitchenDashboardFrame kitchen = new KitchenDashboardFrame(service);
                kitchen.setVisible(true);
            } else if (emp.isWaiter()) {
                WaiterDashboardFrame waiter = new WaiterDashboardFrame(service);
                waiter.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Unknown role assigned to user: " + emp.getRole(),
                    "Access Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dispose(); // Close login window cleanly
        } catch (Exception ex) {
            errorLabel.setText("Authentication failed: " + ex.getMessage());
        }
    }
}
