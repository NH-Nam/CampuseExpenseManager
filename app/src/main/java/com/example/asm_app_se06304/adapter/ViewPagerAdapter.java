package com.example.asm_app_se06304.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.asm_app_se06304.BudgetFragment;
import com.example.asm_app_se06304.ExpensesFragment;
import com.example.asm_app_se06304.HomeFragment;
import com.example.asm_app_se06304.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0 ){
            return new HomeFragment();
        }else if (position == 1){
            return new BudgetFragment();
        }else if (position == 2){
            return new ExpensesFragment();
        }else if (position == 3){
            return new SettingFragment();
        }else
        {
            return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
