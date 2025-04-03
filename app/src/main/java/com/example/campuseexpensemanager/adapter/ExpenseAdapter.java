package com.example.campuseexpensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.R;
import com.example.campuseexpensemanager.model.Expenses;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expenses> expenses;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ExpenseAdapter(List<Expenses> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expenses expense = expenses.get(position);
        holder.tvExpenseName.setText(expense.getName());
        holder.tvExpenseAmount.setText(String.format("$%.2f", expense.getMoney()));
        
        // Format the date
        try {
            String formattedDate = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(expense.getCreatedAt()));
            holder.tvExpenseDate.setText(formattedDate);
        } catch (Exception e) {
            holder.tvExpenseDate.setText(expense.getCreatedAt());
        }

        // Set category icon based on category
        setCategoryIcon(holder.ivExpenseCategoryIcon, expense.getCategory());
    }

    private void setCategoryIcon(ImageView imageView, String category) {
        int iconResource;
        switch (category.toLowerCase()) {
            case "food":
                iconResource = R.drawable.local_dining_24dp;
                break;
            case "transport":
                iconResource = R.drawable.directions_car_24dp;
                break;
            case "entertainment":
                iconResource = R.drawable.stadia_controller_24dp;
                break;
            case "shopping":
                iconResource = R.drawable.shopping_cart_24dp;
                break;
            case "bills":
                iconResource = R.drawable.paid_24dp;
                break;
            default:
                iconResource = R.drawable.category_24dp;
                break;
        }
        imageView.setImageResource(iconResource);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expenses> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExpenseCategoryIcon;
        TextView tvExpenseName;
        TextView tvExpenseDate;
        TextView tvExpenseAmount;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExpenseCategoryIcon = itemView.findViewById(R.id.ivExpenseCategoryIcon);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
        }
    }
} 