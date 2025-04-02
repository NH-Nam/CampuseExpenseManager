package com.example.campuseexpensemanager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.model.Users;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UserDb {
    private final SQLiteDatabase dbRead, dbWrite;
    public UserDb(@Nullable Context context) {
        DatabaseContext dbHelper = new DatabaseContext(context);
        dbRead = dbHelper.getReadableDatabase();
        dbWrite = dbHelper.getWritableDatabase();
    }

    public long insertUserAccount(String username, String password, String email, String phone){
        // xu ly lay thoi gian hien tai
        @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        @SuppressLint({"NewApi", "LocalSuppress"}) ZonedDateTime zoneDt = ZonedDateTime.now();
        @SuppressLint({"NewApi", "LocalSuppress"}) String currentDate = dtf.format(zoneDt);
        ContentValues values = new ContentValues();
        values.put(DatabaseContext.USERNAME_COL, username);
        values.put(DatabaseContext.PASSWORD_COL, password);
        values.put(DatabaseContext.EMAIL_COL, email);
        values.put(DatabaseContext.PHONE_COL, phone);
        values.put(DatabaseContext.ROLE_ID_COL, 1);
        values.put(DatabaseContext.CREATED_AT, currentDate);
        long insert = dbWrite.insert(DatabaseContext.TABLE_NAME, null, values);
        return insert;
    }

    // check user login
    @SuppressLint("Range")
    public Users checkLoginUser(String username, String password){
        Users users = new Users();
        try {
            // SELECT id, username, email, phone, roleId FROM users WHERE username = ? AND password = ?;
            String[] cols = { DatabaseContext.ID_COL, DatabaseContext.USERNAME_COL, DatabaseContext.EMAIL_COL, DatabaseContext.PHONE_COL, DatabaseContext.ROLE_ID_COL };
            String condition = DatabaseContext.USERNAME_COL + " =? AND " + DatabaseContext.PASSWORD_COL + " =? ";
            String[] params = { username, password };
            Cursor cursor = dbRead.query(DatabaseContext.TABLE_NAME, cols, condition, params, null, null, null);
            if (cursor.getCount() > 0){
                cursor.moveToFirst();
                // anh xa du lieu tu database vao model
                users.setId(cursor.getInt(cursor.getColumnIndex(DatabaseContext.ID_COL)));
                users.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseContext.USERNAME_COL)));
                users.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseContext.EMAIL_COL)));
                users.setPhone(cursor.getString(cursor.getColumnIndex(DatabaseContext.PHONE_COL)));
                users.setRoleId(cursor.getInt(cursor.getColumnIndex(DatabaseContext.ROLE_ID_COL)));
            }
            cursor.close();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public boolean checkExistsUsernameAndEmail(String username, String email){
        boolean checking = false;
        try {
            String[] cols = { DatabaseContext.ID_COL, DatabaseContext.USERNAME_COL, DatabaseContext.EMAIL_COL };
            String condition = DatabaseContext.USERNAME_COL + " =? AND " + DatabaseContext.EMAIL_COL + " =? ";
            String[] params = { username, email };
            Cursor cursor = dbRead.query(DatabaseContext.TABLE_NAME, cols, condition, params, null, null, null);
            if (cursor.getCount() > 0){
                checking = true;
            }
            cursor.close();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return checking;
    }

    public int changePassword(String newPassword, String account, String email){
        int check = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContext.PASSWORD_COL, newPassword);
            String condition = DatabaseContext.USERNAME_COL + " =? AND " + DatabaseContext.EMAIL_COL + " =? ";
            String[] params = { account, email };
            check = dbWrite.update(DatabaseContext.TABLE_NAME, values, condition, params);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return check;
    }
}
