package com.example.campuseexpensemanager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BudgetDb {
    private final SQLiteDatabase dbRead, dbWrite;

    public BudgetDb(Context context) {
        DatabaseContext helper = new DatabaseContext(context);
        dbRead = helper.getReadableDatabase();
        dbWrite = helper.getWritableDatabase();
    }

    public long setBudget(String name, double amount, String description, String category) {
        String currentDate = getCurrentDateTime();
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, name);
        values.put(DatabaseContext.MONEY_BUDGET, amount);
        values.put(DatabaseContext.DESCRIPTION_BUDGET, description);
        values.put(DatabaseContext.CATEGORY_BUDGET, category);
        values.put(DatabaseContext.CREATED_AT, currentDate);

        return dbWrite.insert(DatabaseContext.TABLE_NAME_BUDGET, null, values);
    }

    public int editBudget(long id, String name, double amount, String description, String category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.NAME_BUDGET, name);
        values.put(DatabaseContext.MONEY_BUDGET, amount);
        values.put(DatabaseContext.DESCRIPTION_BUDGET, description);
        values.put(DatabaseContext.CATEGORY_BUDGET, category);

        String selection = DatabaseContext.ID_BUDGET + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        return dbWrite.update(DatabaseContext.TABLE_NAME_BUDGET, values, selection, selectionArgs);
    }

    @SuppressLint({"NewApi", "LocalSuppress"})
    private String getCurrentDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now(ZoneId.systemDefault());
        return dtf.format(zoneDt);
    }
}