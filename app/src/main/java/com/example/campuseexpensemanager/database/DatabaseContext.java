package com.example.campuseexpensemanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseContext extends SQLiteOpenHelper {
    private static final String DB_NAME = "campus_expenses";
    private static final int DB_VERSION = 5; // Increment the database version to 5

    // User table
    public static final String TABLE_NAME = "users";
    public static final String ID_COL = "id";
    public static final String USERNAME_COL = "username";
    public static final String PASSWORD_COL = "password";
    public static final String EMAIL_COL = "email";
    public static final String PHONE_COL = "phone";
    public static final String ROLE_ID_COL = "role_id";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String DELETED_AT = "deleted_at";

    // Budget table
    public static final String ID_BUDGET = "id";
    public static final String NAME_BUDGET = "name"; // Optional
    public static final String MONEY_BUDGET = "money";
    public static final String DESCRIPTION_BUDGET = "description"; // Optional
    public static final String TABLE_NAME_BUDGET = "budgets";
    public static final String CATEGORY_BUDGET = "category"; // New: Category for budget

    // Expense table
    public static final String ID_EXPENSE = "id";
    public static final String NAME_EXPENSE = "name";
    public static final String MONEY_EXPENSE = "money";
    public static final String DESCRIPTION_EXPENSE = "description";
    public static final String CATEGORY_EXPENSE = "category"; // Add the category column
    public static final String TABLE_NAME_EXPENSE = "expenses";

    public DatabaseContext(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table
        String tableUser = "CREATE TABLE " + TABLE_NAME + " ( "
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME_COL + " VARCHAR(100) NOT NULL, "
                + PASSWORD_COL + " VARCHAR(100) NOT NULL, "
                + EMAIL_COL + " VARCHAR(200), "
                + PHONE_COL + " VARCHAR(20), "
                + ROLE_ID_COL + " INTEGER NOT NULL, "
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(tableUser);

        // Create the budgets table
        String tableBudget = "CREATE TABLE " + TABLE_NAME_BUDGET + " ( "
                + ID_BUDGET + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_BUDGET + " VARCHAR(100), " // Optional
                + MONEY_BUDGET + " REAL NOT NULL, "
                + DESCRIPTION_BUDGET + " VARCHAR(200), " // Optional
                + CATEGORY_BUDGET + " VARCHAR(100) NOT NULL, " // Add category here
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(tableBudget);

        // Create the expenses table
        String tableExpense = "CREATE TABLE " + TABLE_NAME_EXPENSE + " ( "
                + ID_EXPENSE + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_EXPENSE + " VARCHAR(100) NOT NULL, "
                + MONEY_EXPENSE + " REAL NOT NULL, "
                + DESCRIPTION_EXPENSE + " VARCHAR(200), "
                + CATEGORY_EXPENSE + " VARCHAR(100), " // Add category column to table creation
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(tableExpense);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_BUDGET + " ADD COLUMN " + CATEGORY_BUDGET + " VARCHAR(100)");
        }


        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BUDGET);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EXPENSE);
            onCreate(db);
        }
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}