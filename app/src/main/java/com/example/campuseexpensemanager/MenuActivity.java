package com.example.campuseexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campuseexpensemanager.adapter.CategorySpinnerAdapter;
import com.example.campuseexpensemanager.adapter.ViewPagerAdapter;
import com.example.campuseexpensemanager.database.BudgetDb;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.model.Budgets;
import com.example.campuseexpensemanager.model.Categories;
import com.example.campuseexpensemanager.model.Category;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    private String username;
    private ExtendedFloatingActionButton fabAddExpense;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        
        // Get username from intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            username = bundle.getString("USER_ACCOUNT", "User");
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager2 = findViewById(R.id.viewPager);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        fabAddExpense = findViewById(R.id.fabAddExpense);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem logout = menu.findItem(R.id.nav_logout);
        setupViewPager();

        // Setup FAB click listener
        fabAddExpense.setOnClickListener(v -> {
            // Get current fragment position
            int currentPosition = viewPager2.getCurrentItem();
            
            // Show appropriate dialog based on current fragment
            if (currentPosition == 0) {
                // Home fragment - show add expense dialog
                showAddExpenseDialog();
            } else if (currentPosition == 1) {
                // Expenses fragment - show add expense dialog
                showAddExpenseDialog();
            } else if (currentPosition == 2) {
                // Budget fragment - show add budget dialog
                showAddBudgetDialog();
            }
        });

        // bat su kien logout
        logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intentLogout = new Intent(MenuActivity.this, SignInActivity.class);
                startActivity(intentLogout);
                finish();
                return false;
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home){
                viewPager2.setCurrentItem(0);
            } else if (item.getItemId() == R.id.menu_expense) {
                viewPager2.setCurrentItem(1);
            } else if (item.getItemId() == R.id.menu_budget) {
                viewPager2.setCurrentItem(2);
            } else if (item.getItemId() == R.id.menu_setting) {
                viewPager2.setCurrentItem(3);
            }
            return true;
        });
    }

    private void setupViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), username);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0){
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                    fabAddExpense.show();
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_expense).setChecked(true);
                    fabAddExpense.show();
                } else if (position == 2) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_budget).setChecked(true);
                    fabAddExpense.hide();
                } else if (position == 3) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_setting).setChecked(true);
                    fabAddExpense.hide();
                } else {
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                    fabAddExpense.show();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_home){
            viewPager2.setCurrentItem(0);
        } else if (item.getItemId() == R.id.menu_expense) {
            viewPager2.setCurrentItem(1);
        } else if (item.getItemId() == R.id.menu_budget) {
            viewPager2.setCurrentItem(2);
        } else if (item.getItemId() == R.id.menu_setting) {
            viewPager2.setCurrentItem(3);
        } else {
            viewPager2.setCurrentItem(0);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAddExpenseDialog() {
        // Create a dialog using dialog_expense.xml
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        
        // Initialize views
        EditText etName = dialogView.findViewById(R.id.etExpenseName);
        EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        EditText etDescription = dialogView.findViewById(R.id.etExpenseDescription);
        MaterialAutoCompleteTextView categoryDropdown = dialogView.findViewById(R.id.spinnerCategory);
        
        // Setup category dropdown
        CategoryDb categoryDb = new CategoryDb(this);
        List<Category> categories = categoryDb.getAllCategories();
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(this, categories);
        categoryDropdown.setAdapter(categoryAdapter);
        
        // Set a default selection
        if (categories.size() > 0) {
            categoryDropdown.setText(categories.get(0).getName(), false);
        }
        
        // Make sure the dropdown is properly configured
        categoryDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            if (selectedCategory != null) {
                categoryDropdown.setText(selectedCategory.getName(), false);
            }
        });
        
        // Show dialog
        new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setTitle("Add New Expense")
            .setPositiveButton("Add", (dialog, which) -> {
                String name = etName.getText().toString();
                String amountStr = etAmount.getText().toString();
                String description = etDescription.getText().toString();
                String categoryStr = categoryDropdown.getText().toString();
                
                if (name.isEmpty() || amountStr.isEmpty() || categoryStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    
                    // Get the category from the database
                    Category selectedCategory = null;
                    
                    for (Category category : categories) {
                        if (category.getName().equals(categoryStr)) {
                            selectedCategory = category;
                            break;
                        }
                    }
                    
                    if (selectedCategory == null) {
                        Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Create new expense
                    ExpenseDb expenseDb = new ExpenseDb(this);
                    long result = expenseDb.addExpense(name, amount, description, selectedCategory.getName());
                    
                    if (result != -1) {
                        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the expense list
                        refreshCurrentFragment();
                    } else {
                        Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showAddBudgetDialog() {
        // Create a dialog using dialog_add_edit_budget.xml
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_budget, null);
        
        // Initialize views
        MaterialAutoCompleteTextView categorySpinner = dialogView.findViewById(R.id.spinnerCategory);
        TextInputEditText budgetAmountInput = dialogView.findViewById(R.id.editTextBudgetAmount);
        
        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            new String[]{"Food", "Transportation", "Entertainment", "Shopping", "Bills", "Education", "Health"}
        );
        categorySpinner.setAdapter(categoryAdapter);
        
        // Show dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Budget Category")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String categoryName = categorySpinner.getText().toString().trim();
                String budgetAmountStr = budgetAmountInput.getText().toString().trim();
                
                if (categoryName.isEmpty() || budgetAmountStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    double budgetAmount = Double.parseDouble(budgetAmountStr);
                    if (budgetAmount <= 0) {
                        Toast.makeText(this, "Budget amount must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    BudgetDb budgetDb = new BudgetDb(this);
                    Budgets newCategory = new Budgets();
                    newCategory.setName(categoryName);
                    newCategory.setCategory(categoryName);
                    newCategory.setMoney(budgetAmount);
                    newCategory.setMonthYear(budgetDb.getCurrentMonth());
                    budgetDb.addBudgetCategory(newCategory);
                    budgetDb.close();
                    
                    Toast.makeText(this, "Budget category added successfully", Toast.LENGTH_SHORT).show();
                    
                    // Refresh the current fragment
                    refreshCurrentFragment();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void refreshCurrentFragment() {
        // Get current fragment position
        int currentPosition = viewPager2.getCurrentItem();
        
        // Refresh the appropriate fragment
        if (currentPosition == 0) {
            // Home fragment
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager2.getId() + ":" + currentPosition);
            if (homeFragment != null) {
                homeFragment.refreshData();
            }
        } else if (currentPosition == 1) {
            // Expenses fragment
            ExpensesFragment expensesFragment = (ExpensesFragment) getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager2.getId() + ":" + currentPosition);
            if (expensesFragment != null) {
                expensesFragment.loadExpenses();
            }
        } else if (currentPosition == 2) {
            // Budget fragment
            BudgetFragment budgetFragment = (BudgetFragment) getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager2.getId() + ":" + currentPosition);
            if (budgetFragment != null) {
                budgetFragment.loadBudgetCategories();
            }
        }
    }
}
