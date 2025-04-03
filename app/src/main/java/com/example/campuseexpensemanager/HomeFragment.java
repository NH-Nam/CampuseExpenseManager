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

import com.example.campuseexpensemanager.adapter.CategoryBreakdownAdapter;
import com.example.campuseexpensemanager.adapter.ExpenseAdapter;
import com.example.campuseexpensemanager.adapter.NotificationAdapter;
import com.example.campuseexpensemanager.model.Expenses;
import com.example.campuseexpensemanager.model.Notification;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    TextView tvGreeting, tvTotalSpent, tvBudgetStatus, tvBudgetRemaining;
    ImageView ivProfile;
    ProgressBar pbBudgetProgress;
    RecyclerView rvRecentExpenses, rvNotifications, rvCategoryBreakdown;
    Button btnViewAllExpenses;
    CardView cardView;
    PieChart pieChart;
    List<Notification> notifications = new ArrayList<>();
    NotificationAdapter notificationAdapter;
    CategoryBreakdownAdapter categoryBreakdownAdapter;

    // Data
    List<Expenses> expenses = new ArrayList<>();
    float totalBudget = 1000f;
    float totalSpent = 0f;
    Map<String, Float> categoryBudgets = new HashMap<>();

    // Notification channel ID
    private static final String CHANNEL_ID = "budget_notifications";
    private int notificationId = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        pbBudgetProgress = view.findViewById(R.id.pbBudgetProgress);
        rvRecentExpenses = view.findViewById(R.id.rvRecentExpenses);
        rvCategoryBreakdown = view.findViewById(R.id.rvCategoryBreakdown);
        btnViewAllExpenses = view.findViewById(R.id.btnViewAllExpenses);
        cardView = view.findViewById(R.id.cardView);
        pieChart = view.findViewById(R.id.pieChart);
        rvNotifications = view.findViewById(R.id.rvNotifications);

        // Get username from arguments
        String username = "User";
        if (getArguments() != null) {
            username = getArguments().getString("USERNAME", "User");
        }
        tvGreeting.setText("Hello, " + username);

        // Setup notifications first to avoid null pointer exceptions
        setupNotifications();
        
        // Setup RecyclerViews
//        setupDummyData();
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

//    private void setupDummyData() {
//        // Add some dummy expenses for testing
//        Expenses expense1 = new Expenses();
//        expense1.setId(1);
//        expense1.setName("Lunch");
//        expense1.setMoney(250.0);
//        expense1.setCategory("Food");
//        expense1.setCreatedAt("2024-03-15");
//        expenses.add(expense1);
//
//        Expenses expense2 = new Expenses();
//        expense2.setId(2);
//        expense2.setName("Bus Fare");
//        expense2.setMoney(180.0);
//        expense2.setCategory("Transport");
//        expense2.setCreatedAt("2024-03-14");
//        expenses.add(expense2);
//
//        Expenses expense3 = new Expenses();
//        expense3.setId(3);
//        expense3.setName("Movie Ticket");
//        expense3.setMoney(100.0);
//        expense3.setCategory("Entertainment");
//        expense3.setCreatedAt("2024-03-13");
//        expenses.add(expense3);
//
//        Expenses expense4 = new Expenses();
//        expense4.setId(4);
//        expense4.setName("Dinner");
//        expense4.setMoney(75.0);
//        expense4.setCategory("Food");
//        expense4.setCreatedAt("2024-03-12");
//        expenses.add(expense4);
//
//        // Set category budgets
//        categoryBudgets.put("Food", 300f);
//        categoryBudgets.put("Transport", 200f);
//        categoryBudgets.put("Entertainment", 200f);
//        categoryBudgets.put("Shopping", 300f);
//    }

    private void setupCategoryBreakdown() {
        categoryBreakdownAdapter = new CategoryBreakdownAdapter(totalSpent);
        rvCategoryBreakdown.setAdapter(categoryBreakdownAdapter);
        rvCategoryBreakdown.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateCategoryBreakdown() {
        Map<String, Float> categoryTotals = new HashMap<>();
        
        // Calculate total spent by category
        for (Expenses expense : expenses) {
            String category = expense.getCategory();
            float amount = (float) expense.getMoney();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }

        // Sort categories by amount spent (descending)
        List<Map.Entry<String, Float>> sortedCategories = categoryTotals.entrySet().stream()
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
        ExpenseAdapter adapter = new ExpenseAdapter(expenses);
        rvRecentExpenses.setAdapter(adapter);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        btnViewAllExpenses.setOnClickListener(v -> {
            // TODO: Navigate to view all expenses screen
        });

        ivProfile.setOnClickListener(v -> {
            // TODO: Navigate to profile screen
        });
    }

    private void updateUI() {
        // Calculate total spent
        totalSpent = 0f;
        for (Expenses expense : expenses) {
            totalSpent += expense.getMoney();
        }

        tvTotalSpent.setText("$" + String.format("%.2f", totalSpent));
        tvBudgetStatus.setText("Budget: $" + String.format("%.2f", totalBudget));
        tvBudgetRemaining.setText("Remaining: $" + String.format("%.2f", (totalBudget - totalSpent)));
        pbBudgetProgress.setProgress((int) ((totalSpent / totalBudget) * 100));

        // Update category breakdown
        updateCategoryBreakdown();

        // Check for budget warnings
        checkBudgetWarning();
    }

    private void setupNotifications() {
        notificationAdapter = new NotificationAdapter(notifications);
        rvNotifications.setAdapter(notificationAdapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Add sample notifications
        addSampleNotifications();
    }

    private void addSampleNotifications() {
        notifications.add(new Notification(
            "1",
            "Budget Warning",
            "You've spent 80% of your monthly budget",
            R.drawable.warning_24dp
        ));
        
        notificationAdapter.updateNotifications(notifications);
    }

    private void setupBudgetChart() {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Float> categoryTotals = new HashMap<>();
        
        // Calculate total spent by category
        for (Expenses expense : expenses) {
            String category = expense.getCategory();
            float amount = (float) expense.getMoney();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }
        
        // Add category totals to pie chart
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        // Add remaining budget
        float remaining = totalBudget - totalSpent;
        if (remaining > 0) {
            entries.add(new PieEntry(remaining, "Remaining"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Budget Distribution");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDescription(null);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(1000);
        pieChart.invalidate();
        
        // Check for budget warnings
        checkBudgetWarning();
    }

    private void checkBudgetWarning() {
        float percentageSpent = (totalSpent / totalBudget) * 100;
        
        // Check overall budget
        if (percentageSpent >= 80) {
            showBudgetWarningNotification("Overall Budget", percentageSpent);
        }

        // Check category budgets
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Expenses expense : expenses) {
            String category = expense.getCategory();
            float amount = (float) expense.getMoney();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }

        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            float spent = entry.getValue();
            float budget = categoryBudgets.getOrDefault(category, 0f);
            
            if (budget > 0) {
                float categoryPercentage = (spent / budget) * 100;
                if (categoryPercentage >= 80) {
                    showBudgetWarningNotification(category, categoryPercentage);
                }
            }
        }
    }

    private void showBudgetWarningNotification(String category, float percentage) {
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
            notificationManager.notify(notificationId++, builder.build());
            
            // Add to notifications list
            notifications.add(0, new Notification(
                String.valueOf(notificationId),
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, create notification channel
                createNotificationChannel();
            } else {
                Toast.makeText(requireContext(), 
                        "Notification permission is required for budget alerts", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}