package com.restaurant.ui.components;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UIUtils
 * 
 * Central User Interface design system for "The Royal Spice" Restaurant Management System.
 * Enforces a royal maroon, gold, and warm ivory spice-themed visual language.
 * Provides custom Graphics2D vector icons, colored status badges, modern JTable renderers,
 * and standard plain-text currency formatting (Rs.xxx.xx).
 */
public class UIUtils {

    // "The Royal Spice" Brand Color Palette (Deep Royal Maroon & Saffron Gold)
    public static final Color COLOR_PRIMARY = new Color(126, 18, 29);         // Deep Royal Maroon
    public static final Color COLOR_PRIMARY_DARK = new Color(88, 12, 19);    // Rich Burgundy
    public static final Color COLOR_PRIMARY_LIGHT = new Color(159, 29, 43);
    public static final Color COLOR_ACCENT = new Color(217, 119, 6);         // Saffron Gold
    public static final Color COLOR_ACCENT_HOVER = new Color(180, 83, 9);
    public static final Color COLOR_GOLD_BRIGHT = new Color(245, 158, 11);
    public static final Color COLOR_CREAM = new Color(255, 251, 235);        // Warm Ivory
    public static final Color COLOR_BG = new Color(250, 248, 245);           // Soft Linen Canvas
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(230, 225, 218);
    public static final Color COLOR_TEXT_MAIN = new Color(28, 25, 23);       // Charcoal
    public static final Color COLOR_TEXT_MUTED = new Color(120, 113, 108);

    // Status Badge Semantic Colors
    public static final Color STATUS_PLACED_BG = new Color(254, 243, 199);   // Amber Tint
    public static final Color STATUS_PLACED_FG = new Color(146, 64, 14);
    public static final Color STATUS_PREP_BG = new Color(219, 234, 254);     // Sky Blue Tint
    public static final Color STATUS_PREP_FG = new Color(30, 64, 175);
    public static final Color STATUS_SERVED_BG = new Color(209, 250, 229);   // Emerald Tint
    public static final Color STATUS_SERVED_FG = new Color(6, 95, 70);
    public static final Color STATUS_DONE_BG = new Color(237, 233, 254);     // Violet Tint
    public static final Color STATUS_DONE_FG = new Color(91, 33, 182);
    public static final Color STATUS_DANGER_BG = new Color(254, 226, 226);   // Coral Tint
    public static final Color STATUS_DANGER_FG = new Color(185, 28, 28);

    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    public static final Color COLOR_DANGER = new Color(220, 38, 38);
    public static final Color COLOR_WARNING = new Color(217, 119, 6);

    // Typography
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Formats currency as plain text (e.g. 'Rs.123.45').
     * Avoids Unicode rupee symbol to eliminate cross-platform font rendering issues.
     */
    public static String formatCurrency(double amount) {
        return String.format("Rs.%.2f", amount);
    }

    /**
     * Creates a styled modern button with rounded corners and consistent padding.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a standard styled text field.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * Creates an elevated card panel with subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /**
     * Applies uniform restaurant-themed styling to any JTable.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(new Color(245, 240, 235));
        table.setSelectionBackground(new Color(254, 243, 199)); // Soft saffron gold highlight
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setBorder(new LineBorder(COLOR_PRIMARY_DARK, 1));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Default cell padding
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                return l;
            }
        };
        table.setDefaultRenderer(Object.class, paddedRenderer);
    }

    /**
     * Custom TableCellRenderer rendering colored status pill badges (Placed, Preparing, Served, LOW STOCK, NORMAL).
     */
    public static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String text = value != null ? value.toString() : "";
            JPanel pillPanel = new JPanel(new GridBagLayout());
            pillPanel.setOpaque(true);
            pillPanel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setFont(FONT_BADGE);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));

            if ("Placed".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_PLACED_BG);
                badge.setForeground(STATUS_PLACED_FG);
            } else if ("Preparing".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_PREP_BG);
                badge.setForeground(STATUS_PREP_FG);
            } else if ("Served".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_SERVED_BG);
                badge.setForeground(STATUS_SERVED_FG);
            } else if ("Completed".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_DONE_BG);
                badge.setForeground(STATUS_DONE_FG);
            } else if ("LOW STOCK".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_DANGER_BG);
                badge.setForeground(STATUS_DANGER_FG);
            } else if ("NORMAL".equalsIgnoreCase(text)) {
                badge.setBackground(STATUS_SERVED_BG);
                badge.setForeground(STATUS_SERVED_FG);
            } else {
                badge.setBackground(new Color(241, 245, 249));
                badge.setForeground(COLOR_TEXT_MAIN);
            }

            pillPanel.add(badge);
            return pillPanel;
        }
    }

    /**
     * Vector Graphics2D Logo Icon for "The Royal Spice".
     * Draws an elegant golden cloche & aroma flame mark.
     */
    public static class RoyalSpiceLogoIcon implements Icon {
        private final int size;

        public RoyalSpiceLogoIcon(int size) {
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Scale factor
            double s = size / 32.0;

            // Cloche Tray base (Gold)
            g2.setColor(COLOR_GOLD_BRIGHT);
            g2.fillRoundRect((int) (x + 3 * s), (int) (y + 24 * s), (int) (26 * s), (int) (4 * s), (int) (2 * s), (int) (2 * s));

            // Cloche Dome (Gold Gradient)
            GradientPaint gp = new GradientPaint(
                (float) (x + 16 * s), (float) (y + 10 * s), COLOR_GOLD_BRIGHT,
                (float) (x + 16 * s), (float) (y + 23 * s), COLOR_ACCENT
            );
            g2.setPaint(gp);
            g2.fillArc((int) (x + 5 * s), (int) (y + 11 * s), (int) (22 * s), (int) (22 * s), 0, 180);

            // Dome Handle
            g2.setColor(Color.WHITE);
            g2.fillOval((int) (x + 14 * s), (int) (y + 8 * s), (int) (4 * s), (int) (4 * s));

            // Spice Steam Ribbon
            g2.setColor(new Color(254, 243, 199, 200));
            g2.setStroke(new BasicStroke((float) (1.8 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc((int) (x + 11 * s), (int) (y + 2 * s), (int) (4 * s), (int) (6 * s), 0, 180);
            g2.drawArc((int) (x + 17 * s), (int) (y + 3 * s), (int) (4 * s), (int) (5 * s), 180, 180);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /**
     * Builds a cohesive, high-end branded header banner for any screen.
     */
    public static JPanel createHeaderPanel(String screenSubtitle, String roleLabel, String userName, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left Branding: Logo + Title + Subtitle
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBrand.setOpaque(false);

        JLabel logo = new JLabel(new RoyalSpiceLogoIcon(32));
        leftBrand.add(logo);

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);

        JLabel titleLbl = new JLabel("THE ROYAL SPICE");
        titleLbl.setFont(FONT_BRAND);
        titleLbl.setForeground(COLOR_CREAM);

        JLabel subLbl = new JLabel(screenSubtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(new Color(254, 243, 199));

        textGroup.add(titleLbl);
        textGroup.add(subLbl);
        leftBrand.add(textGroup);

        header.add(leftBrand, BorderLayout.WEST);

        // Right Branding: User badge + Logout
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        rightGroup.setOpaque(false);

        if (userName != null && !userName.isEmpty()) {
            String labelText = (roleLabel != null && !roleLabel.equalsIgnoreCase(userName))
                ? roleLabel + ": " + userName
                : userName;
            JLabel userBadge = new JLabel(labelText);
            userBadge.setFont(FONT_BOLD);
            userBadge.setForeground(COLOR_CREAM);
            rightGroup.add(userBadge);
        }

        if (onLogout != null) {
            JButton logoutBtn = createButton("Logout", COLOR_PRIMARY_DARK, COLOR_CREAM);
            logoutBtn.addActionListener(e -> onLogout.run());
            rightGroup.add(logoutBtn);
        }

        header.add(rightGroup, BorderLayout.EAST);
        return header;
    }

    /**
     * Creates an empty state panel when a table or queue contains zero rows.
     */
    public static JPanel createEmptyStatePanel(String title, String subtitle) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);

        JLabel iconLabel = new JLabel(new RoyalSpiceLogoIcon(48));
        panel.add(iconLabel, gbc);

        gbc.gridy = 1;
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        panel.add(titleLabel, gbc);

        gbc.gridy = 2;
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_REGULAR);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        panel.add(subLabel, gbc);

        return panel;
    }
}
