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
    Button btnSaveChangePw;
    EditText edtNewPw, edtConfirmNewPw;
    UserDb userDb;
    Intent intent;
    Bundle bundle;
    private String account = null;
    private String email = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        btnSaveChangePw = findViewById(R.id.btnSaveChangePassword);
        edtNewPw = findViewById(R.id.edtNewPassword);
        edtConfirmNewPw = findViewById(R.id.edtConfirmNewPassword);
        userDb = new UserDb(UpdatePasswordActivity.this);
        intent = getIntent();
        bundle = intent.getExtras();
        if (bundle != null){
            account = bundle.getString("ACCOUNT_FORGET_PW", "");
            email = bundle.getString("EMAIL_FORGET_PW", "");
        }

        btnSaveChangePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = edtNewPw.getText().toString().trim();
                if (TextUtils.isEmpty(newPassword)){
                    edtNewPw.setError("New password can not empty");
                    return;
                }
                String confirmNewPw = edtConfirmNewPw.getText().toString().trim();
                if (!confirmNewPw.equals(newPassword)){
                    edtConfirmNewPw.setError("Confirm new password not equals new password");
                    return;
                }
                int update = userDb.changePassword(newPassword, account, email);
                if (update == -1){
                    // fail
                    Toast.makeText(UpdatePasswordActivity.this, "Change password fail, please try again", Toast.LENGTH_SHORT).show();
                } else {
                    // success
                    Toast.makeText(UpdatePasswordActivity.this, "Change password success", Toast.LENGTH_SHORT).show();
                    Intent intentLogin = new Intent(UpdatePasswordActivity.this, SignInActivity.class);
                    startActivity(intentLogin);
                    finish();
                }
            }
        });
    }
}
