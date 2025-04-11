package com.example.campuseexpensemanager.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.campuseexpensemanager.R;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.model.Category;

public class AddCategoryDialog {
    private final Context context;
    private final CategoryDb categoryDb;
    private final OnCategoryAddedListener listener;
    private Category categoryToEdit;

    public AddCategoryDialog(Context context, CategoryDb categoryDb, OnCategoryAddedListener listener) {
        this.context = context;
        this.categoryDb = categoryDb;
        this.listener = listener;
    }

    public void setCategoryToEdit(Category category) {
        this.categoryToEdit = category;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.etName);

        if (categoryToEdit != null) {
            etName.setText(categoryToEdit.getName());
        }

        new AlertDialog.Builder(context)
                .setTitle(categoryToEdit != null ? "Edit Category" : "Add New Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (categoryToEdit != null) {
                        categoryToEdit.setName(name);
                        int result = categoryDb.updateCategoryAndPropagate(categoryToEdit);
                        if (result > 0) {
                            listener.onCategoryAdded(categoryToEdit);
                        } else {
                            Toast.makeText(context, "Failed to update category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Category newCategory = new Category(0, name, true);
                        long result = categoryDb.addCategory(newCategory);
                        if (result != -1) {
                            newCategory.setId((int) result);
                            listener.onCategoryAdded(newCategory);
                        } else {
                            Toast.makeText(context, "Failed to add category", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public interface OnCategoryAddedListener {
        void onCategoryAdded(Category category);
    }
} 