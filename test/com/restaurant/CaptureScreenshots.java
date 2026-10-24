package com.restaurant;

import com.restaurant.model.Employee;
import com.restaurant.service.RestaurantService;
import com.restaurant.ui.*;
import com.restaurant.ui.components.UIUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * CaptureScreenshots
 * 
 * Renders the modernized swing windows offscreen and exports high-definition
 * PNG screenshots for visual review.
 */
public class CaptureScreenshots {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting UI Screenshot Captures...");

        // Setup FlatLaf Look and Feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", UIUtils.COLOR_CREAM);
            UIManager.put("TabbedPane.underlineColor", UIUtils.COLOR_ACCENT);
        } catch (Exception e) {
            System.err.println("FlatLaf setup note: " + e.getMessage());
        }

        RestaurantService service = new RestaurantService();

        File outputDir = new File("screenshots");
        if (!outputDir.exists()) outputDir.mkdirs();

        // 1. Login Frame Screenshot (Resized 1100x750 with branded hero panel)
        LoginFrame loginFrame = new LoginFrame(service);
        loginFrame.setSize(1100, 750);
        loginFrame.doLayout();
        saveComponentImage(loginFrame, new File(outputDir, "login_portal.png"));
        loginFrame.dispose();
        System.out.println("Captured login_portal.png");

        // 2. Kitchen Portal Screenshot
        Employee chef = service.login("chef1", "chef123");
        KitchenDashboardFrame kitchenFrame = new KitchenDashboardFrame(service);
        kitchenFrame.setSize(1100, 750);
        kitchenFrame.doLayout();
        saveComponentImage(kitchenFrame, new File(outputDir, "kitchen_portal.png"));
        kitchenFrame.dispose();
        System.out.println("Captured kitchen_portal.png");

        // 3. Waiter Portal Screenshot
        Employee waiter = service.login("waiter1", "waiter123");
        WaiterDashboardFrame waiterFrame = new WaiterDashboardFrame(service);
        waiterFrame.setSize(1100, 750);
        waiterFrame.doLayout();
        saveComponentImage(waiterFrame, new File(outputDir, "waiter_portal.png"));
        waiterFrame.dispose();
        System.out.println("Captured waiter_portal.png");

        // 4. Admin Dashboard (Overview tab) Screenshot
        Employee admin = service.login("admin", "admin123");
        AdminDashboardFrame adminFrame = new AdminDashboardFrame(service);
        adminFrame.setSize(1100, 750);
        adminFrame.setSelectedTab(0);
        adminFrame.doLayout();
        saveComponentImage(adminFrame, new File(outputDir, "admin_dashboard.png"));
        System.out.println("Captured admin_dashboard.png");

        // 5. Admin Dashboard (Consolidated Reports tab) Screenshot
        adminFrame.setSelectedTab(6);
        adminFrame.doLayout();
        saveComponentImage(adminFrame, new File(outputDir, "admin_reports.png"));
        adminFrame.dispose();
        System.out.println("Captured admin_reports.png");

        // Also copy into artifact directory for inline embedding
        File artifactDir = new File("C:/Users/anupa_zaojzqx/.gemini/antigravity/brain/390b125c-75f4-4362-b55a-ef573e858ac4");
        if (artifactDir.exists()) {
            copyFile(new File(outputDir, "login_portal.png"), new File(artifactDir, "login_portal.png"));
            copyFile(new File(outputDir, "kitchen_portal.png"), new File(artifactDir, "kitchen_portal.png"));
            copyFile(new File(outputDir, "waiter_portal.png"), new File(artifactDir, "waiter_portal.png"));
            copyFile(new File(outputDir, "admin_dashboard.png"), new File(artifactDir, "admin_dashboard.png"));
            copyFile(new File(outputDir, "admin_reports.png"), new File(artifactDir, "admin_reports.png"));
            System.out.println("Copied screenshots to artifact directory.");
        }

        System.out.println("All screenshots captured successfully!");
        System.exit(0);
    }

    private static void saveComponentImage(JFrame frame, File file) throws Exception {
        frame.pack();
        frame.setSize(1100, 750);
        Container pane = frame.getContentPane();
        pane.setSize(1100, 750);
        pane.validate();
        pane.doLayout();

        BufferedImage image = new BufferedImage(1100, 750, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 1100, 750);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        pane.printAll(g2);
        g2.dispose();

        ImageIO.write(image, "png", file);
    }

    private static void copyFile(File src, File dest) {
        try {
            java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Could not copy screenshot to artifact dir: " + e.getMessage());
        }
    }
}
