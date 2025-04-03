package com.example.campuseexpensemanager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExpenseDb {
    private final SQLiteDatabase dbRead, dbWrite;
    public ExpenseDb(@Nullable Context context){
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
        }
    // truy van lam viec voi bang expenses

    public long addExpense(String name, double amount, String description, String category) {
        String currentDate = getCurrentDateTime();
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_EXPENSE, name);
        values.put(DatabaseContext.MONEY_EXPENSE, amount);
        values.put(DatabaseContext.DESCRIPTION_EXPENSE, description);
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);
        values.put(DatabaseContext.CREATED_AT, currentDate);

        return dbWrite.insert(DatabaseContext.TABLE_NAME_EXPENSE, null, values);
    }

    public int editExpense(long id, String name, double amount, String description, String category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_EXPENSE, name);
        values.put(DatabaseContext.MONEY_EXPENSE, amount);
        values.put(DatabaseContext.DESCRIPTION_EXPENSE, description);
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);

        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_EXPENSE, values, selection, selectionArgs);
    }

    public int categorizeExpense(long id, String category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.CATEGORY_EXPENSE, category);

        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_EXPENSE, values, selection, selectionArgs);
    }

    public int deleteExpense(long id) {
        String selection = DatabaseContext.ID_EXPENSE + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.delete(DatabaseContext.TABLE_NAME_EXPENSE, selection, selectionArgs);
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private String getCurrentDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        return dtf.format(zoneDt);
    }
}
