package com.example.campuseexpensemanager.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseContext extends SQLiteOpenHelper {
    private static final String DB_NAME = "campus_expenses";
    private static final int DB_VERSION = 10; // Increment version for new column

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
    public static final String TABLE_NAME_BUDGET = "budgets";
    public static final String ID_BUDGET = "id";
    public static final String CATEGORY_BUDGET = "category";
    public static final String MONTH = "month";
    public static final String SPENT_AMOUNT = "spent_amount";
    public static final String NAME_BUDGET = "name";
    public static final String DESCRIPTION_BUDGET = "description";
    public static final String MONEY_BUDGET = "money";

    // Expense table
    public static final String ID_EXPENSE = "id";
    public static final String NAME_EXPENSE = "name";
    public static final String MONEY_EXPENSE = "money";
    public static final String DESCRIPTION_EXPENSE = "description";
    public static final String CATEGORY_EXPENSE = "category";
    public static final String TABLE_NAME_EXPENSE = "expenses";
    public static final String BUDGET_ID = "budget_id"; // New column for budget relationship

    // Category table
    public static final String TABLE_NAME_CATEGORY = "categories";
    public static final String ID_CATEGORY = "id";
    public static final String NAME_CATEGORY = "name";
    public static final String IS_CUSTOM = "is_custom";
    public static final String ICON_NAME = "icon_name";

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
                + NAME_BUDGET + " VARCHAR(100), "
                + MONEY_BUDGET + " REAL NOT NULL, "
                + DESCRIPTION_BUDGET + " VARCHAR(200), "
                + CATEGORY_BUDGET + " VARCHAR(100) NOT NULL, "
                + SPENT_AMOUNT + " REAL DEFAULT 0, "
                + MONTH + " VARCHAR(7) NOT NULL, " // Format: YYYY-MM
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(tableBudget);

        // Create the expenses table with budget_id
        String tableExpense = "CREATE TABLE " + TABLE_NAME_EXPENSE + " ( "
                + ID_EXPENSE + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_EXPENSE + " VARCHAR(100) NOT NULL, "
                + MONEY_EXPENSE + " REAL NOT NULL, "
                + DESCRIPTION_EXPENSE + " VARCHAR(200), "
                + CATEGORY_EXPENSE + " VARCHAR(100), "
                + BUDGET_ID + " INTEGER, " // New column
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME, "
                + "FOREIGN KEY(" + BUDGET_ID + ") REFERENCES " + TABLE_NAME_BUDGET + "(" + ID_BUDGET + ")" // Foreign key constraint
                + " ) ";
        db.execSQL(tableExpense);

        // Create the categories table
        String tableCategory = "CREATE TABLE " + TABLE_NAME_CATEGORY + " ( "
                + ID_CATEGORY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_CATEGORY + " VARCHAR(100) NOT NULL, "
                + IS_CUSTOM + " INTEGER NOT NULL DEFAULT 0, "
                + ICON_NAME + " VARCHAR(100), "
                + CREATED_AT + " DATETIME, "
                + UPDATED_AT + " DATETIME, "
                + DELETED_AT + " DATETIME ) ";
        db.execSQL(tableCategory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 10) {
            // Add budget_id column to expenses table
            db.execSQL("ALTER TABLE " + TABLE_NAME_EXPENSE + " ADD COLUMN " + BUDGET_ID + " INTEGER");
            
            // Add foreign key constraint
            db.execSQL("CREATE TABLE expenses_new ("
                    + ID_EXPENSE + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME_EXPENSE + " VARCHAR(100) NOT NULL, "
                    + MONEY_EXPENSE + " REAL NOT NULL, "
                    + DESCRIPTION_EXPENSE + " VARCHAR(200), "
                    + CATEGORY_EXPENSE + " VARCHAR(100), "
                    + BUDGET_ID + " INTEGER, "
                    + CREATED_AT + " DATETIME, "
                    + UPDATED_AT + " DATETIME, "
                    + DELETED_AT + " DATETIME, "
                    + "FOREIGN KEY(" + BUDGET_ID + ") REFERENCES " + TABLE_NAME_BUDGET + "(" + ID_BUDGET + "))");
            
            // Copy data to new table
            db.execSQL("INSERT INTO expenses_new SELECT * FROM " + TABLE_NAME_EXPENSE);
            
            // Drop old table and rename new one
            db.execSQL("DROP TABLE " + TABLE_NAME_EXPENSE);
            db.execSQL("ALTER TABLE expenses_new RENAME TO " + TABLE_NAME_EXPENSE);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CATEGORY);
        onCreate(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    private String getCurrentMonth() {
        return new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}