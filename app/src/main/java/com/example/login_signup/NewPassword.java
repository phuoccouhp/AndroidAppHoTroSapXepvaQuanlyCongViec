package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewPassword extends AppCompatActivity {

    private EditText etNewPass, etConfirm;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        etNewPass = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirm);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnConfirm).setOnClickListener(v -> changePassword());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String p1 = val(etNewPass);
        String p2 = val(etConfirm);

        if (TextUtils.isEmpty(p1)) { etNewPass.setError("Nhập mật khẩu mới"); etNewPass.requestFocus(); return; }
        if (p1.length() < 6)        { etNewPass.setError("Mật khẩu ≥ 6 ký tự"); etNewPass.requestFocus(); return; }
        if (!TextUtils.equals(p1, p2)) { etConfirm.setError("Không khớp"); etConfirm.requestFocus(); return; }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this,
                    "Bạn chưa đăng nhập. Hãy dùng email đặt lại mật khẩu từ màn trước.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        user.updatePassword(p1)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đổi mật khẩu thành công!",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(NewPassword.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Đổi mật khẩu thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
