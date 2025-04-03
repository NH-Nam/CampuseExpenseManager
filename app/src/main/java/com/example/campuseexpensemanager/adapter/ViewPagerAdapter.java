package com.example.campuseexpensemanager.adapter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campuseexpensemanager.BudgetFragment;
import com.example.campuseexpensemanager.ExpensesFragment;
import com.example.campuseexpensemanager.HomeFragment;
import com.example.campuseexpensemanager.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private String username;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String username) {
        super(fragmentManager, lifecycle);
        this.username = username;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString("USERNAME", username);
            homeFragment.setArguments(args);
            return homeFragment;
        } else if (position == 1) {
            return new ExpensesFragment();
        } else if (position == 2) {
            return new BudgetFragment();
        } else if (position == 3) {
            return new SettingFragment();
        } else {
            return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
