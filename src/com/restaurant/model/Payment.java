package com.restaurant.model;

import java.util.Date;

/**
 * Payment
 * 
 * Abstract base class demonstrating OOP ABSTRACTION and INHERITANCE.
 * Defines protected state shared across all payment modalities (amount, date, order link,
 * and method type) and establishes the contract for DYNAMIC POLYMORPHISM via the abstract
 * method processPayment().
 * 
 * Concrete subclasses (CashPayment, CardPayment, UPIPayment) must provide their own
 * specialized processing logic and distinct transaction audit confirmation text.
 */
public abstract class Payment {
    protected int id;
    protected int orderId;
    protected double amount;
    protected Date paymentDate;
    protected String paymentMethod;
    protected String confirmationMessage;

    public Payment() {
        this.paymentDate = new Date();
    }

    public Payment(int orderId, double amount, String paymentMethod) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = new Date();
    }

    /**
     * Abstract method defining the core polymorphic contract.
     * Overridden by concrete subclasses to perform modality-specific verification,
     * calculate payment balances, and return distinct confirmation receipts.
     *
     * @return Formatted transaction confirmation message
     */
    public abstract String processPayment();

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}
