package com.example.campuseexpensemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.model.Category;
import com.example.campuseexpensemanager.model.Expenses;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {
    RecyclerView recyclerView;
    ExpenseAdapter adapter;
    List<Expenses> expensesList;
    ExpenseDb expenseDb;
    CategoryDb categoryDb;
    TextView tvTotalExpenses;
    private CategorySpinnerAdapter categoryAdapter;
    private List<Category> categories;
    
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
        
        // Initialize databases
        expenseDb = new ExpenseDb(requireContext());
        categoryDb = new CategoryDb(requireContext());
        
        // Create the callback once and store it
        dataChangeCallback = this::refreshData;
        
        // Register for expense data changes
        expenseDb.addOnDataChangedCallback(dataChangeCallback);
        android.util.Log.d("ExpensesFragment", "Callback registered");

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewExpenses);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        
        // Setup RecyclerView
        expensesList = new ArrayList<>();
        adapter = new ExpenseAdapter(expensesList, this::showEditDialog, this::showDeleteDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
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
        // Remove the callback when the fragment is destroyed
        if (expenseDb != null && dataChangeCallback != null) {
            expenseDb.removeOnDataChangedCallback(dataChangeCallback);
        }
        if (expenseDb != null) {
            expenseDb.close();
        }
        if (categoryDb != null) {
            categoryDb.close();
        }
    }

    public void loadExpenses() {
        expensesList.clear();
        expensesList.addAll(expenseDb.getAllExpenses());
        adapter.notifyDataSetChanged();
        updateTotalExpenses();
    }

    private void updateTotalExpenses() {
        double total = 0;
        for (Expenses expense : expensesList) {
            total += expense.getMoney();
        }
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    public void showAddDialog() {
        Intent intent = new Intent(requireContext(), AddExpenseActivity.class);
        startActivityForResult(intent, ADD_EXPENSE_REQUEST_CODE);
    }

    private static final int ADD_EXPENSE_REQUEST_CODE = 1001;


    private void showEditDialog(Expenses expense) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etExpenseName);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etExpenseDescription);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Load categories for spinner
        categories = categoryDb.getAllCategories();
        categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set current values
        etName.setText(expense.getName());
        etAmount.setText(String.valueOf(expense.getMoney()));
        etDescription.setText(expense.getDescription());
        
        // Set current category
        String currentCategory = expense.getCategory();
        spinnerCategory.setText(currentCategory, false);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Expense")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String amountStr = etAmount.getText().toString();
                    String description = etDescription.getText().toString();
                    String selectedCategory = spinnerCategory.getText().toString();

                    if (name.isEmpty() || amountStr.isEmpty() || selectedCategory.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    expense.setName(name);
                    expense.setMoney(amount);
                    expense.setDescription(description);
                    expense.setCategory(selectedCategory);

                    int result = expenseDb.editExpense(expense.getId(), name, amount, description, selectedCategory);
                    if (result > 0) {
                        loadExpenses();
                        Toast.makeText(requireContext(), "Expense updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Expenses expense) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = expenseDb.deleteExpense(expense.getId());
                    if (result > 0) {
                        loadExpenses();
                        Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
