package com.example.asm_app_se06304.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDb {
    private DatabaseContext dbContext;
    private SQLiteDatabase database;

    public UserDb(Context context) {
        dbContext = new DatabaseContext(context);
    }

    public void open() {
        database = dbContext.getWritableDatabase();
    }

    public void close() {
        dbContext.close();
    }

    // Insert a new user
    public long insertUser(String username, String passwordHash, String email, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.USERNAME_COL, username);
        values.put(DatabaseContext.PASSWORD_COL, passwordHash);
        values.put(DatabaseContext.EMAIL_COL, email);
        values.put(DatabaseContext.PHONE_COL, phoneNumber);
        return database.insert(DatabaseContext.USERS_TABLE, null, values);
    }

    // Get user by ID
    public Cursor getUserById(int userId) {
        String[] columns = {
                DatabaseContext.ID_COL,
                DatabaseContext.USERNAME_COL,
                DatabaseContext.PASSWORD_COL,
                DatabaseContext.EMAIL_COL,
                DatabaseContext.PHONE_COL,
                DatabaseContext.CREATED_AT_COL,
                DatabaseContext.LAST_LOGIN_COL
        };
        return database.query(DatabaseContext.USERS_TABLE, columns, DatabaseContext.ID_COL + " = ?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    // Update user
    public int updateUser(int userId, String username, String passwordHash, String email, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.USERNAME_COL, username);
        values.put(DatabaseContext.PASSWORD_COL, passwordHash);
        values.put(DatabaseContext.EMAIL_COL, email);
        values.put(DatabaseContext.PHONE_COL, phoneNumber);
        return database.update(DatabaseContext.USERS_TABLE, values, DatabaseContext.ID_COL + " = ?", new String[]{String.valueOf(userId)});
    }

    // Delete user
    public void deleteUser(int userId) {
        database.delete(DatabaseContext.USERS_TABLE, DatabaseContext.ID_COL + " = ?", new String[]{String.valueOf(userId)});
    }

    // Check if a username exists
    public boolean isUsernameExists(String username) {
        Cursor cursor = database.query(DatabaseContext.USERS_TABLE, new String[]{DatabaseContext.ID_COL}, DatabaseContext.USERNAME_COL + " = ?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Get user by username
    public Cursor getUserByUsername(String username) {
        String[] columns = {
                DatabaseContext.ID_COL,
                DatabaseContext.USERNAME_COL,
                DatabaseContext.PASSWORD_COL,
                DatabaseContext.EMAIL_COL,
                DatabaseContext.PHONE_COL,
                DatabaseContext.CREATED_AT_COL,
                DatabaseContext.LAST_LOGIN_COL
        };
        return database.query(DatabaseContext.USERS_TABLE, columns, DatabaseContext.USERNAME_COL + " = ?", new String[]{username}, null, null, null);
    }
}