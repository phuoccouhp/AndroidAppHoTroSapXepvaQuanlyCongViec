package com.example.login_signup.password;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;

// ForgetPassword: Màn hình hỗ trợ người dùng khôi phục mật khẩu qua email
public class ForgetPassword extends AppCompatActivity {

    private EditText etEmail;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Ánh xạ thành phần giao diện
        etEmail = findViewById(R.id.etEmail);
        fbRepo = new FirebaseRepo();

        // Thiết lập sự kiện click cho các nút
        findViewById(R.id.btnNext).setOnClickListener(v -> sendResetEmail());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Xử lý logic gửi email yêu cầu đặt lại mật khẩu
    private void sendResetEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        // Kiểm tra tính hợp lệ của email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
            return;
        }

        // Vô hiệu hóa nút nhấn để tránh gửi yêu cầu liên tục
        findViewById(R.id.btnNext).setEnabled(false);

        // Gọi FirebaseRepo để gửi email khôi phục
        fbRepo.sendPasswordResetEmail(email, (message, e) -> {
            // Kích hoạt lại nút nhấn sau khi có kết quả trả về
            findViewById(R.id.btnNext).setEnabled(true);

            if (e == null) {
                // Thành công: Thông báo và quay lại màn hình đăng nhập
                Toast.makeText(this,
                        "Password reset link sent to " + email + ". Please check your inbox.",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                // Thất bại: Hiển thị lỗi từ Firebase
                Toast.makeText(this,
                        "Failed to send email: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
