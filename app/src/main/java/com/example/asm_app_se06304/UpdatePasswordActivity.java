package com.example.asm_app_se06304;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.asm_app_se06304.DataBase.DatabaseContext;
import com.example.asm_app_se06304.DataBase.UserDb;

public class UpdatePasswordActivity extends AppCompatActivity {
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button confirmButton;
    private Button cancelButton;
    private UserDb userDb;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        newPasswordEditText = findViewById(R.id.etNewPassword);
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword);
        confirmButton = findViewById(R.id.btnConfirm);
        cancelButton = findViewById(R.id.btnCancel);

        userDb = new UserDb(this);
        userDb.open();

        // Retrieve the username passed from ForgotPasswordActivity
        username = getIntent().getStringExtra("USERNAME");

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void updatePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash the password (you might want to implement actual hashing)
        String passwordHash = hashPassword(newPassword);

        // Update password in the database
        Cursor cursor = userDb.getUserByUsername(username);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(DatabaseContext.ID_COL));
            @SuppressLint("Range") int rowsUpdated = userDb.updateUser(userId, username, passwordHash, cursor.getString(cursor.getColumnIndex(DatabaseContext.EMAIL_COL)), cursor.getString(cursor.getColumnIndex(DatabaseContext.PHONE_COL)));
            if (rowsUpdated > 0) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Optionally close the activity
            } else {
                Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show();
            }
        }
        cursor.close();
    }

    private String hashPassword(String password) {
        // Implement your password hashing logic here
        // For now, we will just return the plain password
        return password; // Replace this with actual hashing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDb.close();
    }
}