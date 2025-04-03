package com.example.campuseexpensemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseexpensemanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.ViewHolder> {
    private List<Map.Entry<String, Double>> categories = new ArrayList<>();
    private double totalSpent;

    public CategoryBreakdownAdapter(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_breakdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> category = categories.get(position);
        holder.tvCategory.setText(category.getKey());
        holder.tvCategoryAmount.setText(String.format("$%.2f", category.getValue()));
        double percentage = (category.getValue() / totalSpent) * 100;
        holder.tvCategoryPercentage.setText(String.format("%.1f%%", percentage));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Map.Entry<String, Double>> categories, double totalSpent) {
        this.categories = categories;
        this.totalSpent = totalSpent;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvCategoryAmount, tvCategoryPercentage;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
            tvCategoryPercentage = itemView.findViewById(R.id.tvCategoryPercentage);
        }
    }
} 