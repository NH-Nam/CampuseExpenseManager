package com.example.campuseexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseexpensemanager.database.UserDb;

public class ForgetPasswordActivity extends AppCompatActivity {
    Button btnConfirm, btnCancel;
    EditText edtAccount, edtEmail;
    UserDb userDb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        userDb = new UserDb(ForgetPasswordActivity.this);
        btnConfirm = findViewById(R.id.btnConfirmAccount);
        btnCancel = findViewById(R.id.btnCancel);
        edtAccount = findViewById(R.id.edtAccount);
        edtEmail = findViewById(R.id.edtEmail);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = edtAccount.getText().toString().trim();
                if (TextUtils.isEmpty(account)){
                    edtAccount.setError("Account can not empty");
                    return;
                }
                String email = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    edtEmail.setError("Email can not empty");
                    return;
                }
                boolean checking = userDb.checkExistsUsernameAndEmail(account, email);
                if (checking){
                    // success
                    Intent intent = new Intent(ForgetPasswordActivity.this, UpdatePasswordActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ACCOUNT_FORGET_PW", account);
                    bundle.putString("EMAIL_FORGET_PW", email);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    // fail
                    Toast.makeText(ForgetPasswordActivity.this, "Account Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
