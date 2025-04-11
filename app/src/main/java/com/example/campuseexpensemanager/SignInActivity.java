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
import com.example.campuseexpensemanager.model.Users;

public class SignInActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView tvSignUp, tvForgetPassword;
    UserDb userDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        userDb = new UserDb(SignInActivity.this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgetPassword = findViewById(R.id.tvForgetPassword);

        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentForgetPw = new Intent(SignInActivity.this, ForgetPasswordActivity.class);
                startActivity(intentForgetPw);
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        checkLoginUser();
    }
    private void checkLoginUser(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(username)){
                    edtUsername.setError("Username can not empty");
                    return; // stop
                }
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Password can not empty");
                    return; // stop
                }
                // check login with database - SQLite
                Users infoUser = userDb.checkLoginUser(username, password);
                assert infoUser != null;
                if (infoUser.getUsername() != null){
                    // login success
                    Intent intent = new Intent(SignInActivity.this, MenuActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID_USER", infoUser.getId());
                    bundle.putString("USER_ACCOUNT", infoUser.getUsername());
                    bundle.putString("USER_EMAIL", infoUser.getEmail());
                    bundle.putInt("ROLE_ID", infoUser.getRoleId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else {
                    // login fail
                    Toast.makeText(SignInActivity.this, "Account Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
