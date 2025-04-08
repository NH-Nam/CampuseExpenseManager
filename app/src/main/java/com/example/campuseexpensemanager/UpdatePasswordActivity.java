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

public class UpdatePasswordActivity extends AppCompatActivity {
    Button btnUpdatePassword, btnCancel;
    EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    UserDb userDb;
    Intent intent;
    Bundle bundle;
    private String account = null;
    private String email = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        
        // Initialize views
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnCancel = findViewById(R.id.btnCancel);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        
        userDb = new UserDb(UpdatePasswordActivity.this);
        intent = getIntent();
        bundle = intent.getExtras();
        if (bundle != null){
            account = bundle.getString("ACCOUNT_FORGET_PW", "");
            email = bundle.getString("EMAIL_FORGET_PW", "");
        }

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = edtCurrentPassword.getText().toString().trim();
                if (TextUtils.isEmpty(currentPassword)) {
                    edtCurrentPassword.setError("Current password cannot be empty");
                    return;
                }
                
                String newPassword = edtNewPassword.getText().toString().trim();
                if (TextUtils.isEmpty(newPassword)) {
                    edtNewPassword.setError("New password cannot be empty");
                    return;
                }
                
                String confirmPassword = edtConfirmPassword.getText().toString().trim();
                if (!confirmPassword.equals(newPassword)) {
                    edtConfirmPassword.setError("Confirm password does not match new password");
                    return;
                }
                
                int update = userDb.changePassword(newPassword, account, email);
                if (update == -1) {
                    Toast.makeText(UpdatePasswordActivity.this, "Change password failed, please try again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdatePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    Intent intentLogin = new Intent(UpdatePasswordActivity.this, SignInActivity.class);
                    startActivity(intentLogin);
                    finish();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
