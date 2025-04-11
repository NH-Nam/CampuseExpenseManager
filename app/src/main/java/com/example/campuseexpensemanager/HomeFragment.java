package com.example.campuseexpensemanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campuseexpensemanager.adapter.CategoryBreakdownAdapter;
import com.example.campuseexpensemanager.adapter.ExpenseAdapter;
import com.example.campuseexpensemanager.adapter.NotificationAdapter;
import com.example.campuseexpensemanager.database.BudgetDb;
import com.example.campuseexpensemanager.database.ExpenseDb;
import com.example.campuseexpensemanager.database.CategoryDb;
import com.example.campuseexpensemanager.model.Budgets;
import com.example.campuseexpensemanager.model.Expenses;
import com.example.campuseexpensemanager.model.Notification;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.SharedPreferences;
import android.content.Context;

public class HomeFragment extends Fragment {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    TextView tvGreeting, tvBudgetStatus, tvBudgetRemaining, tvExpenseOverviewTotalSpent;
    ImageView ivProfile;
    ProgressBar pbBudgetProgress;
    RecyclerView rvRecentExpenses, rvNotifications, rvCategoryBreakdown;
    Button btnViewAllExpenses;
    CardView cardView;
    PieChart pieChart, pieChartSpent;
    List<Notification> notifications = new ArrayList<>();
    NotificationAdapter notificationAdapter;
    CategoryBreakdownAdapter categoryBreakdownAdapter;
    ExpenseDb expenseDb;
    BudgetDb budgetDb;
    private CategoryDb categoryDb;

    // Data
    List<Expenses> expenses = new ArrayList<>();
    double totalBudget = 0.0;
    double totalSpent = 0.0;
    Map<String, Double> categoryBudgets = new HashMap<>();

    // Notification channel ID
    private static final String CHANNEL_ID = "budget_alerts";
    private static final int NOTIFICATION_ID = 1001;
    private static final String PREF_NAME = "budget_notifications";
    private boolean isRefreshing = false;
    private SharedPreferences sharedPreferences;

    // Store callback as a field
    private Runnable dataChangeCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        tvExpenseOverviewTotalSpent = view.findViewById(R.id.tvExpenseOverviewTotalSpent);
        pbBudgetProgress = view.findViewById(R.id.pbBudgetProgress);
        rvRecentExpenses = view.findViewById(R.id.rvRecentExpenses);
        rvCategoryBreakdown = view.findViewById(R.id.rvCategoryBreakdown);
        btnViewAllExpenses = view.findViewById(R.id.btnViewAllExpenses);
        cardView = view.findViewById(R.id.cardView);
        pieChart = view.findViewById(R.id.pieChart);
        pieChartSpent = view.findViewById(R.id.pieChartSpent);
        rvNotifications = view.findViewById(R.id.rvNotifications);

        // Initialize database
        expenseDb = new ExpenseDb(requireContext());
        budgetDb = new BudgetDb(requireContext());
        categoryDb = new CategoryDb(requireContext());
        
        // Create callback once and store it
        dataChangeCallback = () -> {
            if (getActivity() != null && !isRefreshing) {
                getActivity().runOnUiThread(this::refreshData);
            }
        };
        
        // Register the callbacks
        expenseDb.addOnDataChangedCallback(dataChangeCallback);
        categoryDb.addOnDataChangedCallback(dataChangeCallback);

        // Get username from arguments
        String username = "User";
        if (getArguments() != null) {
            username = getArguments().getString("USERNAME", "User");
        }
        tvGreeting.setText("Hello, " + username);

        // Setup notifications
        setupNotifications();
        
        // Setup RecyclerViews
        loadExpenses();
        setupRecentExpensesRecyclerView();
        setupCategoryBreakdown();
        setupBudgetChart();

        // Set up listeners
        setupListeners();

        // Update UI
        updateUI();

        //Create channel
        createNotificationChannel();

        // Request notification permission if needed
        requestNotificationPermission();

        return view;
    }

    private void loadExpenses() {
        expenses.clear();
        expenses.addAll(expenseDb.getAllExpenses());
    }

    private void setupCategoryBreakdown() {
        categoryBreakdownAdapter = new CategoryBreakdownAdapter(totalSpent);
        rvCategoryBreakdown.setAdapter(categoryBreakdownAdapter);
        rvCategoryBreakdown.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateCategoryBreakdown() {
        Map<String, Double> categoryTotals = new HashMap<>();
        
        // Calculate total spent by category
        for (Expenses expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getMoney();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        // Sort categories by amount spent (descending)
        List<Map.Entry<String, Double>> sortedCategories = categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        categoryBreakdownAdapter.updateCategories(sortedCategories, totalSpent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Notifications";
            String description = "Channel for budget-related notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupRecentExpensesRecyclerView() {
        // Get only the 5 most recent expenses
        List<Expenses> recentExpenses = new ArrayList<>();
        int count = Math.min(expenses.size(), 5);
        for (int i = 0; i < count; i++) {
            recentExpenses.add(expenses.get(i));
        }
        
        ExpenseAdapter adapter = new ExpenseAdapter(
            recentExpenses,
            expense -> {
                // Navigate to ExpensesFragment and scroll to this expense
                ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(1); // Switch to Expenses tab
                
                // We can't directly scroll to the item in ExpensesFragment from here
                // The ExpensesFragment will need to handle this when it becomes visible
                Toast.makeText(getContext(), "Edit expense: " + expense.getName(), Toast.LENGTH_SHORT).show();
            },
            expense -> {
                // Show delete confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        expenseDb.deleteExpense(expense.getId());
                        loadExpenses();
                        updateUI();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        );
        rvRecentExpenses.setAdapter(adapter);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        btnViewAllExpenses.setOnClickListener(v -> {
            // Navigate to ExpensesFragment
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            viewPager.setCurrentItem(1); // Switch to Expenses tab
        });

        ivProfile.setOnClickListener(v -> {
            // TODO: Navigate to profile screen
        });
    }

    private void updateUI() {
        // Load budget categories for current month
        List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
        
        // Calculate total budget and spent
        totalBudget = 0.0;
        totalSpent = 0.0;
        categoryBudgets.clear();
        
        // Calculate total spent from expenses
        for (Expenses expense : expenses) {
            totalSpent += expense.getMoney();
        }
        
        // Calculate total budget from budget categories
        for (Budgets budget : budgetCategories) {
            totalBudget += budget.getMoney();
            categoryBudgets.put(budget.getName(), budget.getMoney());
        }

        android.util.Log.d("HomeFragment", "Total spent calculated from expenses: $" + totalSpent);

        // Update UI with proper text formatting
        tvBudgetStatus.setText(String.format("Budget: $%.2f", totalBudget));
        tvExpenseOverviewTotalSpent.setText(String.format("Total Spent: $%.2f", totalSpent));
        tvBudgetRemaining.setText(String.format("Remaining: $%.2f", (totalBudget - totalSpent)));
        
        if (totalBudget > 0) {
            pbBudgetProgress.setProgress((int) ((totalSpent / totalBudget) * 100));
        } else {
            pbBudgetProgress.setProgress(0);
        }

        // Update category breakdown
        updateCategoryBreakdown();
        
        // Update recent expenses
        setupRecentExpensesRecyclerView();

        // Check for budget warnings
        checkBudgetWarning();
    }

    private void setupNotifications() {
        notificationAdapter = new NotificationAdapter(notifications);
        rvNotifications.setAdapter(notificationAdapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupBudgetChart() {
        // Setup budget pie chart
        List<PieEntry> budgetEntries = new ArrayList<>();
        List<PieEntry> spentEntries = new ArrayList<>();
        
        // Get budget categories for current month
        List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
        
        // Calculate spent amounts per category from expenses
        Map<String, Double> categorySpentAmounts = new HashMap<>();
        for (Expenses expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getMoney();
            categorySpentAmounts.put(category, 
                categorySpentAmounts.getOrDefault(category, 0.0) + amount);
        }
        
        // Add budget categories with their amounts
        for (Budgets budget : budgetCategories) {
            if (budget.getMoney() > 0) {
                // Add the budget amount
                budgetEntries.add(new PieEntry((float) budget.getMoney(), budget.getCategory()));
                
                // Add the spent amount if any expenses exist for this category
                double spentAmount = categorySpentAmounts.getOrDefault(budget.getCategory(), 0.0);
                if (spentAmount > 0) {
                    spentEntries.add(new PieEntry((float) spentAmount, budget.getCategory()));
                }
            }
        }

        // Create a dataset for the budget amounts
        PieDataSet budgetDataSet = new PieDataSet(budgetEntries, "Budget Distribution");
        budgetDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        budgetDataSet.setValueTextSize(12f);
        budgetDataSet.setValueTextColor(Color.WHITE);
        
        // Create a legend for the budget pie chart
        Legend budgetLegend = pieChart.getLegend();
        budgetLegend.setEnabled(true);
        budgetLegend.setTextSize(12f);
        budgetLegend.setForm(Legend.LegendForm.CIRCLE);
        budgetLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        
        PieData budgetData = new PieData(budgetDataSet);
        budgetData.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("$%.0f", value);
            }
        });
        
        pieChart.setData(budgetData);
        pieChart.setDescription(null);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(1000);
        pieChart.invalidate();
        
        // Create a dataset for the spent amounts
        PieDataSet spentDataSet = new PieDataSet(spentEntries, "Spent Distribution");
        spentDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        spentDataSet.setValueTextSize(12f);
        spentDataSet.setValueTextColor(Color.WHITE);
        
        // Create a legend for the spent pie chart
        Legend spentLegend = pieChartSpent.getLegend();
        spentLegend.setEnabled(true);
        spentLegend.setTextSize(12f);
        spentLegend.setForm(Legend.LegendForm.CIRCLE);
        spentLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        
        PieData spentData = new PieData(spentDataSet);
        spentData.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("$%.0f", value);
            }
        });
        
        pieChartSpent.setData(spentData);
        pieChartSpent.setDescription(null);
        pieChartSpent.setHoleRadius(40f);
        pieChartSpent.setTransparentCircleRadius(45f);
        pieChartSpent.setEntryLabelColor(Color.WHITE);
        pieChartSpent.setEntryLabelTextSize(12f);
        pieChartSpent.animateY(1000);
        pieChartSpent.invalidate();
    }

    private String getLastWarningKey(String category) {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        return String.format("%s_%s_last_warning", category, currentMonth);
    }

    private boolean hasShownWarningThisMonth(String category) {
        return sharedPreferences.getBoolean(getLastWarningKey(category), false);
    }

    private void markWarningShown(String category) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getLastWarningKey(category), true);
        editor.apply();
    }

    private void checkBudgetWarning() {
        if (isRefreshing) {
            return;
        }

        double percentageSpent = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;
        
        // Check overall budget
        if (percentageSpent >= 80 && !hasShownWarningThisMonth("Overall")) {
            showBudgetWarningNotification("Overall Budget", percentageSpent);
            markWarningShown("Overall");
        }

        // Check category budgets for current month
        List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
        for (Budgets budget : budgetCategories) {
            double budgetAmount = budget.getMoney();
            double spentAmount = budget.getSpentAmount();
            
            if (budgetAmount > 0) {
                double categoryPercentage = (spentAmount / budgetAmount) * 100;
                if (categoryPercentage >= 80 && !hasShownWarningThisMonth(budget.getName())) {
                    showBudgetWarningNotification(budget.getName(), categoryPercentage);
                    markWarningShown(budget.getName());
                }
            }
        }
    }

    private void showBudgetWarningNotification(String category, double percentage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning_24dp)
                .setContentTitle("Budget Warning: " + category)
                .setContentText(String.format("You've spent %.0f%% of your %s budget", percentage, category))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            // Use a unique ID for each category by combining base ID with category hashcode
            int uniqueId = NOTIFICATION_ID + Math.abs(category.hashCode());
            notificationManager.notify(uniqueId, builder.build());
            
            // Add to notifications list
            notifications.add(0, new Notification(
                String.valueOf(uniqueId),
                "Budget Warning: " + category,
                String.format("You've spent %.0f%% of your %s budget", percentage, category),
                R.drawable.warning_24dp
            ));
            
            // Only update the adapter if it's not null
            if (notificationAdapter != null) {
                notificationAdapter.updateNotifications(notifications);
            }
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), 
                    "Cannot show notification: permission denied", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove callbacks to prevent memory leaks
        if (expenseDb != null && dataChangeCallback != null) {
            expenseDb.removeOnDataChangedCallback(dataChangeCallback);
        }
        if (categoryDb != null && dataChangeCallback != null) {
            categoryDb.removeOnDataChangedCallback(dataChangeCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
        updateUI();
        setupBudgetChart();
    }
    
    /**
     * Refreshes all data in the HomeFragment
     * This method is called from MenuActivity when a new expense is added
     */
    public void refreshData() {
        if (getActivity() != null && isAdded() && !isRefreshing) {
            android.util.Log.d("HomeFragment", "Refreshing data");
            isRefreshing = true;
            
            try {
                // Load expenses
                loadExpenses();
                
                // Get budget categories for current month
                List<Budgets> budgetCategories = budgetDb.getBudgetCategoriesByMonth(budgetDb.getCurrentMonth());
                
                // Calculate total budget and spent
                totalBudget = 0.0;
                totalSpent = 0.0;
                categoryBudgets.clear();
                
                // Calculate total spent from expenses
                for (Expenses expense : expenses) {
                    totalSpent += expense.getMoney();
                }
                
                // Calculate total budget from budget categories
                for (Budgets budget : budgetCategories) {
                    totalBudget += budget.getMoney();
                    categoryBudgets.put(budget.getName(), budget.getMoney());
                }

                android.util.Log.d("HomeFragment", "Total spent calculated from expenses: $" + totalSpent);
                
                // Update UI on main thread
                getActivity().runOnUiThread(() -> {
                    // Update UI with proper text formatting
                    tvBudgetStatus.setText(String.format("Budget: $%.2f", totalBudget));
                    tvExpenseOverviewTotalSpent.setText(String.format("Total Spent: $%.2f", totalSpent));
                    tvBudgetRemaining.setText(String.format("Remaining: $%.2f", (totalBudget - totalSpent)));
                    
                    if (totalBudget > 0) {
                        pbBudgetProgress.setProgress((int) ((totalSpent / totalBudget) * 100));
                    } else {
                        pbBudgetProgress.setProgress(0);
                    }
                    
                    // Update pie charts
                    setupBudgetChart();
                    
                    // Update category breakdown
                    updateCategoryBreakdown();
                    
                    // Setup recent expenses
                    setupRecentExpensesRecyclerView();
                    
                    // Check for budget warnings
                    checkBudgetWarning();
                });
            } finally {
                isRefreshing = false;
            }
            
            android.util.Log.d("HomeFragment", "Data refresh completed");
        } else {
            android.util.Log.d("HomeFragment", "Cannot refresh data: Activity is null or fragment not added");
        }
    }
}