package com.example.campuseexpensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDb {
    private SQLiteDatabase database;
    private DatabaseContext dbHelper;

    public CategoryDb(Context context) {
        dbHelper = new DatabaseContext(context);
        database = dbHelper.getWritableDatabase();
    }

    public long addCategory(Category category) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContext.NAME_CATEGORY, category.getName());
            values.put(DatabaseContext.IS_CUSTOM, category.isCustom() ? 1 : 0);
            values.put(DatabaseContext.ICON_NAME, "category_24dp");
            values.put(DatabaseContext.CREATED_AT, getCurrentDateTime());
            long result = database.insert(DatabaseContext.TABLE_NAME_CATEGORY, null, values);
            android.util.Log.d("CategoryDb", "Add category result: " + result);
            return result;
        } catch (Exception e) {
            android.util.Log.e("CategoryDb", "Error adding category: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = database.query("categories", null, null, null, null, null, "name ASC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                boolean isCustom = cursor.getInt(cursor.getColumnIndexOrThrow("is_custom")) == 1;
                categories.add(new Category(id, name, isCustom));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    public int updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put("name", category.getName());
        values.put("is_custom", category.isCustom() ? 1 : 0);
        return database.update("categories", values, "id = ?", new String[]{String.valueOf(category.getId())});
    }

    public int updateCategoryAndPropagate(Category category) {
        // Start a transaction
        database.beginTransaction();
        try {
            // Get the old category name before updating
            Cursor cursor = database.query("categories", 
                new String[]{"name"}, 
                "id = ?", 
                new String[]{String.valueOf(category.getId())}, 
                null, null, null);
            
            String oldCategoryName = null;
            if (cursor.moveToFirst()) {
                oldCategoryName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
            cursor.close();

            if (oldCategoryName == null) {
                return 0;
            }

            // Update the category
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("is_custom", category.isCustom() ? 1 : 0);
            values.put("updated_at", getCurrentDateTime());
            
            int result = database.update("categories", 
                values, 
                "id = ?", 
                new String[]{String.valueOf(category.getId())});

            if (result > 0) {
                // Update budgets
                ContentValues budgetValues = new ContentValues();
                budgetValues.put(DatabaseContext.CATEGORY_BUDGET, category.getName());
                budgetValues.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());
                database.update(DatabaseContext.TABLE_NAME_BUDGET,
                    budgetValues,
                    DatabaseContext.CATEGORY_BUDGET + " = ?",
                    new String[]{oldCategoryName});

                // Update expenses
                ContentValues expenseValues = new ContentValues();
                expenseValues.put(DatabaseContext.CATEGORY_EXPENSE, category.getName());
                expenseValues.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());
                database.update(DatabaseContext.TABLE_NAME_EXPENSE,
                    expenseValues,
                    DatabaseContext.CATEGORY_EXPENSE + " = ?",
                    new String[]{oldCategoryName});

                // Update budget categories
                ContentValues budgetCategoryValues = new ContentValues();
                budgetCategoryValues.put(DatabaseContext.CATEGORY_NAME, category.getName());
                budgetCategoryValues.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());
                database.update(DatabaseContext.TABLE_NAME_BUDGET_CATEGORY,
                    budgetCategoryValues,
                    DatabaseContext.CATEGORY_NAME + " = ?",
                    new String[]{oldCategoryName});
            }

            // Commit the transaction
            database.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            android.util.Log.e("CategoryDb", "Error updating category: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            // End the transaction
            database.endTransaction();
        }
    }

    public int deleteCategory(int id) {
        return database.delete("categories", "id = ?", new String[]{String.valueOf(id)});
    }

    public void close() {
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 