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

import java.io.FileInputStream;

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
    private void checkLoginWithDataFile(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(username)){
                    edtUsername.setError("Username can not empty");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Password can not empty");
                    return;
                }
                try {
                    // xu ly doc noi dung tu file local storage de kiem tra dang nhap
                    FileInputStream fileInSt = openFileInput("account.txt");
                    int read = -1;
                    StringBuilder builder = new StringBuilder();
                    while ((read = fileInSt.read()) != -1){
                        builder.append((char) read);
                        // tat ca du lieu gan vao bien builder
                    }
                    fileInSt.close();
                    // validation account
                    String[] infoAccount = null;
                    // mang thong tin cua tat ca cac tai khoan
                    infoAccount = builder.toString().trim().split("\n");
                    boolean checkAccount = false;
                    // duyet mang de kiem tra tai khoan
                    int sizeArrayAccount = infoAccount.length;
                    for (int i = 0; i < sizeArrayAccount; i++){
                        String user = infoAccount[i].substring(0, infoAccount[i].indexOf("|"));
                        String pass = infoAccount[i].substring(infoAccount[i].indexOf("|")+1);
                        if (username.equals(user) && password.equals(pass)){
                            checkAccount = true;
                            break;
                        }
                    }
                    if (checkAccount){
                        // login success
                        Intent intent = new Intent(SignInActivity.this, MenuActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("USER_ACCOUNT", username);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        // login fail
                        Toast.makeText(SignInActivity.this, "Account Invalid", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
