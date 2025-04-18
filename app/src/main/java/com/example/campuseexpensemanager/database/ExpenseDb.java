package com.example.campuseexpensemanager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.model.Budgets;
import com.example.campuseexpensemanager.model.Expenses;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDb {
    private final SQLiteDatabase dbRead, dbWrite;
    private BudgetDb budgetDb;
    private List<Runnable> dataChangeCallbacks;
    
    public ExpenseDb(@Nullable Context context){
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
        budgetDb = new BudgetDb(context);
        dataChangeCallbacks = new ArrayList<>();
    }

    public void addOnDataChangedCallback(Runnable callback) {
        if (!dataChangeCallbacks.contains(callback)) {
            dataChangeCallbacks.add(callback);
        }
    }

    public void removeOnDataChangedCallback(Runnable callback) {
        dataChangeCallbacks.remove(callback);
    }

    private void notifyDataChanged() {
        // Make sure we're on the main thread when notifying
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        for (Runnable callback : dataChangeCallbacks) {
            if (callback != null) {
                try {
                    mainHandler.post(callback);
                } catch (Exception e) {
                    // Log the error but continue with other callbacks
                    e.printStackTrace();
                }
            }
        }
    }

    public long addExpense(String name, double amount, String description, String category) {
        // First check if a budget exists for this category
        if (!budgetDb.hasBudgetForCategory(category)) {
            // Return -3 to indicate no budget exists for this category
            return -3;
        }

        // Get the budget for this category
        List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
        long budgetId = -1;
        for (Budgets budget : budgetCategories) {
            if (budget.getCategory().equals(category)) {
                double currentSpent = budget.getSpentAmount();
                double budgetLimit = budget.getMoney();
                if (currentSpent + amount > budgetLimit) {
                    // Return -2 to indicate budget limit exceeded
                    return -2;
                }
                budgetId = budget.getId();
                break;
            }
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_EXPENSE, name);
        values.put(DatabaseContext.MONEY_EXPENSE, amount);
        values.put(DatabaseContext.DESCRIPTION_EXPENSE, description);
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);
        values.put(DatabaseContext.BUDGET_ID, budgetId);
        values.put(DatabaseContext.CREATED_AT, getCurrentDateTime());

        long result = dbWrite.insert(DatabaseContext.TABLE_NAME_EXPENSE, null, values);

        if (result != -1) {
            // Update budget category spent amount
            updateBudgetCategorySpentAmount(category, amount);
            notifyDataChanged();
        }

        return result;
    }

    public int editExpense(long id, String name, double amount, String description, String category) {
        // Get the old expense to calculate the difference in amount
        Expenses oldExpense = getExpenseById(id);
        double oldAmount = oldExpense != null ? oldExpense.getMoney() : 0;
        String oldCategory = oldExpense != null ? oldExpense.getCategory() : null;
        
        // Get the new budget ID
        long budgetId = -1;
        if (category != null && !category.isEmpty()) {
            List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
            for (Budgets budget : budgetCategories) {
                if (budget.getCategory().equals(category)) {
                    budgetId = budget.getId();
                    break;
                }
            }
        }
        
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_EXPENSE, name);
        values.put(DatabaseContext.MONEY_EXPENSE, amount);
        values.put(DatabaseContext.DESCRIPTION_EXPENSE, description);
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);
        values.put(DatabaseContext.BUDGET_ID, budgetId);
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        int result = dbWrite.update(DatabaseContext.TABLE_NAME_EXPENSE, values, selection, selectionArgs);
        
        // Update budget category spent amounts
        if (result > 0) {
            // If category changed, update both old and new categories
            if (oldCategory != null && !oldCategory.equals(category)) {
                // Subtract from old category
                if (oldCategory != null && !oldCategory.isEmpty()) {
                    updateBudgetCategorySpentAmount(oldCategory, -oldAmount);
                }
                
                // Add to new category
                if (category != null && !category.isEmpty()) {
                    updateBudgetCategorySpentAmount(category, amount);
                }
            } else if (oldCategory != null && !oldCategory.isEmpty()) {
                // Same category, just update the difference
                double difference = amount - oldAmount;
                if (difference != 0) {
                    updateBudgetCategorySpentAmount(oldCategory, difference);
                }
            }
            // Notify all listeners about the data change
            notifyDataChanged();
        }
        
        return result;
    }

    public int categorizeExpense(long id, String category) {
        // Get the old expense to get the old category and amount
        Expenses oldExpense = getExpenseById(id);
        String oldCategory = oldExpense != null ? oldExpense.getCategory() : null;
        double amount = oldExpense != null ? oldExpense.getMoney() : 0;
        
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);
        values.put(DatabaseContext.UPDATED_AT, getCurrentDateTime());

        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        int result = dbWrite.update(DatabaseContext.TABLE_NAME_EXPENSE, values, selection, selectionArgs);
        
        // Update budget category spent amounts
        if (result > 0) {
            // Subtract from old category
            if (oldCategory != null && !oldCategory.isEmpty()) {
                updateBudgetCategorySpentAmount(oldCategory, -amount);
            }
            
            // Add to new category
            if (category != null && !category.isEmpty()) {
                updateBudgetCategorySpentAmount(category, amount);
            }
            notifyDataChanged();
        }
        
        return result;
    }

    public int deleteExpense(long id) {
        // Get the expense to get the category and amount
        Expenses expense = getExpenseById(id);
        String category = expense != null ? expense.getCategory() : null;
        double amount = expense != null ? expense.getMoney() : 0;
        
        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        int result = dbWrite.delete(DatabaseContext.TABLE_NAME_EXPENSE, selection, selectionArgs);
        
        // Update budget category spent amount
        if (result > 0 && category != null && !category.isEmpty()) {
            updateBudgetCategorySpentAmount(category, -amount);
            // Notify all listeners about the data change
            notifyDataChanged();
        }
        
        return result;
    }

    public List<Expenses> getAllExpenses() {
        List<Expenses> expensesList = new ArrayList<>();
        
        String[] projection = {
            DatabaseContext.ID_EXPENSE,
            DatabaseContext.NAME_EXPENSE,
            DatabaseContext.MONEY_EXPENSE,
            DatabaseContext.DESCRIPTION_EXPENSE,
            DatabaseContext.CATEGORY_EXPENSE,
            DatabaseContext.CREATED_AT,
            DatabaseContext.UPDATED_AT,
            DatabaseContext.DELETED_AT
        };
        
        String selection = DatabaseContext.DELETED_AT + " IS NULL";
        
        Cursor cursor = dbRead.query(
            DatabaseContext.TABLE_NAME_EXPENSE,
            projection,
            selection,
            null,
            null,
            null,
            DatabaseContext.CREATED_AT + " DESC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expenses expense = new Expenses();
                expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContext.ID_EXPENSE)));
                expense.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_EXPENSE)));
                expense.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_EXPENSE)));
                expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DESCRIPTION_EXPENSE)));
                expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_EXPENSE)));
                expense.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
                expense.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.UPDATED_AT)));
                expense.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DELETED_AT)));
                
                expensesList.add(expense);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        return expensesList;
    }
    
    public Expenses getExpenseById(long id) {
        String[] projection = {
            DatabaseContext.ID_EXPENSE,
            DatabaseContext.NAME_EXPENSE,
            DatabaseContext.MONEY_EXPENSE,
            DatabaseContext.DESCRIPTION_EXPENSE,
            DatabaseContext.CATEGORY_EXPENSE,
            DatabaseContext.CREATED_AT,
            DatabaseContext.UPDATED_AT,
            DatabaseContext.DELETED_AT
        };
        
        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = dbRead.query(
            DatabaseContext.TABLE_NAME_EXPENSE,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Expenses expense = null;
        if (cursor != null && cursor.moveToFirst()) {
            expense = new Expenses();
            expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContext.ID_EXPENSE)));
            expense.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_EXPENSE)));
            expense.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_EXPENSE)));
            expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DESCRIPTION_EXPENSE)));
            expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_EXPENSE)));
            expense.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
            expense.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.UPDATED_AT)));
            expense.setDeletedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DELETED_AT)));
            
            cursor.close();
        }
        
        return expense;
    }

    private void updateBudgetCategorySpentAmount(String category, double amount) {
        if (category == null || category.isEmpty()) {
            return;
        }

        try {
            // Get the current spent amount for this category
            List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
            if (budgetCategories == null) {
                return;
            }

            boolean categoryExists = false;
            for (Budgets budget : budgetCategories) {
                if (budget != null && budget.getCategory() != null && budget.getCategory().equals(category)) {
                    // Update the spent amount
                    double newSpentAmount = budget.getSpentAmount() + amount;
                    budget.setSpentAmount(newSpentAmount);
                    budgetDb.updateSpentAmount(budget.getId(), newSpentAmount);
                    categoryExists = true;
                    break;
                }
            }

            // If category doesn't exist in budget, create it
            if (!categoryExists) {
                Budgets newBudget = new Budgets();
                newBudget.setName(category);
                newBudget.setCategory(category);
                newBudget.setMoney(0); // Set initial budget to 0
                newBudget.setSpentAmount(amount);
                newBudget.setMonth(budgetDb.getCurrentMonth());
                budgetDb.addBudgetCategory(newBudget);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private String getCurrentDateTime() {
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf.format(zoneDt);
    }
    
    public void close() {
        if (budgetDb != null) {
            budgetDb.close();
        }
    }

    public List<Expenses> getExpensesByBudget(long budgetId) {
        List<Expenses> expensesList = new ArrayList<>();
        
        String[] projection = {
            DatabaseContext.ID_EXPENSE,
            DatabaseContext.NAME_EXPENSE,
            DatabaseContext.MONEY_EXPENSE,
            DatabaseContext.DESCRIPTION_EXPENSE,
            DatabaseContext.CATEGORY_EXPENSE,
            DatabaseContext.BUDGET_ID,
            DatabaseContext.CREATED_AT
        };

        String selection = DatabaseContext.BUDGET_ID + " = ? AND " + DatabaseContext.DELETED_AT + " IS NULL";
        String[] selectionArgs = {String.valueOf(budgetId)};
        String sortOrder = DatabaseContext.CREATED_AT + " DESC";

        Cursor cursor = dbRead.query(
            DatabaseContext.TABLE_NAME_EXPENSE,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expenses expense = new Expenses();
                expense.setId((int) cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContext.ID_EXPENSE)));
                expense.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.NAME_EXPENSE)));
                expense.setMoney(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContext.MONEY_EXPENSE)));
                expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.DESCRIPTION_EXPENSE)));
                expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CATEGORY_EXPENSE)));
                expense.setBudgetId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContext.BUDGET_ID)));
                expense.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContext.CREATED_AT)));
                
                expensesList.add(expense);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return expensesList;
    }
}
