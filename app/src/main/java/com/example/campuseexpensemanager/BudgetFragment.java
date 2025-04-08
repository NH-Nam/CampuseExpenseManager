package com.example.campuseexpensemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.adapter.BudgetCategoryAdapter;
import com.example.campuseexpensemanager.adapter.CategorySpinnerAdapter;
import com.example.campuseexpensemanager.database.BudgetDb;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.model.Budgets;
import com.example.campuseexpensemanager.model.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment implements BudgetCategoryAdapter.OnBudgetCategoryClickListener {
    private BudgetDb budgetDb;
    private ExpenseDb expenseDb;
    private CategoryDb categoryDb;
    private RecyclerView recyclerView;
    private BudgetCategoryAdapter adapter;
    private TextView totalBudgetTextView;
    private TextView totalSpentTextView;
    private TextView remainingBudgetTextView;
    private LinearProgressIndicator progressIndicator;
    private MaterialButton addCategoryButton;
    private List<Budgets> budgetCategories;
    private List<Category> categories;

    // Store the callback as a field so we can properly remove it later
    private Runnable dataChangeCallback;

    private static final String[] PREDEFINED_CATEGORIES = {
        "Food", "Transportation", "Entertainment", "Shopping", "Bills", "Education", "Health"
    };

    private void refreshData() {
        if (getActivity() != null && isAdded()) {
            android.util.Log.d("BudgetFragment", "Refreshing budget data");
            // Clear and reload budget categories
            budgetCategories.clear();
            budgetCategories.addAll(budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth()));
            
            // Update budget summary
            updateBudgetSummary();
            
            // Notify adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                android.util.Log.d("BudgetFragment", "Budget adapter notified of data change");
            }
        } else {
            android.util.Log.d("BudgetFragment", "Cannot refresh data: Activity is null or fragment not added");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        refreshData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        budgetDb = new BudgetDb(requireContext());
        expenseDb = new ExpenseDb(requireContext());
        categoryDb = new CategoryDb(requireContext());
        
        // Create the callback once and store it
        dataChangeCallback = () -> {
            android.util.Log.d("BudgetFragment", "Data change callback triggered");
            refreshData();
        };
        
        // Register for expense data changes
        expenseDb.addOnDataChangedCallback(dataChangeCallback);
        android.util.Log.d("BudgetFragment", "Callback registered for expense data changes");
        
        budgetCategories = new ArrayList<>();
        categories = categoryDb.getAllCategories();

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
        budgetCategories.addAll(budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth()));
        updateBudgetSummary();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateBudgetSummary() {
        double totalBudget = 0.0;
        double totalSpent = 0.0;

        for (Budgets category : budgetCategories) {
            totalBudget += category.getMoney();
            totalSpent += category.getSpentAmount();
        }

        final double finalTotalBudget = totalBudget;
        final double finalTotalSpent = totalSpent;
        final double remainingBudget = totalBudget - totalSpent;
        final int progressPercentage = totalBudget > 0 ? (int) ((totalSpent / totalBudget) * 100) : 0;

        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                try {
                    totalBudgetTextView.setText(String.format("Total Budget: $%.2f", finalTotalBudget));
                    totalSpentTextView.setText(String.format("Total Spent: $%.2f", finalTotalSpent));
                    remainingBudgetTextView.setText(String.format("Remaining: $%.2f", remainingBudget));
                    progressIndicator.setProgress(progressPercentage);
                    android.util.Log.d("BudgetFragment", "Budget summary updated - Total: $" + finalTotalBudget + ", Spent: $" + finalTotalSpent);
                } catch (Exception e) {
                    android.util.Log.e("BudgetFragment", "Error updating budget summary", e);
                }
            });
        }
    }

    private void showAddEditBudgetDialog(Budgets budgetCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_budget, null);
        builder.setView(dialogView);

        // Initialize views
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        TextInputEditText etBudgetAmount = dialogView.findViewById(R.id.editTextBudgetAmount);

        // Set up category dropdown
        List<Category> categories = categoryDb.getAllCategories();
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set title and populate fields if editing
        if (budgetCategory != null) {
            builder.setTitle("Edit Budget Category");
            spinnerCategory.setText(budgetCategory.getCategory(), false);
            etBudgetAmount.setText(String.valueOf(budgetCategory.getMoney()));
        } else {
            builder.setTitle("Add Budget Category");
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String category = spinnerCategory.getText().toString().trim();
            String amountStr = etBudgetAmount.getText().toString().trim();

            if (category.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (budgetCategory == null) {
                    // Add new budget category
                    Budgets newBudget = new Budgets();
                    newBudget.setName(category);
                    newBudget.setMoney(amount);
                    newBudget.setCategory(category);
                    newBudget.setMonthYear(getCurrentMonthYear());
                    newBudget.setSpentAmount(0.0);
                    budgetDb.addBudgetCategory(newBudget);
                } else {
                    // Update existing budget category
                    budgetCategory.setName(category);
                    budgetCategory.setMoney(amount);
                    budgetCategory.setCategory(category);
                    budgetDb.updateBudgetCategory(budgetCategory);
                }
                refreshData();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showDeleteDialog(Budgets budgetCategory) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Budget Category")
                .setMessage("Are you sure you want to delete this budget category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = budgetDb.deleteBudgetCategory(budgetCategory.getId());
                    if (result == -1) {
                        // There are expenses in this category
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Cannot Delete Category")
                                .setMessage("This budget category has associated expenses. Please delete all expenses in this category first.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else if (result > 0) {
                        Toast.makeText(requireContext(), "Budget category deleted successfully", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete budget category", Toast.LENGTH_SHORT).show();
                    }
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
        showDeleteDialog(budgetCategory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the callback to prevent memory leaks
        if (expenseDb != null && dataChangeCallback != null) {
            expenseDb.removeOnDataChangedCallback(dataChangeCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (budgetDb != null) {
            budgetDb.close();
        }
    }

    private String getCurrentMonthYear() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
