package com.example.campuseexpensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.adapter.CategorySpinnerAdapter;
import com.example.campuseexpensemanager.adapter.ExpenseAdapter;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.model.Categories;
import com.example.campuseexpensemanager.model.Expenses;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {
    RecyclerView recyclerView;
    ExpenseAdapter adapter;
    List<Expenses> expensesList;
    ExpenseDb expenseDb;
    TextView tvTotalExpenses;
    ExtendedFloatingActionButton fabAddExpense;
    
    // Store the callback as a field so we can properly remove it later
    private Runnable dataChangeCallback;

    private void refreshData() {
        if (getActivity() != null && isAdded()) {
            android.util.Log.d("ExpensesFragment", "Refreshing data");
            // Clear and reload all expenses
            expensesList.clear();
            expensesList.addAll(expenseDb.getAllExpenses());
            
            // Update total expenses
            updateTotalExpenses();
            
            // Notify adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                android.util.Log.d("ExpensesFragment", "Adapter notified of data change");
            }
        } else {
            android.util.Log.d("ExpensesFragment", "Cannot refresh data: Activity is null or fragment not added");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        
        // Initialize database
        expenseDb = new ExpenseDb(requireContext());
        
        // Create the callback once and store it
        dataChangeCallback = this::refreshData;
        
        // Register for expense data changes
        expenseDb.addOnDataChangedCallback(dataChangeCallback);
        android.util.Log.d("ExpensesFragment", "Callback registered");

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewExpenses);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        
        // Setup RecyclerView
        expensesList = new ArrayList<>();
        adapter = new ExpenseAdapter(expensesList, this::showEditDialog, this::showDeleteDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Set up FAB click listener
        fabAddExpense.setOnClickListener(v -> showAddDialog());
        
        // Load initial expenses
        loadExpenses();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        refreshData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the callback to prevent memory leaks
        if (expenseDb != null && dataChangeCallback != null) {
            expenseDb.removeOnDataChangedCallback(dataChangeCallback);
        }
    }

    public void loadExpenses() {
        expensesList.clear();
        expensesList.addAll(expenseDb.getAllExpenses());
        adapter.notifyDataSetChanged();
        updateTotalExpenses();
    }

    private void updateTotalExpenses() {
        double total = 0.0;
        for (Expenses expense : expensesList) {
            total += expense.getMoney();
        }
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        
        EditText etName = view.findViewById(R.id.etExpenseName);
        EditText etAmount = view.findViewById(R.id.etExpenseAmount);
        EditText etDescription = view.findViewById(R.id.etExpenseDescription);
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.spinnerCategory);
        
        // Setup category dropdown with custom adapter
        List<Categories> categories = Arrays.asList(Categories.values());
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        categoryDropdown.setAdapter(categoryAdapter);
        
        // Set a default selection
        if (categories.size() > 0) {
            Categories defaultCategory = categories.get(0);
            categoryDropdown.setText(defaultCategory.getDisplayName());
        }
        
        // Make sure the dropdown is properly configured
        categoryDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            Categories selectedCategory = categoryAdapter.getItem(position);
            if (selectedCategory != null) {
                categoryDropdown.setText(selectedCategory.getDisplayName());
            }
        });
        
        new MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle("Add New Expense")
            .setPositiveButton("Add", (dialog, which) -> {
                String name = etName.getText().toString();
                String amountStr = etAmount.getText().toString();
                String description = etDescription.getText().toString();
                String categoryStr = categoryDropdown.getText().toString().trim();
                
                if (name.isEmpty() || amountStr.isEmpty() || categoryStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    
                    // Find the matching category enum
                    Categories selectedCategory = Categories.fromDisplayName(categoryStr);
                    if (selectedCategory == Categories.OTHER && !categoryStr.equals("Other")) {
                        Toast.makeText(getContext(), "Please select a valid category", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Create new expense
                    long result = expenseDb.addExpense(name, amount, description, selectedCategory.getDisplayName());
                    
                    if (result != -1) {
                        Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
                        // Let the callback handle the UI update
                    } else {
                        Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditDialog(Expenses expense) {
        View view = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        
        EditText etName = view.findViewById(R.id.etExpenseName);
        EditText etAmount = view.findViewById(R.id.etExpenseAmount);
        EditText etDescription = view.findViewById(R.id.etExpenseDescription);
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.spinnerCategory);
        
        // Setup category dropdown with custom adapter
        List<Categories> categories = Arrays.asList(Categories.values());
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        categoryDropdown.setAdapter(categoryAdapter);
        
        // Set current values
        etName.setText(expense.getName());
        etAmount.setText(String.valueOf(expense.getMoney()));
        etDescription.setText(expense.getDescription());
        categoryDropdown.setText(expense.getCategory());
        
        // Make sure the dropdown is properly configured
        categoryDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            Categories selectedCategory = categoryAdapter.getItem(position);
            if (selectedCategory != null) {
                categoryDropdown.setText(selectedCategory.getDisplayName());
            }
        });
        
        new MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle("Edit Expense")
            .setPositiveButton("Update", (dialog, which) -> {
                String name = etName.getText().toString();
                String amountStr = etAmount.getText().toString();
                String description = etDescription.getText().toString();
                String categoryStr = categoryDropdown.getText().toString().trim();
                
                if (name.isEmpty() || amountStr.isEmpty() || categoryStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    
                    // Find the matching category enum
                    Categories selectedCategory = Categories.fromDisplayName(categoryStr);
                    if (selectedCategory == Categories.OTHER && !categoryStr.equals("Other")) {
                        Toast.makeText(getContext(), "Please select a valid category", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    expenseDb.editExpense(expense.getId(), name, amount, description, selectedCategory.getDisplayName());
                    // Let the callback handle the UI update
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDeleteDialog(Expenses expense) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete", (dialog, which) -> {
                expenseDb.deleteExpense(expense.getId());
                // Let the callback handle the UI update
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
