package com.example.campuseexpensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.adapter.CategoryAdapter;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.dialog.AddCategoryDialog;
import com.example.campuseexpensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private CategoryDb categoryDb;
    private List<Category> categories;
    private CategoryAdapter adapter;
    private RecyclerView recyclerView;
    private Runnable dataChangeCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Initialize database
        categoryDb = new CategoryDb(requireContext());

        // Initialize views
        recyclerView = view.findViewById(R.id.listViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        com.google.android.material.button.MaterialButton btnAddCategory = view.findViewById(R.id.btnAddCategory);

        // Create callback for data changes
        dataChangeCallback = this::loadCategories;
        
        // Register the callback
        categoryDb.addOnDataChangedCallback(dataChangeCallback);

        // Load categories
        loadCategories();

        // Setup add category button
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        return view;
    }

    private void loadCategories() {
        categories = categoryDb.getAllCategories();
        adapter = new CategoryAdapter(categories, category -> {
            if (category.isCustom()) {
                showCategoryOptionsDialog(category);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        AddCategoryDialog dialog = new AddCategoryDialog(requireContext(), categoryDb, newCategory -> {
            categories.add(newCategory);
            adapter.updateCategories(categories);
        });
        dialog.show();
    }

    private void showCategoryOptionsDialog(Category category) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Category Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditCategoryDialog(category);
                    } else {
                        showDeleteCategoryDialog(category);
                    }
                })
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        AddCategoryDialog dialog = new AddCategoryDialog(requireContext(), categoryDb, updatedCategory -> {
            int position = categories.indexOf(category);
            if (position != -1) {
                categories.set(position, updatedCategory);
                adapter.updateCategories(categories);
            }
        });
        dialog.setCategoryToEdit(category);
        dialog.show();
    }

    private void showDeleteCategoryDialog(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = categoryDb.deleteCategory(category.getId());
                    if (result == -2) {
                        // Category has existing budgets
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Cannot Delete Category")
                                .setMessage("This category cannot be deleted because it has existing budgets. Please delete all budgets in this category first.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else if (result > 0) {
                        categories.remove(category);
                        adapter.updateCategories(categories);
                        Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove callback to prevent memory leaks
        if (categoryDb != null && dataChangeCallback != null) {
            categoryDb.removeOnDataChangedCallback(dataChangeCallback);
        }
    }
}
