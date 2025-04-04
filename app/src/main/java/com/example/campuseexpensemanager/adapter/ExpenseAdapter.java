package com.example.campuseexpensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private List<Expenses> expensesList;
    private OnExpenseEditListener editListener;
    private OnExpenseDeleteListener deleteListener;

    public interface OnExpenseEditListener {
        void onEdit(Expenses expense);
    }

    public interface OnExpenseDeleteListener {
        void onDelete(Expenses expense);
    }

    public ExpenseAdapter(List<Expenses> expensesList, OnExpenseEditListener editListener, OnExpenseDeleteListener deleteListener) {
        this.expensesList = expensesList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expenses expense = expensesList.get(position);
        holder.tvName.setText(expense.getName());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", expense.getMoney()));
        holder.tvCategory.setText(expense.getCategory());
        holder.tvDescription.setText(expense.getDescription());
        
        // Set category icon based on category
        setCategoryIcon(holder.ivCategoryIcon, expense.getCategory());
        
        // Format and display the date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String date = outputFormat.format(inputFormat.parse(expense.getCreatedAt()));
            holder.tvDate.setText(date);
        } catch (Exception e) {
            holder.tvDate.setText(expense.getCreatedAt());
        }

        holder.btnEdit.setOnClickListener(v -> editListener.onEdit(expense));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(expense));
    }

    private void setCategoryIcon(ImageView imageView, String category) {
        int iconResource;
        switch (category.toLowerCase()) {
            case "food":
                iconResource = R.drawable.local_dining_24dp;
                break;
            case "transportation":
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
            case "education":
                iconResource = R.drawable.school_24dp;
                break;
            case "health":
                iconResource = R.drawable.local_hospital_24dp;
                break;
            default:
                iconResource = R.drawable.category_24dp;
                break;
        }
        imageView.setImageResource(iconResource);
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvCategory, tvDescription, tvDate;
        ImageView ivCategoryIcon;
        ImageButton btnEdit, btnDelete;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExpenseName);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            ivCategoryIcon = itemView.findViewById(R.id.ivExpenseCategoryIcon);
            btnEdit = itemView.findViewById(R.id.btnEditExpense);
            btnDelete = itemView.findViewById(R.id.btnDeleteExpense);
        }
    }
} 