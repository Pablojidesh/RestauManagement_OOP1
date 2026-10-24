package com.restaurant.model;

/**
 * UPIPayment
 * 
 * Concrete subclass extending Payment.
 * Demonstrates INHERITANCE and DYNAMIC POLYMORPHISM.
 * Emulates Unified Payments Interface (UPI / QR-code) instant settlement,
 * validating Virtual Payment Address (VPA / UPI ID) format and generating
 * an NPCI banking transaction reference number.
 */
public class
UPIPayment extends Payment {
    private String upiId;
    private String transactionRef;

    public UPIPayment() {
        super();
        this.paymentMethod = "UPI";
    }

    public UPIPayment(int orderId, double amount, String upiId) {
        super(orderId, amount, "UPI");
        this.upiId = upiId;
    }

    @Override
    public String processPayment() {
        if (upiId == null || !upiId.contains("@") || upiId.trim().length() < 3) {
            throw new IllegalArgumentException("Invalid UPI ID! Must follow format username@bank (e.g. diner@okhdfcbank).");
        }

        // Generate instant settlement banking reference number
        long timestampSuffix = System.currentTimeMillis() % 100000000L;
        this.transactionRef = "UPI-REF-" + timestampSuffix;

        this.confirmationMessage = String.format(
            "UPI PAYMENT COMPLETED SUCCESSFULLY%n" +
            "----------------------------------------%n" +
            "Amount Transferred: Rs.%.2f%n" +
            "UPI VPA ID:         %s%n" +
            "Bank Ref Number:    %s%n" +
            "Status:             Instant Settlement Verified via UPI Switch",
            amount, upiId.trim(), transactionRef
        );
        return this.confirmationMessage;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
}
