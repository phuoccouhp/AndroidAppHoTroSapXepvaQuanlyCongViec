package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirm;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirm);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnConfirm).setOnClickListener(v -> onChangePassword());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void onChangePassword() {
        String oldPw = val(etOldPass);
        String newPw = val(etNewPass);
        String cfPw  = val(etConfirm);

        if (TextUtils.isEmpty(oldPw)) { etOldPass.setError("Nhập mật khẩu cũ"); etOldPass.requestFocus(); return; }
        if (TextUtils.isEmpty(newPw)) { etNewPass.setError("Nhập mật khẩu mới"); etNewPass.requestFocus(); return; }
        if (newPw.length() < 6)       { etNewPass.setError("Mật khẩu ≥ 6 ký tự"); etNewPass.requestFocus(); return; }
        if (!TextUtils.equals(newPw, cfPw)) { etConfirm.setError("Không khớp"); etConfirm.requestFocus(); return; }
        if (oldPw.equals(newPw)) { etNewPass.setError("Mật khẩu mới phải khác mật khẩu cũ"); etNewPass.requestFocus(); return; }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            return;
        }

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Không xác định được email người dùng.", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPw);
        findViewById(R.id.btnConfirm).setEnabled(false);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPw)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                findViewById(R.id.btnConfirm).setEnabled(true);
                                Toast.makeText(this, "Đổi mật khẩu thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.btnConfirm).setEnabled(true);
                    Toast.makeText(this, "Mật khẩu cũ không đúng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
