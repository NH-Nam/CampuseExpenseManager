package com.example.campuseexpensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.R;
import com.example.campuseexpensemanager.model.Budgets;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DecimalFormat;
import java.util.List;

public class BudgetCategoryAdapter extends RecyclerView.Adapter<BudgetCategoryAdapter.BudgetCategoryViewHolder> {

    private List<Budgets> budgetCategories;
    private OnBudgetCategoryClickListener listener;
    private DecimalFormat decimalFormat = new DecimalFormat("$#,##0.00");

    public interface OnBudgetCategoryClickListener {
        void onEditClick(Budgets budgetCategory);
        void onDeleteClick(Budgets budgetCategory);
    }

    public BudgetCategoryAdapter(List<Budgets> budgetCategories, OnBudgetCategoryClickListener listener) {
        this.budgetCategories = budgetCategories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_category, parent, false);
        return new BudgetCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetCategoryViewHolder holder, int position) {
        Budgets budgetCategory = budgetCategories.get(position);
        holder.bind(budgetCategory);
    }

    @Override
    public int getItemCount() {
        return budgetCategories.size();
    }

    public void updateBudgetCategories(List<Budgets> newBudgetCategories) {
        this.budgetCategories = newBudgetCategories;
        notifyDataSetChanged();
    }

    class BudgetCategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCategoryIcon;
        TextView tvSpentAmount, tvRemainingAmount, tvBudgetAmount, tvCategoryName;
        LinearProgressIndicator progressCategory;
        MaterialButton btnEdit, btnDelete;

        public BudgetCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvSpentAmount = itemView.findViewById(R.id.tvSpentAmount);
            tvRemainingAmount = itemView.findViewById(R.id.tvRemainingAmount);
            progressCategory = itemView.findViewById(R.id.progressCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(budgetCategories.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(budgetCategories.get(position));
                }
            });
        }

        public void bind(Budgets budgetCategory) {
            tvCategoryName.setText(budgetCategory.getCategory());
            tvBudgetAmount.setText(decimalFormat.format(budgetCategory.getMoney()));
            
            // Use the spentAmount field from the Budgets class
            double spentAmount = budgetCategory.getSpentAmount();
            double remainingAmount = budgetCategory.getMoney() - spentAmount;
            
            tvSpentAmount.setText("Spent: " + decimalFormat.format(spentAmount));
            tvRemainingAmount.setText("Remaining: " + decimalFormat.format(remainingAmount));
            
            // Update progress indicator
            int progressPercentage = budgetCategory.getMoney() > 0 ? 
                    (int) ((spentAmount / budgetCategory.getMoney()) * 100) : 0;
            progressCategory.setProgress(progressPercentage);
            
            // Set category icon based on category name
            setCategoryIcon(budgetCategory.getCategory());
        }
        
        private void setCategoryIcon(String category) {
            // Set appropriate icon based on category
            int iconResource = R.drawable.category_24dp;
            
            if (category != null) {
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
            }
            
            ivCategoryIcon.setImageResource(iconResource);
        }
    }
} 