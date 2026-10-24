package com.restaurant.model;

import java.util.Date;
import java.util.List;

/**
 * Receipt
 * 
 * Model class encapsulating full customer bill breakdown and payment receipt details:
 * Order ID, Table Number, Customer Name, Timestamp, Line Items, Subtotal,
 * Discount %, Tax %, Grand Total, Payment Type, and Payment Confirmation Text.
 */
public class Receipt {
    private int orderId;
    private String tableNumber;
    private String customerName;
    private Date billDate;
    private List<OrderItem> items;
    private double subtotal;
    private double discountPercent;
    private double discountAmount;
    private double taxPercent;
    private double taxAmount;
    private double grandTotal;
    private String paymentMethod;
    private String paymentConfirmation;

    public Receipt(int orderId, String tableNumber, String customerName, Date billDate,
                   List<OrderItem> items, double subtotal, double discountPercent,
                   double discountAmount, double taxPercent, double taxAmount,
                   double grandTotal, String paymentMethod, String paymentConfirmation) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.customerName = customerName;
        this.billDate = billDate;
        this.items = items;
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.paymentConfirmation = paymentConfirmation;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Date getBillDate() {
        return billDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentConfirmation() {
        return paymentConfirmation;
    }

    /**
     * Produces a clean text receipt formatted with ASCII tables and currency.
     */
    public String getFormattedReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("              THE ROYAL SPICE RESTAURANT             \n");
        sb.append("                  GSTIN: 27AABCT1234D1Z2             \n");
        sb.append("=====================================================\n");
        sb.append(String.format("Order ID:    #%-10d Table:    %s%n", orderId, tableNumber));
        sb.append(String.format("Customer:    %-10s Date:     %s%n", customerName, billDate));
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("%-22s %-6s %-10s %-10s%n", "Item", "Qty", "Price", "Total"));
        sb.append("-----------------------------------------------------\n");

        if (items != null) {
            for (OrderItem item : items) {
                String name = item.getMenuItemName();
                if (name.length() > 20) name = name.substring(0, 19) + ".";
                sb.append(String.format("%-22s %-6d Rs.%-7.2f Rs.%-7.2f%n",
                    name, item.getQuantity(), item.getPrice(), item.getSubtotal()));
            }
        }

        sb.append("-----------------------------------------------------\n");
        sb.append(String.format("Subtotal:                            Rs.%.2f%n", subtotal));
        sb.append(String.format("Discount (%.1f%%):                   -Rs.%.2f%n", discountPercent, discountAmount));
        sb.append(String.format("Tax / GST (%.1f%%):                  +Rs.%.2f%n", taxPercent, taxAmount));
        sb.append("=====================================================\n");
        sb.append(String.format("GRAND TOTAL:                         Rs.%.2f%n", grandTotal));
        sb.append("=====================================================\n\n");
        sb.append("PAYMENT DETAILS:\n");
        sb.append(paymentConfirmation).append("\n");
        sb.append("-----------------------------------------------------\n");
        sb.append("       Thank you for dining with us! Visit again!    \n");
        sb.append("=====================================================\n");
        return sb.toString();
    }
}
