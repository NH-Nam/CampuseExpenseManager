package com.example.campuseexpensemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.adapter.BudgetCategoryAdapter;
import com.example.campuseexpensemanager.database.BudgetDb;
import com.example.campuseexpensemanager.model.Budgets;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment implements BudgetCategoryAdapter.OnBudgetCategoryClickListener {
    private BudgetDb budgetDb;
    private RecyclerView recyclerView;
    private BudgetCategoryAdapter adapter;
    private TextView totalBudgetTextView;
    private TextView totalSpentTextView;
    private TextView remainingBudgetTextView;
    private LinearProgressIndicator progressIndicator;
    private MaterialButton addCategoryButton;
    private ExtendedFloatingActionButton fabAddCategory;
    private List<Budgets> budgetCategories;

    private static final String[] PREDEFINED_CATEGORIES = {
        "Food", "Transportation", "Entertainment", "Shopping", "Bills", "Education", "Health"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        budgetDb = new BudgetDb(requireContext());
        budgetCategories = new ArrayList<>();

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadBudgetCategories();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvBudgetCategories);
        totalBudgetTextView = view.findViewById(R.id.tvTotalBudget);
        totalSpentTextView = view.findViewById(R.id.tvTotalSpent);
        remainingBudgetTextView = view.findViewById(R.id.tvRemainingBudget);
        progressIndicator = view.findViewById(R.id.progressBudget);
        addCategoryButton = view.findViewById(R.id.btnAddCategory);
    }

    private void setupRecyclerView() {
        adapter = new BudgetCategoryAdapter(budgetCategories, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        addCategoryButton.setOnClickListener(v -> showAddEditBudgetDialog(null));
    }

    public void loadBudgetCategories() {
        budgetCategories.clear();
        budgetCategories.addAll(budgetDb.getAllBudgetCategories());
        adapter.notifyDataSetChanged();
        updateBudgetSummary();
    }

    private void updateBudgetSummary() {
        double totalBudget = 0.0;
        double totalSpent = 0.0;

        for (Budgets category : budgetCategories) {
            totalBudget += category.getMoney();
            totalSpent += category.getSpentAmount();
        }

        double remainingBudget = totalBudget - totalSpent;
        int progressPercentage = totalBudget > 0 ? (int) ((totalSpent / totalBudget) * 100) : 0;

        totalBudgetTextView.setText(String.format("Total Budget: $%.2f", totalBudget));
        totalSpentTextView.setText(String.format("Total Spent: $%.2f", totalSpent));
        remainingBudgetTextView.setText(String.format("Remaining: $%.2f", remainingBudget));
        progressIndicator.setProgress(progressPercentage);
    }

    private void showAddEditBudgetDialog(Budgets budgetCategory) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_budget, null);
        AutoCompleteTextView categorySpinner = dialogView.findViewById(R.id.spinnerCategory);
        TextInputEditText budgetAmountInput = dialogView.findViewById(R.id.editTextBudgetAmount);

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PREDEFINED_CATEGORIES
        );
        categorySpinner.setAdapter(categoryAdapter);

        if (budgetCategory != null) {
            categorySpinner.setText(budgetCategory.getName(), false);
            budgetAmountInput.setText(String.valueOf(budgetCategory.getMoney()));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(budgetCategory == null ? "Add Budget Category" : "Edit Budget Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String categoryName = categorySpinner.getText().toString().trim();
                    String budgetAmountStr = budgetAmountInput.getText().toString().trim();

                    if (categoryName.isEmpty() || budgetAmountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double budgetAmount;
                    try {
                        budgetAmount = Double.parseDouble(budgetAmountStr);
                        if (budgetAmount <= 0) {
                            Toast.makeText(requireContext(), "Budget amount must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (budgetCategory == null) {
                        Budgets newCategory = new Budgets();
                        newCategory.setName(categoryName);
                        newCategory.setCategory(categoryName);
                        newCategory.setMoney(budgetAmount);
                        newCategory.setMonthYear(budgetDb.getCurrentMonth());
                        budgetDb.addBudgetCategory(newCategory);
                    } else {
                        budgetCategory.setName(categoryName);
                        budgetCategory.setCategory(categoryName);
                        budgetCategory.setMoney(budgetAmount);
                        budgetDb.updateBudgetCategory(budgetCategory);
                    }

                    loadBudgetCategories();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Budgets budgetCategory) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Budget Category")
                .setMessage("Are you sure you want to delete this budget category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    budgetDb.deleteBudgetCategory(budgetCategory.getId());
                    loadBudgetCategories();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditClick(Budgets budgetCategory) {
        showAddEditBudgetDialog(budgetCategory);
    }

    @Override
    public void onDeleteClick(Budgets budgetCategory) {
        showDeleteConfirmationDialog(budgetCategory);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (budgetDb != null) {
            budgetDb.close();
        }
    }
}
