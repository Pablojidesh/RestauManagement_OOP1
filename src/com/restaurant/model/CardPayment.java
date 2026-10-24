package com.restaurant.model;

/**
 * CardPayment
 * 
 * Concrete subclass extending Payment.
 * Demonstrates INHERITANCE and DYNAMIC POLYMORPHISM.
 * Emulates POS terminal debit/credit card processing by masking PAN sensitive digits
 * (e.g. '**** **** **** 5678') for PCI compliance, capturing cardholder details,
 * and generating a unique terminal authorization code.
 */
public class CardPayment extends Payment {
    private String cardNumber;
    private String cardHolderName;
    private String cardType; // 'Visa', 'MasterCard', 'RuPay', 'Amex'
    private String authCode;

    public CardPayment() {
        super();
        this.paymentMethod = "CARD";
    }

    public CardPayment(int orderId, double amount, String cardNumber, String cardHolderName, String cardType) {
        super(orderId, amount, "CARD");
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardType = cardType != null ? cardType : "Debit/Credit Card";
    }

    @Override
    public String processPayment() {
        if (cardNumber == null || cardNumber.trim().length() < 4) {
            throw new IllegalArgumentException("Invalid card number provided! Must contain at least 4 digits.");
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required!");
        }

        // Clean digits
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        String last4 = cleaned.length() >= 4 ? cleaned.substring(cleaned.length() - 4) : "XXXX";
        String maskedCard = "**** **** **** " + last4;

        // Generate deterministic or pseudorandom authorization approval code
        int hash = Math.abs((cardNumber + cardHolderName + System.currentTimeMillis()).hashCode() % 900000) + 100000;
        this.authCode = "AUTH-" + hash;

        this.confirmationMessage = String.format(
            "CARD PAYMENT AUTHORIZED SUCCESSFULLY%n" +
            "----------------------------------------%n" +
            "Amount Charged:  Rs.%.2f%n" +
            "Card Number:     %s%n" +
            "Network / Type:  %s%n" +
            "Cardholder:      %s%n" +
            "Terminal Auth:   %s%n" +
            "Status:          Bank Transaction Approved",
            amount, maskedCard, cardType, cardHolderName.trim(), authCode
        );
        return this.confirmationMessage;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
