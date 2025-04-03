package com.example.asm_app_se06304.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asm_app_se06304.R;
import com.example.asm_app_se06304.model.Budget;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgets;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Budget budget);
    }

    public BudgetAdapter(List<Budget> budgets, OnItemClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.tvDescription.setText(budget.getDescription());
        holder.tvAmount.setText(String.format("%,.2f VND", budget.getAmount()));
        holder.tvDate.setText(budget.getDate());
        holder.tvCategory.setText(budget.getBudgetId());
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(budget));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateBudgets(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate, tvCategory;
        Button btnEdit;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_Budgetdescription);
            tvAmount = itemView.findViewById(R.id.tv_Budgetamount);
            tvDate = itemView.findViewById(R.id.tv_Budgetdate);
            tvCategory = itemView.findViewById(R.id.tv_Budgetcategory);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}