package com.example.campuseexpensemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.campuseexpensemanager.R;
import com.example.campuseexpensemanager.model.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    private final Context context;
    private final List<Category> categories;

    public CategorySpinnerAdapter(Context context, List<Category> categories) {
        super(context, R.layout.item_category_dropdown, categories);
        this.context = context;
        this.categories = categories;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_dropdown, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textCategory);
        Category category = categories.get(position);
        textView.setText(category.getName());

        return convertView;
    }
} 