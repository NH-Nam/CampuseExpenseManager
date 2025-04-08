package com.example.campuseexpensemanager.model;

public class Category {
    private int id;
    private String name;
    private boolean isCustom;

    public Category(int id, String name, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.isCustom = isCustom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    @Override
    public String toString() {
        return name;
    }
} 