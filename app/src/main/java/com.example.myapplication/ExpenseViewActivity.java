package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExpenseViewActivity extends AppCompatActivity {
    TextView tvtotalspend;
    ExpenseView circleChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expensesanalyze);

        tvtotalspend = findViewById(R.id.totalSpending);
        circleChart = findViewById(R.id.circleChart);
        dbHelper = new DatabaseHelper(this);

        // Retrieve category spending data
        ArrayList<DatabaseHelper.CategorySpending> categorySpendingList = dbHelper.getCategorySpending();
        // Retrieve category budget data
        ArrayList<DatabaseHelper.CategoryBudget> categoryBudgetList = dbHelper.getCategoryBudgets();

        // Prepare a map for quick budget lookup
        HashMap<String, Float> budgetMap = new HashMap<>();
        for (DatabaseHelper.CategoryBudget budget : categoryBudgetList) {
            budgetMap.put(budget.category, budget.budgetAmount);
        }

        // Calculate total spending and prepare data for chart
        float totalSpending = 0;
        List<Float> percentages = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (DatabaseHelper.CategorySpending category : categorySpendingList) {
            totalSpending += category.total;
            percentages.add(category.total);
            colors.add(getColorForCategory(category.category));

            // Calculate budget comparison
            float budgetAmount = budgetMap.getOrDefault(category.category, 0f);
            float budgetLeft = budgetAmount - category.total;

            // Display budget comparison
            String analysis = "Category: " + category.category + "\n" +
                    "Spent: $" + category.total + "\n" +
                    "Budget: $" + budgetAmount + "\n" +
                    "Budget Left: $" + budgetLeft + "\n";
            // You can display this analysis in a TextView or log it
            System.out.println(analysis);
        }

        // Update the total spending text
        totalSpendingTextView.setText("Total Spending: $" + totalSpending);

        // Set data for the chart
        circleChart.setData(percentages, colors);
    }

    // Method to assign colors based on category
    private int getColorForCategory(String category) {
        switch (category) {
            case "Food":
                return Color.RED;
            case "Transport":
                return Color.BLUE;
            case "Entertainment":
                return Color.GREEN;
        }
    }
}

