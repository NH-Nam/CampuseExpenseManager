package com.example.asm_app_se06304.model;

public class Transaction {
    private long id;
    private String type; // "Budget" hoặc "Expense"
    private String description;
    private double amount;
    private String date;
    private int categoryId; // Chỉ áp dụng cho Expense, -1 cho Budget

    public Transaction(long id, String type, String description, double amount, String date, int categoryId) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
    }

    public long getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public int getCategoryId() { return categoryId; }
}