package com.example.campuseexpensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDb {
    private SQLiteDatabase database;
    private DatabaseContext dbHelper;

    public CategoryDb(Context context) {
        dbHelper = new DatabaseContext(context);
        database = dbHelper.getWritableDatabase();
    }

    public long addCategory(Category category) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContext.NAME_CATEGORY, category.getName());
            values.put(DatabaseContext.IS_CUSTOM, category.isCustom() ? 1 : 0);
            values.put(DatabaseContext.ICON_NAME, "category_24dp");
            values.put(DatabaseContext.CREATED_AT, getCurrentDateTime());
            long result = database.insert(DatabaseContext.TABLE_NAME_CATEGORY, null, values);
            android.util.Log.d("CategoryDb", "Add category result: " + result);
            return result;
        } catch (Exception e) {
            android.util.Log.e("CategoryDb", "Error adding category: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = database.query("categories", null, null, null, null, null, "name ASC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                boolean isCustom = cursor.getInt(cursor.getColumnIndexOrThrow("is_custom")) == 1;
                categories.add(new Category(id, name, isCustom));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    public int updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put("name", category.getName());
        values.put("is_custom", category.isCustom() ? 1 : 0);
        return database.update("categories", values, "id = ?", new String[]{String.valueOf(category.getId())});
    }

    public int deleteCategory(int id) {
        return database.delete("categories", "id = ?", new String[]{String.valueOf(id)});
    }

    public void close() {
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 