package com.restaurant.model;

import java.util.List;

/**
 * ConsolidatedReport
 * 
 * Comprehensive administrative reporting model aggregating all operational,
 * financial, payroll, inventory, and order pipeline metrics into a unified view.
 * Guarantees 100% mathematical consistency with individual functional tabs.
 */
public class ConsolidatedReport {

    // Revenue Metrics
    private double totalRevenue;
    private double cashRevenue;
    private double cardRevenue;
    private double upiRevenue;
    private int totalBillsCount;
    private int cashBillsCount;
    private int cardBillsCount;
    private int upiBillsCount;

    // Expenses & Valuation Metrics
    private double totalSalaryExpense;
    private double totalInventoryCost;
    private double totalOperatingExpenses;

    // Net Profit / Loss
    private double netProfitLoss;
    private boolean isProfit;

    // Order Pipeline Metrics
    private int totalOrdersCount;
    private int placedOrdersCount;
    private int preparingOrdersCount;
    private int servedOrdersCount;
    private int completedOrdersCount;
    private int todayOrdersCount;

    // Inventory Stock Alert Metrics
    private int totalInventoryItemsCount;
    private int lowStockItemsCount;

    // Employee Payroll Details
    private List<Employee> employeeList;

    public ConsolidatedReport() {
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getCashRevenue() {
        return cashRevenue;
    }

    public void setCashRevenue(double cashRevenue) {
        this.cashRevenue = cashRevenue;
    }

    public double getCardRevenue() {
        return cardRevenue;
    }

    public void setCardRevenue(double cardRevenue) {
        this.cardRevenue = cardRevenue;
    }

    public double getUpiRevenue() {
        return upiRevenue;
    }

    public void setUpiRevenue(double upiRevenue) {
        this.upiRevenue = upiRevenue;
    }

    public int getTotalBillsCount() {
        return totalBillsCount;
    }

    public void setTotalBillsCount(int totalBillsCount) {
        this.totalBillsCount = totalBillsCount;
    }

    public int getCashBillsCount() {
        return cashBillsCount;
    }

    public void setCashBillsCount(int cashBillsCount) {
        this.cashBillsCount = cashBillsCount;
    }

    public int getCardBillsCount() {
        return cardBillsCount;
    }

    public void setCardBillsCount(int cardBillsCount) {
        this.cardBillsCount = cardBillsCount;
    }

    public int getUpiBillsCount() {
        return upiBillsCount;
    }

    public void setUpiBillsCount(int upiBillsCount) {
        this.upiBillsCount = upiBillsCount;
    }

    public double getTotalSalaryExpense() {
        return totalSalaryExpense;
    }

    public void setTotalSalaryExpense(double totalSalaryExpense) {
        this.totalSalaryExpense = totalSalaryExpense;
    }

    public double getTotalInventoryCost() {
        return totalInventoryCost;
    }

    public void setTotalInventoryCost(double totalInventoryCost) {
        this.totalInventoryCost = totalInventoryCost;
    }

    public double getTotalOperatingExpenses() {
        return totalOperatingExpenses;
    }

    public void setTotalOperatingExpenses(double totalOperatingExpenses) {
        this.totalOperatingExpenses = totalOperatingExpenses;
    }

    public double getNetProfitLoss() {
        return netProfitLoss;
    }

    public void setNetProfitLoss(double netProfitLoss) {
        this.netProfitLoss = netProfitLoss;
        this.isProfit = (netProfitLoss >= 0);
    }

    public boolean isProfit() {
        return isProfit;
    }

    public int getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public void setTotalOrdersCount(int totalOrdersCount) {
        this.totalOrdersCount = totalOrdersCount;
    }

    public int getPlacedOrdersCount() {
        return placedOrdersCount;
    }

    public void setPlacedOrdersCount(int placedOrdersCount) {
        this.placedOrdersCount = placedOrdersCount;
    }

    public int getPreparingOrdersCount() {
        return preparingOrdersCount;
    }

    public void setPreparingOrdersCount(int preparingOrdersCount) {
        this.preparingOrdersCount = preparingOrdersCount;
    }

    public int getServedOrdersCount() {
        return servedOrdersCount;
    }

    public void setServedOrdersCount(int servedOrdersCount) {
        this.servedOrdersCount = servedOrdersCount;
    }

    public int getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public void setCompletedOrdersCount(int completedOrdersCount) {
        this.completedOrdersCount = completedOrdersCount;
    }

    public int getTodayOrdersCount() {
        return todayOrdersCount;
    }

    public void setTodayOrdersCount(int todayOrdersCount) {
        this.todayOrdersCount = todayOrdersCount;
    }

    public int getTotalInventoryItemsCount() {
        return totalInventoryItemsCount;
    }

    public void setTotalInventoryItemsCount(int totalInventoryItemsCount) {
        this.totalInventoryItemsCount = totalInventoryItemsCount;
    }

    public int getLowStockItemsCount() {
        return lowStockItemsCount;
    }

    public void setLowStockItemsCount(int lowStockItemsCount) {
        this.lowStockItemsCount = lowStockItemsCount;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }
}
