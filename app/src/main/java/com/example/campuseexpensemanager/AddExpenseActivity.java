package com.example.campuseexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campuseexpensemanager.adapter.CategorySpinnerAdapter;
import com.example.campuseexpensemanager.database.BudgetDb;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.model.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText etName, etAmount, etDescription;
    private AutoCompleteTextView spinnerCategory;
    private MaterialButton btnSave;
    private ExpenseDb expenseDb;
    private CategoryDb categoryDb;
    private BudgetDb budgetDb;
    private List<Category> categories;
    private CategorySpinnerAdapter categoryAdapter;
    private boolean isEditMode = false;
    private int expenseId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize databases
        expenseDb = new ExpenseDb(this);
        categoryDb = new CategoryDb(this);
        budgetDb = new BudgetDb(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        etName = findViewById(R.id.etExpenseName);
        etAmount = findViewById(R.id.etExpenseAmount);
        etDescription = findViewById(R.id.etExpenseDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);

        // Setup category dropdown
        categories = categoryDb.getAllCategories();
        categoryAdapter = new CategorySpinnerAdapter(this, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Check if we're in edit mode
        if (getIntent().hasExtra("expense_id")) {
            isEditMode = true;
            expenseId = getIntent().getIntExtra("expense_id", -1);
            String name = getIntent().getStringExtra("expense_name");
            double amount = getIntent().getDoubleExtra("expense_amount", 0);
            String description = getIntent().getStringExtra("expense_description");
            String category = getIntent().getStringExtra("expense_category");

            // Set the fields with existing data
            etName.setText(name);
            etAmount.setText(String.valueOf(amount));
            etDescription.setText(description);
            spinnerCategory.setText(category, false);

            // Update UI for edit mode
            getSupportActionBar().setTitle("Edit Expense");
            btnSave.setText("Update Expense");
        } else {
            getSupportActionBar().setTitle("Add New Expense");
            btnSave.setText("Add Expense");
        }

        // Setup save button click listener
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String amountStr = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            String categoryName = spinnerCategory.getText().toString();

            if (name.isEmpty() || amountStr.isEmpty() || categoryName.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                long result;

                if (isEditMode) {
                    result = expenseDb.editExpense(expenseId, name, amount, description, categoryName);
                    if (result > 0) {
                        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Check if category has a budget before adding expense
                    if (!budgetDb.hasBudgetForCategory(categoryName)) {
                        android.util.Log.d("AddExpenseActivity", "No budget found for category: " + categoryName);
                        Toast.makeText(this, "Cannot add expense: No budget set for this category", Toast.LENGTH_LONG).show();
                        return;
                    }
                    android.util.Log.d("AddExpenseActivity", "Budget found for category: " + categoryName);

                    result = expenseDb.addExpense(name, amount, description, categoryName);
                    if (result != -1) {
                        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (expenseDb != null) {
            expenseDb.close();
        }
        if (categoryDb != null) {
            categoryDb.close();
        }
        if (budgetDb != null) {
            budgetDb.close();
        }
    }
} 