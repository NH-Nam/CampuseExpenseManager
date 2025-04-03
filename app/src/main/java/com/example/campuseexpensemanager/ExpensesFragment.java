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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewExpenses);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        
        // Initialize database
        expenseDb = new ExpenseDb(requireContext());
        
        // Setup RecyclerView
        expensesList = new ArrayList<>();
        adapter = new ExpenseAdapter(expensesList, this::showEditDialog, this::showDeleteDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Load expenses
        loadExpenses();
        
        return view;
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
        Spinner spCategory = view.findViewById(R.id.spinnerCategory);
        
        // Setup category spinner with custom adapter
        List<Categories> categories = Arrays.asList(Categories.values());
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        spCategory.setAdapter(categoryAdapter);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle("Add New Expense")
            .setPositiveButton("Add", (dialog, which) -> {
                String name = etName.getText().toString();
                String amountStr = etAmount.getText().toString();
                String description = etDescription.getText().toString();
                Categories category = (Categories) spCategory.getSelectedItem();
                
                if (name.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double amount = Double.parseDouble(amountStr);
                expenseDb.addExpense(name, amount, description, category.getDisplayName());
                loadExpenses();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditDialog(Expenses expense) {
        View view = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        
        EditText etName = view.findViewById(R.id.etExpenseName);
        EditText etAmount = view.findViewById(R.id.etExpenseAmount);
        EditText etDescription = view.findViewById(R.id.etExpenseDescription);
        Spinner spCategory = view.findViewById(R.id.spinnerCategory);
        
        // Setup category spinner with custom adapter
        List<Categories> categories = Arrays.asList(Categories.values());
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        spCategory.setAdapter(categoryAdapter);
        
        // Set current values
        etName.setText(expense.getName());
        etAmount.setText(String.valueOf(expense.getMoney()));
        etDescription.setText(expense.getDescription());
        spCategory.setSelection(categoryAdapter.getPosition(Categories.fromDisplayName(expense.getCategory())));
        
        new MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle("Edit Expense")
            .setPositiveButton("Update", (dialog, which) -> {
                String name = etName.getText().toString();
                String amountStr = etAmount.getText().toString();
                String description = etDescription.getText().toString();
                Categories category = (Categories) spCategory.getSelectedItem();
                
                if (name.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double amount = Double.parseDouble(amountStr);
                expenseDb.editExpense(expense.getId(), name, amount, description, category.getDisplayName());
                loadExpenses();
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
                loadExpenses();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
