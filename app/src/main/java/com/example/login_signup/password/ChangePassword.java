package com.example.login_signup.password;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;

// ChangePassword: Màn hình cho phép người dùng đã đăng nhập thay đổi mật khẩu tài khoản
public class ChangePassword extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirm;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Ánh xạ các thành phần từ giao diện
        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirm);

        fbRepo = new FirebaseRepo();

        // Thiết lập sự kiện click cho các nút chức năng
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onChangePassword());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Thực hiện quy trình kiểm tra và thay đổi mật khẩu
    private void onChangePassword() {
        String oldPw = val(etOldPass);
        String newPw = val(etNewPass);
        String cfPw = val(etConfirm);

        // Kiểm tra tính hợp lệ của dữ liệu đầu vào
        if (TextUtils.isEmpty(oldPw)) { 
            etOldPass.setError("Enter old password"); 
            etOldPass.requestFocus(); return; 
        }
        if (TextUtils.isEmpty(newPw)) { 
            etNewPass.setError("Enter new password"); 
            etNewPass.requestFocus(); return; 
        }
        // Kiểm tra độ dài tối thiểu của mật khẩu mới
        if (newPw.length() < 6) { 
            etNewPass.setError("Password must be at least 6 characters"); 
            etNewPass.requestFocus(); return; 
        }
        // Kiểm tra mật khẩu xác nhận phải trùng khớp
        if (!TextUtils.equals(newPw, cfPw)) { 
            etConfirm.setError("Passwords do not match"); 
            etConfirm.requestFocus(); return; 
        }
        // Đảm bảo mật khẩu mới không giống mật khẩu cũ
        if (oldPw.equals(newPw)) { 
            etNewPass.setError("New password must be different from the old one"); 
            etNewPass.requestFocus(); return; 
        }

        // Vô hiệu hóa nút nhấn trong quá trình xử lý với Firebase
        findViewById(R.id.btnConfirm).setEnabled(false);

        // Gọi FirebaseRepo để thực hiện đổi mật khẩu trên hệ thống
        fbRepo.changePassword(oldPw, newPw, (message, e) -> {
            // Kích hoạt lại nút sau khi có kết quả
            findViewById(R.id.btnConfirm).setEnabled(true);

            if (e == null) {
                // Thành công: Thông báo và đóng màn hình
                Toast.makeText(ChangePassword.this, message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Thất bại: Hiển thị thông báo lỗi
                Toast.makeText(ChangePassword.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm hỗ trợ lấy giá trị văn bản từ EditText
    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
