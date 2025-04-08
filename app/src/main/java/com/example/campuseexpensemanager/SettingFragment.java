package com.example.campuseexpensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuseexpensemanager.adapter.CategorySpinnerAdapter;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.dialog.AddCategoryDialog;
import com.example.campuseexpensemanager.model.Category;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private CategoryDb categoryDb;
    private List<Category> categories;
    private ArrayAdapter<Category> adapter;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Initialize database
        categoryDb = new CategoryDb(requireContext());

        // Initialize views
        listView = view.findViewById(R.id.listViewCategories);
        Button btnAddCategory = view.findViewById(R.id.btnAddCategory);

        // Load categories
        loadCategories();

        // Setup add category button
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        // Setup list item click listener
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Category category = categories.get(position);
            if (category.isCustom()) {
                showCategoryOptionsDialog(category);
            }
        });

        return view;
    }

    private void loadCategories() {
        categories = categoryDb.getAllCategories();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        AddCategoryDialog dialog = new AddCategoryDialog(requireContext(), categoryDb, newCategory -> {
            categories.add(newCategory);
            adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
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
                    if (result > 0) {
                        categories.remove(category);
                        adapter.notifyDataSetChanged();
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
        if (categoryDb != null) {
            categoryDb.close();
        }
    }
}
