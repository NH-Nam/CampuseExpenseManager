package com.example.campuseexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseexpensemanager.database.UserDb;

public class SignUpActivity extends AppCompatActivity {
    EditText edtUser, edtPassword, edtEmail, edtPhone;
    Button btnRegister, btnCancel;
    UserDb userDb;
    TextView tvLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userDb = new UserDb(SignUpActivity.this);
        edtUser = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        tvLogin = findViewById(R.id.tvLogin);
        signupAccount(); // save sqlite

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentLogin = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intentLogin);
            }
        });
    }

    private void signupAccount(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = edtUser.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)){
                    Toast.makeText(SignUpActivity.this, "Can not empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean checkUsernameEmail = userDb.checkExistsUsernameAndEmail(user, email);
                if (checkUsernameEmail){
                    // khong cho dang ky tai khoan
                    edtUser.setError("Username already exists");
                    edtEmail.setError("Email already exists");
                    return;
                }
                // insert data to Sqlite
                long insert = userDb.insertUserAccount(user, password, email, phone);
                if (insert == -1){
                    // fail
                    Toast.makeText(SignUpActivity.this, "Create account fail", Toast.LENGTH_SHORT).show();
                } else {
                    // success
                    Toast.makeText(SignUpActivity.this, "Create account success", Toast.LENGTH_SHORT).show();
                    // go to login page
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
