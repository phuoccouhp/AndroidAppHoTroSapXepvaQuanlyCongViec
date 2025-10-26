package com.example.login_signup;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    // Khai báo các thành phần UI
    private EditText etEmail;
    private EditText etOldPass;
    private Button btnLogin;
    private TextView tvForgetPass;
    private TextView tvSignUp;
    private ImageButton btnGoogle;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "Đã tự động đăng nhập!", Toast.LENGTH_SHORT).show();
            navigateToHomeActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etOldPass = findViewById(R.id.etOldPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgetPass = findViewById(R.id.tvForgetPass);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);

        createGoogleSignInRequest();
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
    protected void onResume() {
        super.onResume();
        checkPasswordResetFlow();
    }

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

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 5000);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etOldPass.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHomeActivity();
                    } else {
                        Toast.makeText(Login.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createGoogleSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHomeActivity();
                    } else {
                        Toast.makeText(Login.this, "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(Login.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}