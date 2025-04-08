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

    public int deleteBudgetCategory(long id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.DELETED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, selection, selectionArgs);
    }

    public List<Budgets> getAllBudgetCategories() {
        List<Budgets> budgetCategories = new ArrayList<>();
        Cursor cursor = dbRead.query(DatabaseContext.TABLE_NAME_BUDGET, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Budgets budgetCategory = new Budgets();
                budgetCategory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContext.ID_BUDGET)));
                budgetCategory.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_BUDGET)));
                budgetCategory.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_BUDGET)));
                budgetCategory.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DESCRIPTION_BUDGET)));
                budgetCategory.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_BUDGET)));
                budgetCategory.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.SPENT_AMOUNT)));
                budgetCategories.add(budgetCategory);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return budgetCategories;
    }

    public double calculateSpentAmount(String category, String monthYear) {
        String query = "SELECT SUM(" + DatabaseContext.MONEY_EXPENSE + ") FROM " + DatabaseContext.TABLE_NAME_EXPENSE +
                " WHERE " + DatabaseContext.CATEGORY_EXPENSE + " = ? AND " + DatabaseContext.CREATED_AT + " LIKE ?";
        Cursor cursor = dbRead.rawQuery(query, new String[]{category, monthYear + "%"});

        double spentAmount = 0;
        if (cursor.moveToFirst()) {
            spentAmount = cursor.getDouble(0);
        }
        cursor.close();
        return spentAmount;
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
}