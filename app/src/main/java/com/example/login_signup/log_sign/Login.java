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

import com.example.login_signup.ForgetPassword;
import com.example.login_signup.HomeActivity;
import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
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

    private EditText etEmail;
    private EditText etOldPass;
    private Button btnLogin;
    private TextView tvForgetPass;
    private TextView tvSignUp;
    private ImageButton btnGoogle;

    private FirebaseRepo fbRepo;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is required for reminders.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        if (fbRepo.getCurrentUser() != null) {
            Toast.makeText(this, "Đã tự động đăng nhập!", Toast.LENGTH_SHORT).show();
            navigateToHomeActivity();
        }
    }

    void Init(){
        etEmail = findViewById(R.id.etEmail);
        etOldPass = findViewById(R.id.etOldPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgetPass = findViewById(R.id.tvForgetPass);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

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

        Init();
        setOnClick();

        fbRepo = new FirebaseRepo();

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

        fbRepo.signInWithEmailAndPassword(email, password, (message, e) -> {
            if (e == null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                navigateToHomeActivity();
            } else {
                Toast.makeText(Login.this, e.getMessage(),
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
        fbRepo.firebaseAuthWithGoogle(idToken, (message, e) -> {
            if (e == null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                navigateToHomeActivity();
            } else {
                Toast.makeText(Login.this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHomeActivity() {
        requestNotificationPermission();

        Intent intent = new Intent(Login.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
