package com.restaurant;

import com.restaurant.config.DBConnection;
import com.restaurant.service.RestaurantService;
import com.restaurant.ui.LoginFrame;
import com.restaurant.ui.components.UIUtils;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Main
 * 
 * Application Entry Point for "The Royal Spice" Restaurant Management System.
 * 1. Sets up the modern FlatLaf Look and Feel with brand theme customizations.
 * 2. Runs an immediate startup database connectivity verification, showing a descriptive
 *    error dialog if MySQL is unreachable (no silent failures).
 * 3. Launches the staff authentication window on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize Modern FlatLaf Look and Feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", UIUtils.COLOR_CREAM);
            UIManager.put("TabbedPane.underlineColor", UIUtils.COLOR_ACCENT);
        } catch (Throwable t) {
            System.err.println("[Main] Could not load FlatLaf, falling back to System Look and Feel: " + t.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // 2. Perform Immediate Database Connectivity Check on Startup
        try {
            DBConnection.testConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                e.getMessage(),
                "MySQL Database Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.err.println("[Main] Startup halted due to database connection failure.");
            System.exit(1);
        }

        // 3. Launch Application Window on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                RestaurantService service = new RestaurantService();
                LoginFrame loginFrame = new LoginFrame(service);
                loginFrame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Error launching Restaurant Management System:\n" + ex.getMessage(),
                    "Startup Failure", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
}
