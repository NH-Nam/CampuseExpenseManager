package com.example.asm_app_se06304;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.DataBase.UserDb;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText emailEditText;
    private Button submitButton;
    private UserDb userDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        usernameEditText = findViewById(R.id.etUsername);
        emailEditText = findViewById(R.id.etEmail);
        submitButton = findViewById(R.id.btnConfirm);
        userDb = new UserDb(this);

        userDb.open();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                if (isInputValid(username, email)) {
                    validateUser(username, email);
                }
            }
        });
    }

    private boolean isInputValid(String username, String email) {
        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validateUser(String username, String email) {
        Cursor cursor = userDb.getUserByUsername(username);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String dbEmail = cursor.getString(cursor.getColumnIndex(DatabaseContext.EMAIL_COL));
            if (dbEmail.equals(email)) {
                // Username and email match, proceed to UpdatePasswordActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, UpdatePasswordActivity.class);
                intent.putExtra("USERNAME", username); // Pass the username
                startActivity(intent);
                finish(); // Close the current activity
            } else {
                Toast.makeText(this, "Email does not match the username", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDb.close();
    }
}