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

    public BudgetDb(Context context) {
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
    }

    public long addBudgetCategory(Budgets budgetCategory) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, budgetCategory.getName());
        values.put(DatabaseContext.MONEY_BUDGET, budgetCategory.getMoney());
        values.put(DatabaseContext.CATEGORY_BUDGET, budgetCategory.getCategory());
        values.put(DatabaseContext.SPENT_AMOUNT, budgetCategory.getSpentAmount());
        values.put(DatabaseContext.CREATED_AT, getCurrentDateTime());

        return dbWrite.insert(DatabaseContext.TABLE_NAME_BUDGET, null, values);
    }

    public int updateBudgetCategory(Budgets budgetCategory) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, budgetCategory.getName());
        values.put(DatabaseContext.MONEY_BUDGET, budgetCategory.getMoney());
        values.put(DatabaseContext.CATEGORY_BUDGET, budgetCategory.getCategory());
        values.put(DatabaseContext.SPENT_AMOUNT, budgetCategory.getSpentAmount());
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(budgetCategory.getId())};

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
                budgetCategory.setMonthYear(getCurrentMonthYear());
                budgetCategory.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
                
                budgetCategories.add(budgetCategory);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return budgetCategories;
    }

    private double calculateSpentAmountForCategory(String category) {
        if (category == null || category.isEmpty()) {
            return 0.0;
        }
        
        double totalSpent = 0.0;
        
        // Query expenses for this category
        String[] projection = {DatabaseContext.MONEY_EXPENSE};
        String selection = DatabaseContext.CATEGORY_EXPENSE + " = ? AND " + DatabaseContext.DELETED_AT + " IS NULL";
        String[] selectionArgs = {category};
        
        Cursor cursor = dbRead.query(
                DatabaseContext.TABLE_NAME_EXPENSE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalSpent += cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_EXPENSE));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return totalSpent;
    }

    public List<Budgets> getBudgetCategoriesByMonth(String monthYear) {
        // Since the budgets table doesn't have a month field, we'll return all budgets
        // and filter them in the fragment
        List<Budgets> allBudgets = getAllBudgetCategories();
        List<Budgets> filteredBudgets = new ArrayList<>();
        
        for (Budgets budget : allBudgets) {
            if (budget.getMonthYear() != null && budget.getMonthYear().equals(monthYear)) {
                filteredBudgets.add(budget);
            }
        }
        
        return filteredBudgets;
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

    public int updateSpentAmount(long id, double spentAmount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.SPENT_AMOUNT, spentAmount);
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_BUDGET + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, selection, selectionArgs);
    }

    public void close() {
        dbRead.close();
        dbWrite.close();
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