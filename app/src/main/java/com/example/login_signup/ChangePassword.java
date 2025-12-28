package com.example.login_signup;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.classes.FirebaseRepo;

public class ChangePassword extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirm;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirm);

        fbRepo = new FirebaseRepo();

        findViewById(R.id.btnConfirm).setOnClickListener(v -> onChangePassword());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void onChangePassword() {
        String oldPw = val(etOldPass);
        String newPw = val(etNewPass);
        String cfPw  = val(etConfirm);

        if (TextUtils.isEmpty(oldPw)) { etOldPass.setError("Enter old password"); etOldPass.requestFocus(); return; }
        if (TextUtils.isEmpty(newPw)) { etNewPass.setError("Enter new password"); etNewPass.requestFocus(); return; }
        if (newPw.length() < 6) { etNewPass.setError("Password must be at least 6 characters"); etNewPass.requestFocus(); return; }
        if (!TextUtils.equals(newPw, cfPw)) { etConfirm.setError("Passwords do not match"); etConfirm.requestFocus(); return; }
        if (oldPw.equals(newPw)) { etNewPass.setError("New password must be different from the old one"); etNewPass.requestFocus(); return; }

        findViewById(R.id.btnConfirm).setEnabled(false);

        fbRepo.changePassword(oldPw, newPw, (message, e) -> {
            findViewById(R.id.btnConfirm).setEnabled(true);

            if (e == null) {
                Toast.makeText(ChangePassword.this, message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ChangePassword.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}