package com.example.asm_app_se06304;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.adapter.ExpenseCategoryAdapter;
import com.example.asm_app_se06304.model.ExpenseCategory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listExpenseCategories;
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvTotalBudget, tvTotalSpent, tvRemainingBudget, tvBudgetPercentage, tvSeeAll;
    private DatabaseContext db;
    private int userId = 1;
    private ExpenseCategoryAdapter adapter;
    private List<ExpenseCategory> expenseCategories;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseContext(requireActivity());
        expenseCategories = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listExpenseCategories = view.findViewById(R.id.list_expense_categories);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvRemainingBudget = view.findViewById(R.id.tv_remaining_budget);
        tvBudgetPercentage = view.findViewById(R.id.tv_budget_percentage);
        tvSeeAll = view.findViewById(R.id.see_all_button); // Thêm dòng này

        adapter = new ExpenseCategoryAdapter(requireActivity(), expenseCategories);
        listExpenseCategories.setAdapter(adapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getMonths());
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, getYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(getYears().indexOf(String.valueOf(calendar.get(Calendar.YEAR))));

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getBoolean("expense_added", false) || result.getBoolean("budget_added", false)) {
                    loadExpenses();
                }
            }
        });

        // Xử lý sự kiện nhấn "See All"
        tvSeeAll.setOnClickListener(v -> {
            TransactionListFragment fragment = new TransactionListFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadExpenses();
        return view;
    }

    private List<String> getMonths() {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        return months;
    }

    private List<String> getYears() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    private void loadExpenses() {
        int month = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        expenseCategories = db.getExpensesByMonth(userId, month, year);
        adapter.updateCategories(expenseCategories);

        double totalSpent = 0;
        for (ExpenseCategory category : expenseCategories) {
            totalSpent += category.getTotalAmount();
        }

        double totalBudget = db.getTotalBudget(userId);
        double remainingBudget = totalBudget - totalSpent;
        double budgetPercentage = totalBudget == 0 ? 0 : (totalSpent / totalBudget) * 100;

        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        tvTotalBudget.setText("Tổng ngân sách: " + formatter.format(totalBudget));
        tvTotalSpent.setText("Tổng chi tiêu: " + formatter.format(totalSpent));
        tvRemainingBudget.setText("Ngân sách còn lại: " + formatter.format(remainingBudget));
        tvBudgetPercentage.setText(String.format("%.2f%% ngân sách đã sử dụng", budgetPercentage));
    }
}