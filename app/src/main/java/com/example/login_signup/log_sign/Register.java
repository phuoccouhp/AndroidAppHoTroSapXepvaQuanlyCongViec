package com.example.login_signup.log_sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;

// Register: Màn hình điền thông tin đăng ký tài khoản
public class Register extends AppCompatActivity {
    private FirebaseRepo fbRepo;

    private EditText etName, etOldPass, etConfirm;
    private Button btnConfirm;

    private String emailFromSignUp;

    // Ánh xạ các thành phần giao diện
    void Init(){
        etName = findViewById(R.id.etName);
        etOldPass = findViewById(R.id.etOldPass);
        etConfirm = findViewById(R.id.etConfirm);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Init();

        fbRepo = new FirebaseRepo();

        // Lấy email được truyền từ màn hình SignUp
        emailFromSignUp = getIntent().getStringExtra("email");
        
        // Kiểm tra tính hợp lệ của email truyền sang
        if (TextUtils.isEmpty(emailFromSignUp) ||
                !Patterns.EMAIL_ADDRESS.matcher(emailFromSignUp).matches()) {
            Toast.makeText(this, "Missing or incorrect email, please go back and re-enter.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConfirm.setOnClickListener(v -> doRegister());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Thực hiện quy trình đăng ký tài khoản sau khi đã kiểm tra dữ liệu đầu vào.
    private void doRegister() {
        String name = val(etName);
        String pass = val(etOldPass);
        String cf = val(etConfirm);

        // Kiểm tra tên không được để trống
        if (TextUtils.isEmpty(name))
        {
            etName.setError("Enter name");
            etName.requestFocus();
            return;
        }
        
        // Kiểm tra mật khẩu không được để trống
        if (TextUtils.isEmpty(pass))
        {
            etOldPass.setError("Enter password");
            etOldPass.requestFocus();
            return;
        }
        
        // Kiểm tra độ dài mật khẩu
        if (pass.length() < 6)
        {
            etOldPass.setError("Password must be at least 6 characters");
            etOldPass.requestFocus(); return;
        }
        
        // Kiểm tra mật khẩu xác nhận phải trùng khớp
        if (!TextUtils.equals(pass, cf))
        {
            etConfirm.setError("Passwords do not match");
            etConfirm.requestFocus();
            return;
        }

        // Gọi FirebaseRepo để tạo tài khoản và lưu thông tin người dùng
        fbRepo.createUserWithEmailAndPassword(name, emailFromSignUp, pass, new FirebaseRepo.OnRegisterListener() {
            @Override
            public void onSuccess() {
                // Đăng ký thành công và chuyển sang màn hình Đăng nhập
                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onAuthFailure(Exception e) {
                // Lỗi xác thực khi đăng ký
                Toast.makeText(Register.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDbFailure(Exception e) {
                // Lỗi khi không thể lưu thông tin Profile vào Firestore
                Toast.makeText(Register.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    // Hàm hỗ trợ lấy chuỗi văn bản đã được cắt bỏ khoảng trắng từ EditText
    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
