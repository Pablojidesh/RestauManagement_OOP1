package com.restaurant.model;

/**
 * CashPayment
 * 
 * Concrete subclass extending Payment.
 * Demonstrates INHERITANCE and DYNAMIC POLYMORPHISM.
 * Implements physical currency handling by verifying tendered cash against total bill amount,
 * computing remaining change to be returned to the diner, and producing cash receipt details.
 */
public class CashPayment extends Payment {
    private double tenderedAmount;
    private double changeAmount;

    public CashPayment() {
        super();
        this.paymentMethod = "CASH";
    }

    public CashPayment(int orderId, double amount, double tenderedAmount) {
        super(orderId, amount, "CASH");
        this.tenderedAmount = tenderedAmount;
    }

    @Override
    public String processPayment() {
        if (tenderedAmount < amount) {
            double shortage = amount - tenderedAmount;
            throw new IllegalArgumentException("Insufficient cash tendered! Short by Rs." + String.format("%.2f", shortage));
        }
        this.changeAmount = tenderedAmount - amount;
        this.confirmationMessage = String.format(
            "CASH PAYMENT PROCESSED SUCCESSFULLY%n" +
            "----------------------------------------%n" +
            "Bill Amount:     Rs.%.2f%n" +
            "Cash Tendered:   Rs.%.2f%n" +
            "Change Returned: Rs.%.2f%n" +
            "Status:          Collected by Cashier in Drawer",
            amount, tenderedAmount, changeAmount
        );
        return this.confirmationMessage;
    }

    public double getTenderedAmount() {
        return tenderedAmount;
    }

    public void setTenderedAmount(double tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
    }
}
