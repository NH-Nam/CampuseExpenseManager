package com.example.campuseexpensemanager.model;

public enum Categories {
    FOOD("Food"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    EDUCATION("Education"),
    HEALTH("Health"),
    OTHER("Other");

    private final String displayName;

    Categories(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    public static Categories fromDisplayName(String displayName) {
        for (Categories category : values()) {
            if (category.getDisplayName().equals(displayName)) {
                return category;
            }
        }
        return OTHER; // Default to "Other" if not found
    }
}