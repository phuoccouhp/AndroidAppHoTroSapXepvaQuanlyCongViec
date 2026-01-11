package com.example.login_signup.log_sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;

// SignUp: Màn hình bước đầu tiên của quy trình đăng ký
public class SignUp extends AppCompatActivity {
    private FirebaseRepo fbRepo;

    private EditText etEmail;
    private ImageButton btnGoogle;
    private Button btnNext;
    private TextView tvLogin;

    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googlePicker;

    // Ánh xạ các thành phần giao diện
    private void Init(){
        etEmail = findViewById(R.id.etEmail);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnNext = findViewById(R.id.btnNext);
        tvLogin = findViewById(R.id.tvLogin);
    }

    // Thiết lập các sự kiện click cho các nút và văn bản điều hướng
    private void setOnClick(){

        // Xử lý khi nhấn nút "Next" để kiểm tra email
        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            // Kiểm tra định dạng email hợp lệ
            if (!isValidEmail(email)) {
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }

            // Kiểm tra email đã tồn tại trong hệ thống Firebase chưa
            fbRepo.checkEmailExists(email, (emailExists, message, e) -> {
                if (e != null) {
                    // Lỗi trong quá trình kiểm tra
                    Toast.makeText(this, message + e.getMessage(), Toast.LENGTH_LONG).show();

                } else if (emailExists) {
                    // Nếu email đã có tài khoản: Thông báo và chuyển sang màn hình Đăng nhập
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, Login.class));
                    finish();

                } else {
                    // Nếu email chưa có: Chuyển sang màn hình Register để hoàn tất thông tin
                    Intent i = new Intent(this, Register.class);
                    i.putExtra("email", email);
                    startActivity(i);
                }
            });
        });

        // Kích hoạt quy trình chọn tài khoản Google
        btnGoogle.setOnClickListener(v -> googlePicker.launch(googleClient.getSignInIntent()));

        // Chuyển nhanh sang màn hình Đăng nhập
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Init();
        setOnClick();

        fbRepo = new FirebaseRepo();

        // Cấu hình Google Sign-In để lấy thông tin Email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Đăng ký bộ thu nhận kết quả từ màn hình chọn tài khoản Google
        googlePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() == null) return;
                    try {
                        // Lấy thông tin tài khoản Google đã chọn
                        GoogleSignInAccount acc = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        if (acc != null && !TextUtils.isEmpty(acc.getEmail())) {
                            // Chuyển sang màn hình hoàn tất đăng ký với email từ Google
                            Intent i = new Intent(this, Register.class);
                            i.putExtra("email", acc.getEmail());
                            startActivity(i);
                        } else {
                            Toast.makeText(this, "Could not get Google email", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google account selection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hàm hỗ trợ kiểm tra định dạng email bằng Patterns của hệ thống
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
