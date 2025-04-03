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

public class HomeFragment extends Fragment {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    TextView tvGreeting, tvTotalSpent, tvBudgetStatus, tvBudgetRemaining;
    ImageView ivProfile;
    ProgressBar pbBudgetProgress;
    RecyclerView rvRecentExpenses, rvNotifications;
    Button btnViewAllExpenses;
    CardView cardView;
    PieChart pieChart;
    List<Notification> notifications = new ArrayList<>();
    NotificationAdapter notificationAdapter;

    // Data
    List<Expenses> expenses = new ArrayList<>();
    private float totalBudget = 1000f;
    private float totalSpent = 0f;

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

        // Setup RecyclerViews
        setupDummyData();
        setupRecentExpensesRecyclerView();
        setupBudgetChart();

        // Setup notifications
        setupNotifications();

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

    private void setupDummyData() {
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

        // Check for budget warning
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
        
        if (percentageSpent >= 80) {
            showBudgetWarningNotification();
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

    private void showBudgetWarningNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning_24dp)
                .setContentTitle("Budget Warning")
                .setContentText("You've spent " + String.format("%.0f", (totalSpent / totalBudget) * 100) + "% of your budget")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(notificationId++, builder.build());
            
            // Add to notifications list
            notifications.add(0, new Notification(
                String.valueOf(notificationId),
                "Budget Warning",
                "You've spent " + String.format("%.0f", (totalSpent / totalBudget) * 100) + "% of your budget",
                R.drawable.warning_24dp
            ));
            notificationAdapter.updateNotifications(notifications);
        } catch (SecurityException e) {
            // Handle the case where permission was revoked
            Toast.makeText(requireContext(), 
                    "Cannot show notification: permission denied", 
                    Toast.LENGTH_SHORT).show();
        }
    }
}