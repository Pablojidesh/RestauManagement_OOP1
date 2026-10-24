package com.restaurant.model;

/**
 * ProfitLossReport
 * 
 * Model class encapsulating aggregated financial metrics for Admin reporting:
 * - Total Revenue (sum of completed bills)
 * - Total Salary Expense (sum of all employee salaries)
 * - Total Inventory Cost (sum of pantry valuation)
 * - Net Profit or Loss (Total Revenue - Total Salaries - Total Inventory Cost)
 */
public class ProfitLossReport {
    private double totalRevenue;
    private double totalSalaryExpense;
    private double totalInventoryCost;
    private double netAmount;
    private boolean isProfit;
    private int totalOrders;

    public ProfitLossReport(double totalRevenue, double totalSalaryExpense, double totalInventoryCost, int totalOrders) {
        this.totalRevenue = totalRevenue;
        this.totalSalaryExpense = totalSalaryExpense;
        this.totalInventoryCost = totalInventoryCost;
        this.totalOrders = totalOrders;
        this.netAmount = totalRevenue - totalSalaryExpense - totalInventoryCost;
        this.isProfit = (this.netAmount >= 0);
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalSalaryExpense() {
        return totalSalaryExpense;
    }

    public double getTotalInventoryCost() {
        return totalInventoryCost;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public boolean isProfit() {
        return isProfit;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public String getStatusLabel() {
        return isProfit ? "Net Profit" : "Net Loss";
    }
}
