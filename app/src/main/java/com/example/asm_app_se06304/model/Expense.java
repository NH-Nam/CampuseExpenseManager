package com.example.asm_app_se06304.model;

public class Expense {
    private long id;
    private String description;
    private double amount;
    private String date;
    private String categoryName;
    private int categoryId;

    public Expense(long id, String description, double amount, String date, String categoryName, int categoryId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }
}