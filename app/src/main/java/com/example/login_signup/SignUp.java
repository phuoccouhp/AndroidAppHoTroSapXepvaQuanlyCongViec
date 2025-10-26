package com.example.login_signup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {

    private EditText etEmail;
    private ImageButton btnGoogle;

    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googlePicker;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        btnGoogle = findViewById(R.id.btnGoogle);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (!isValidEmail(email)) {
                etEmail.setError("Email không hợp lệ");
                etEmail.requestFocus();
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            Toast.makeText(this, "Email đã tồn tại. Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, Login.class));
                            finish();
                        } else {
                            // sendSignInLink(email);
                            Intent i = new Intent(this, Register.class);
                            i.putExtra("email", email);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không kiểm tra được email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        googlePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() == null) return;
                    try {
                        GoogleSignInAccount acc = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        if (acc != null && !TextUtils.isEmpty(acc.getEmail())) {
                            Intent i = new Intent(this, Register.class);
                            i.putExtra("email", acc.getEmail());
                            startActivity(i);
                        } else {
                            Toast.makeText(this, "Không lấy được email Google", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google chọn tài khoản lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        btnGoogle.setOnClickListener(v -> googlePicker.launch(googleClient.getSignInIntent()));
    }

//    private void sendSignInLink(String email) {
//        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
//                .setUrl("https://login-3f2b3.web.app/__/auth/handler")
//                .setHandleCodeInApp(true)
//                .setAndroidPackageName(getPackageName(), true, null)
//                .build();
//
//        mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(this, "Đã gửi liên kết xác minh tới " + email, Toast.LENGTH_LONG).show();
//                        getSharedPreferences("auth", MODE_PRIVATE)
//                                .edit().putString("emailForSignIn", email).apply();
//                    } else {
//                        Toast.makeText(this, "Gửi liên kết thất bại: " +
//                                (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//    private void handleEmailLinkIntent(Intent intent) {
//        if (intent == null) return;
//        Uri link = intent.getData();
//        if (link != null && mAuth.isSignInWithEmailLink(link.toString())) {
//            String email = getSharedPreferences("auth", MODE_PRIVATE)
//                    .getString("emailForSignIn", null);
//            if (email == null) {
//                Toast.makeText(this, "Không tìm thấy email đã lưu. Hãy nhập lại.", Toast.LENGTH_LONG).show();
//                return;
//            }
//            mAuth.signInWithEmailLink(email, link.toString())
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            startActivity(new Intent(this, Register.class)
//                                    .putExtra("email", email));
//                            finish();
//                        } else {
//                            Toast.makeText(this, "Liên kết không hợp lệ/hết hạn.", Toast.LENGTH_LONG).show();
//                        }
//                    });
//        }
//    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        handleEmailLinkIntent(getIntent());
//        Intent intent = getIntent();
//        Uri link = intent.getData();
//
//        if (link != null && mAuth.isSignInWithEmailLink(link.toString())) {
//            String email = getSharedPreferences("auth", MODE_PRIVATE)
//                    .getString("emailForSignIn", null);
//
//            if (email != null) {
//                mAuth.signInWithEmailLink(email, link.toString())
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(this, "Email đã được xác thực!", Toast.LENGTH_SHORT).show();
//
//                                Intent i = new Intent(this, Register.class);
//                                i.putExtra("email", email);
//                                startActivity(i);
//                                finish();
//                            } else {
//                                Toast.makeText(this, "Liên kết không hợp lệ hoặc đã hết hạn.", Toast.LENGTH_LONG).show();
//                            }
//                        });
//            } else {
//                Toast.makeText(this, "Không tìm thấy email đã lưu. Hãy nhập lại.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//    @Override protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        handleEmailLinkIntent(intent);
//    }
}