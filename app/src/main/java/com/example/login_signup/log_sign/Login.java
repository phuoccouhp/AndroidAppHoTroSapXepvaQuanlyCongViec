package com.example.login_signup.log_sign;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.login_signup.home.HomeActivity;
import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.password.ForgetPassword;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

// Login: Màn hình đăng nhập của ứng dụng
public class Login extends AppCompatActivity {
    private FirebaseRepo fbRepo;

    // Các đối tượng thành phần giao diện
    private EditText etEmail;
    private EditText etOldPass;
    private Button btnLogin;
    private TextView tvForgetPass;
    private TextView tvSignUp;
    private ImageButton btnGoogle;

    // Đăng nhập bằng Google
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // Bộ khởi chạy để yêu cầu quyền thông báo
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is required for reminders.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        // Tự động đăng nhập nếu đã có phiên làm việc trước đó
        if (fbRepo.getCurrentUser() != null) {
            Toast.makeText(this, "Automatically logged in!", Toast.LENGTH_SHORT).show();
            navigateToHomeActivity();
        }
    }

    // Ánh xạ các thành phần giao diện
    void Init(){
        etEmail = findViewById(R.id.etEmail);
        etOldPass = findViewById(R.id.etOldPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgetPass = findViewById(R.id.tvForgetPass);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    // Thiết lập sự kiện click cho các nút bấm và liên kết
    void setOnClick(){
        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgetPass.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgetPassword.class);
            startActivity(intent);
        });
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fbRepo = new FirebaseRepo();
        Init();
        setOnClick();

        // Cấu hình đăng nhập Google
        createGoogleSignInRequest();
        
        // Nhận kết quả từ màn hình chọn tài khoản Google
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra xem người dùng có vừa quay lại sau khi đặt lại mật khẩu không
        checkPasswordResetFlow();
    }

    // Kiểm tra trạng thái đặt lại mật khẩu
    private void checkPasswordResetFlow() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean passwordResetFlow = prefs.getBoolean("passwordResetViaEmail", false);

        if (passwordResetFlow) {
            showSuccessDialog();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("passwordResetViaEmail", false);
            editor.apply();
        }
    }

    // Hiển thị hộp thoại thông báo đặt lại mật khẩu thành công
    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        dialog.show();

        // Tự động đóng dialog sau 5 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 5000);
    }

    // Xử lý đăng nhập bằng Email và Mật khẩu qua Firebase
    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etOldPass.getText().toString();

        fbRepo.signInWithEmailAndPassword(email, password, (message, e) -> {
            if (e == null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                navigateToHomeActivity();
            } else {
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Cấu hình đăng nhập Google
    private void createGoogleSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Đăng nhập bằng Google
    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // Xác thực với Firebase bằng Token nhận được từ Google
    private void firebaseAuthWithGoogle(String idToken) {
        fbRepo.firebaseAuthWithGoogle(idToken, (message, e) -> {
            if (e == null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                navigateToHomeActivity();
            } else {
                Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chuyển sang màn hình chính (HomeActivity)
    private void navigateToHomeActivity() {
        requestNotificationPermission();

        Intent intent = new Intent(Login.this, HomeActivity.class);
        // Xóa sạch stack các Activity trước đó
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Yêu cầu quyền thông báo nếu chạy trên Android 13 trở lên
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
