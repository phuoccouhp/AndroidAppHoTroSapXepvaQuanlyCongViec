package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText etName, etOldPass, etConfirm;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String emailFromSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etOldPass = findViewById(R.id.etOldPass);
        etConfirm = findViewById(R.id.etConfirm);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailFromSignUp = getIntent().getStringExtra("email");
        if (TextUtils.isEmpty(emailFromSignUp) ||
                !Patterns.EMAIL_ADDRESS.matcher(emailFromSignUp).matches()) {
            Toast.makeText(this, "Thiếu hoặc sai email, hãy quay lại nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViewById(R.id.btnConfirm).setOnClickListener(v -> doRegister());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String name = val(etName);
        String pass = val(etOldPass);
        String cf   = val(etConfirm);

        if (TextUtils.isEmpty(name))
        {
            etName.setError("Nhập tên");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass))
        {
            etOldPass.setError("Nhập mật khẩu");
            etOldPass.requestFocus();
            return;
        }
        if (pass.length() < 6)
        {
            etOldPass.setError("Mật khẩu ≥ 6 ký tự");
            etOldPass.requestFocus(); return;
        }
        if (!TextUtils.equals(pass, cf))
        {
            etConfirm.setError("Không khớp");
            etConfirm.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailFromSignUp, pass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Không lấy được người dùng sau khi tạo", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", emailFromSignUp);

                    db.collection("users").document(user.getUid())
                            .set(data)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(this, Login.class);
                                startActivity(i);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lưu hồ sơ thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                if (mAuth.getCurrentUser() != null) mAuth.getCurrentUser().delete();
                            });
                });
    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}