package com.example.asm_app_se06304.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.asm_app_se06304.R;
import com.example.asm_app_se06304.model.Transaction;

import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private final OnTransactionActionListener listener;

    public interface OnTransactionActionListener {
        void onEdit(Transaction transaction);
        void onDelete(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionActionListener listener) {
        super(context, 0, transactions);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        }

        Transaction transaction = getItem(position);

        TextView tvType = convertView.findViewById(R.id.tv_transaction_type);
        TextView tvDescription = convertView.findViewById(R.id.tv_transaction_description);
        TextView tvAmount = convertView.findViewById(R.id.tv_transaction_amount);
        TextView tvDate = convertView.findViewById(R.id.tv_transaction_date);
        Button btnEdit = convertView.findViewById(R.id.btn_edit_transaction);
        Button btnDelete = convertView.findViewById(R.id.btn_delete_transaction);

        tvType.setText(transaction.getType());
        tvDescription.setText(transaction.getDescription());
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        tvAmount.setText(formatter.format(transaction.getAmount()));
        tvDate.setText(transaction.getDate());

        btnEdit.setOnClickListener(v -> listener.onEdit(transaction));
        btnDelete.setOnClickListener(v -> listener.onDelete(transaction));

        return convertView;
    }
}