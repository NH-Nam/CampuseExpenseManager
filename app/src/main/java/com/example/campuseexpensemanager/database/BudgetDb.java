package com.example.campuseexpensemanager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campuseexpensemanager.model.Budgets;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BudgetDb {
    private final SQLiteDatabase dbRead, dbWrite;
    private final List<OnDataChangeListener> listeners = new ArrayList<>();

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public BudgetDb(Context context) {
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
    }

    public void addOnDataChangeListener(OnDataChangeListener listener) {
        listeners.add(listener);
    }

    public void removeOnDataChangeListener(OnDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyDataChanged() {
        for (OnDataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    public void addBudgetCategory(Budgets budgetCategory) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, budgetCategory.getName());
        values.put(DatabaseContext.MONEY_BUDGET, budgetCategory.getMoney());
        values.put(DatabaseContext.DESCRIPTION_BUDGET, budgetCategory.getDescription());
        values.put(DatabaseContext.CATEGORY_BUDGET, budgetCategory.getCategory());
        values.put(DatabaseContext.SPENT_AMOUNT, budgetCategory.getSpentAmount());
        values.put(DatabaseContext.CREATED_AT, getCurrentDateTime());

        long result = dbWrite.insert(DatabaseContext.TABLE_NAME_BUDGET, null, values);
        if (result == -1) {
            android.util.Log.e("BudgetDb", "Failed to add budget category");
        } else {
            android.util.Log.d("BudgetDb", "Budget category added successfully with ID: " + result);
            notifyDataChanged();
        }
    }

    public void updateBudgetCategory(Budgets budgetCategory) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, budgetCategory.getName());
        values.put(DatabaseContext.MONEY_BUDGET, budgetCategory.getMoney());
        values.put(DatabaseContext.DESCRIPTION_BUDGET, budgetCategory.getDescription());
        values.put(DatabaseContext.CATEGORY_BUDGET, budgetCategory.getCategory());
        values.put(DatabaseContext.SPENT_AMOUNT, budgetCategory.getSpentAmount());
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        int result = dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, 
            DatabaseContext.ID_BUDGET + " = ?", 
            new String[]{String.valueOf(budgetCategory.getId())});
        
        if (result == 0) {
            android.util.Log.e("BudgetDb", "Failed to update budget category");
        } else {
            android.util.Log.d("BudgetDb", "Budget category updated successfully");
            notifyDataChanged();
        }
    }

    public int updateSpentAmount(long id, double spentAmount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.SPENT_AMOUNT, spentAmount);
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, selection, selectionArgs);
    }

    public boolean hasExpensesInCategory(String category) {
        String query = "SELECT COUNT(*) FROM " + DatabaseContext.TABLE_NAME_EXPENSE +
                " WHERE " + DatabaseContext.CATEGORY_EXPENSE + " = ? AND " + 
                DatabaseContext.DELETED_AT + " IS NULL";
        Cursor cursor = dbRead.rawQuery(query, new String[]{category});

        boolean hasExpenses = false;
        if (cursor.moveToFirst()) {
            hasExpenses = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasExpenses;
    }

    public int deleteBudgetCategory(long id) {
        // First get the category name
        String category = getCategoryNameById(id);
        if (category == null) {
            return 0;
        }

        // Check if there are any expenses in this category
        if (hasExpensesInCategory(category)) {
            return -1; // Return -1 to indicate there are expenses
        }

        // If no expenses, proceed with deletion
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.DELETED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int result = dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, selection, selectionArgs);
        if (result > 0) {
            notifyDataChanged();
        }
        return result;
    }

    private String getCategoryNameById(long id) {
        String[] projection = {DatabaseContext.CATEGORY_BUDGET};
        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = dbRead.query(
                DatabaseContext.TABLE_NAME_BUDGET,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String category = null;
        if (cursor.moveToFirst()) {
            category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_BUDGET));
        }
        cursor.close();
        return category;
    }


    public List<Budgets> getBudgetCategoriesByMonth(String monthYear) {
        List<Budgets> budgetCategories = new ArrayList<>();
        String[] projection = {
                DatabaseContext.ID_BUDGET,
                DatabaseContext.NAME_BUDGET,
                DatabaseContext.MONEY_BUDGET,
                DatabaseContext.CATEGORY_BUDGET,
                DatabaseContext.SPENT_AMOUNT,
                DatabaseContext.CREATED_AT
        };

        String selection = DatabaseContext.DELETED_AT + " IS NULL";
        String sortOrder = DatabaseContext.CATEGORY_BUDGET + " ASC";
        
        Cursor cursor = dbRead.query(
                DatabaseContext.TABLE_NAME_BUDGET,
                projection,
                selection,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Budgets budgetCategory = new Budgets();
                budgetCategory.setId((int) cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContext.ID_BUDGET)));
                budgetCategory.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_BUDGET)));
                budgetCategory.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_BUDGET)));
                budgetCategory.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_BUDGET)));
                budgetCategory.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.SPENT_AMOUNT)));
                budgetCategory.setMonthYear(monthYear);
                budgetCategory.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
                
                budgetCategories.add(budgetCategory);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return budgetCategories;
    }

    public Budgets getBudgetCategoryById(long id) {
        String[] projection = {
                DatabaseContext.ID_BUDGET,
                DatabaseContext.NAME_BUDGET,
                DatabaseContext.MONEY_BUDGET,
                DatabaseContext.CATEGORY_BUDGET,
                DatabaseContext.SPENT_AMOUNT,
                DatabaseContext.CREATED_AT
        };

        String selection = DatabaseContext.ID_BUDGET + " = ? AND " + DatabaseContext.DELETED_AT + " IS NULL";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = dbRead.query(
                DatabaseContext.TABLE_NAME_BUDGET,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Budgets budgetCategory = null;
        if (cursor != null && cursor.moveToFirst()) {
            budgetCategory = new Budgets();
            budgetCategory.setId((int) cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContext.ID_BUDGET)));
            budgetCategory.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_BUDGET)));
            budgetCategory.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_BUDGET)));
            budgetCategory.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_BUDGET)));
            budgetCategory.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.SPENT_AMOUNT)));
            budgetCategory.setMonthYear(getCurrentMonthYear());
            budgetCategory.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
            
            cursor.close();
        }

        return budgetCategory;
    }

    public void close() {
        if (dbRead != null && dbRead.isOpen()) {
            dbRead.close();
        }
        if (dbWrite != null && dbWrite.isOpen()) {
            dbWrite.close();
        }
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private String getCurrentDateTime() {
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf.format(zoneDt);
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private String getCurrentMonthYear() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        return dtf.format(zoneDt);
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    public String getCurrentMonth() {
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
        return dtf.format(zoneDt);
    }

    public boolean hasBudgetForCategory(String categoryName) {
        String[] columns = {DatabaseContext.ID_BUDGET};
        String selection = DatabaseContext.CATEGORY_BUDGET + " = ? AND " + DatabaseContext.DELETED_AT + " IS NULL";
        String[] selectionArgs = {categoryName};
        
        Cursor cursor = dbRead.query(DatabaseContext.TABLE_NAME_BUDGET, columns, selection, selectionArgs, null, null, null);
        boolean hasBudget = cursor.getCount() > 0;
        cursor.close();
        
        return hasBudget;
    }
}