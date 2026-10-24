package com.restaurant.ui;

import com.restaurant.model.Receipt;
import com.restaurant.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ReceiptDialog
 * 
 * Modal Swing dialog presenting the itemized customer bill and the
 * polymorphic payment confirmation details upon checkout completion.
 */
public class ReceiptDialog extends JDialog {

    public ReceiptDialog(Frame parent, Receipt receipt) {
        super(parent, "Transaction Receipt - Order #" + receipt.getOrderId(), true);
        setSize(520, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.add(new JLabel(new UIUtils.RoyalSpiceLogoIcon(28)));

        JLabel titleLabel = new JLabel("Checkout Receipt - Order #" + receipt.getOrderId());
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.COLOR_CREAM);
        brandRow.add(titleLabel);

        JLabel subLabel = new JLabel("Payment Modality: " + receipt.getPaymentMethod() + " | Table: " + receipt.getTableNumber());
        subLabel.setFont(UIUtils.FONT_REGULAR);
        subLabel.setForeground(new Color(254, 243, 199));

        headerPanel.add(brandRow, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Receipt Content in Monospaced Text Area
        JTextArea receiptArea = new JTextArea(receipt.getFormattedReceiptText());
        receiptArea.setFont(UIUtils.FONT_MONO);
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(254, 254, 255));
        receiptArea.setBorder(new EmptyBorder(12, 16, 12, 16));
        receiptArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        bottomPanel.setBackground(UIUtils.COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER));

        JButton closeBtn = UIUtils.createButton("Close Receipt", UIUtils.COLOR_PRIMARY, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
