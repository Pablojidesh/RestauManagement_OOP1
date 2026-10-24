package com.restaurant.model;

import java.sql.Timestamp;

/**
 * Bill
 * 
 * Model class representing the finalized billing invoice for an order.
 * Stores calculated subtotal, discounts, taxes, grand total, payment method,
 * transaction reference, and timestamp.
 */
public class Bill {
    private int id;
    private int orderId;
    private double subtotal;
    private double discountPercent;
    private double taxPercent;
    private double grandTotal;
    private String paymentMethod;
    private String paymentReference;
    private Timestamp createdAt;

    public Bill() {
    }

    public Bill(int id, int orderId, double subtotal, double discountPercent, double taxPercent,
                double grandTotal, String paymentMethod, String paymentReference, Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.taxPercent = taxPercent;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt;
    }

    public Bill(int orderId, double subtotal, double discountPercent, double taxPercent,
                double grandTotal, String paymentMethod, String paymentReference) {
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.taxPercent = taxPercent;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(double taxPercent) {
        this.taxPercent = taxPercent;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Bill #" + id + " (Order #" + orderId + ", Grand Total: Rs." + String.format("%.2f", grandTotal) + ")";
    }
}
