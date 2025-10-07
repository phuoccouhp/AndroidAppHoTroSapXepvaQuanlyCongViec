package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class Sign_up extends AppCompatActivity {

    // Khai báo các thành phần UI
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private ImageView googleSignInButton;

    // Khai báo các đối tượng Firebase và Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các thành phần View từ layout XML
        nameEditText = findViewById(R.id.text_name); // Thay ID cho đúng với file XML của bạn
        emailEditText = findViewById(R.id.text_email); // Thay ID cho đúng
        passwordEditText = findViewById(R.id.password_edit_text); // Thay ID cho đúng
        confirmPasswordEditText = findViewById(R.id.confirm_pass_edit_text); // Thay ID cho đúng
        signUpButton = findViewById(R.id.btn_sign_up); // Thay ID cho đúng
        loginTextView = findViewById(R.id.text_login); // Thay ID cho đúng
        googleSignInButton = findViewById(R.id.image_gg); // Thay ID cho đúng

        // Cấu hình cho Google Sign-In
        createGoogleSignInRequest();

        // Chuẩn bị để nhận kết quả trả về từ màn hình đăng nhập Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Thiết lập sự kiện click cho nút Đăng ký
        signUpButton.setOnClickListener(v -> registerUser());

        // Thiết lập sự kiện click cho chữ "LOG IN"
        loginTextView.setOnClickListener(v -> startActivity(new Intent(Sign_up.this, Login.class)));

        // Thiết lập sự kiện click cho nút Google
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Hàm xử lý logic đăng ký người dùng bằng Email và Password
     */
    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo người dùng trên Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveAdditionalUserInfo(name, email);
                    } else {
                        Toast.makeText(Sign_up.this, "Đăng ký thất bại: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Hàm lưu các thông tin bổ sung của người dùng (tên, email) vào Realtime Database
     */
    private void saveAdditionalUserInfo(String name, String email) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);

        FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Sign_up.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHomeActivity();
                    } else {
                        Toast.makeText(Sign_up.this, "Lưu thông tin thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hàm cấu hình các tùy chọn cho Google Sign-In
     */
    private void createGoogleSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Hàm mở màn hình đăng nhập của Google
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Hàm xác thực với Firebase sau khi đã đăng nhập Google thành công
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Nếu là người dùng mới, lưu thông tin vào database
                        if (task.getResult().getAdditionalUserInfo().isNewUser() && user != null) {
                            saveAdditionalUserInfo(user.getDisplayName(), user.getEmail());
                        } else {
                            // Nếu là người dùng cũ, chỉ cần chuyển màn hình
                            Toast.makeText(Sign_up.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToHomeActivity();
                        }
                    } else {
                        Toast.makeText(Sign_up.this, "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hàm điều hướng tới màn hình chính và xóa các màn hình trước đó
     */
    private void navigateToHomeActivity() {
        // Thay MainActivity.class thành HomeActivity.class (hoặc tên Activity trang Home của bạn)
        Intent intent = new Intent(Sign_up.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}