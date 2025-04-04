package com.example.campuseexpensemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.R;
import com.example.campuseexpensemanager.model.Categories;

import java.util.ArrayList;
import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Categories> implements Filterable {
    private final Context context;
    private final List<Categories> categories;
    private List<Categories> filteredCategories;

    public CategorySpinnerAdapter(Context context, List<Categories> categories) {
        super(context, 0, categories);
        this.context = context;
        this.categories = categories;
        this.filteredCategories = new ArrayList<>(categories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_spinner, parent, false);
        }

        Categories category = getItem(position);
        if (category != null) {
            ImageView ivCategoryIcon = convertView.findViewById(R.id.ivCategoryIcon);
            TextView tvCategoryName = convertView.findViewById(R.id.tvCategoryName);

            tvCategoryName.setText(category.getDisplayName());
            setCategoryIcon(ivCategoryIcon, category.getDisplayName());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredCategories.size();
    }

    @Nullable
    @Override
    public Categories getItem(int position) {
        return filteredCategories.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Categories> filteredList = new ArrayList<>();
                
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(categories);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Categories category : categories) {
                        if (category.getDisplayName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(category);
                        }
                    }
                }
                
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredCategories = (List<Categories>) results.values;
                notifyDataSetChanged();
            }
        };
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
} 