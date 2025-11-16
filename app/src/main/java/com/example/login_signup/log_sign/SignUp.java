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
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {

    private EditText etEmail;
    private ImageButton btnGoogle;
    private Button btnNext;
    private TextView tvLogin;
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googlePicker;

    private FirebaseRepo fbRepo;
    private FirebaseFirestore db;

    private void Init(){
        etEmail = findViewById(R.id.etEmail);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnNext = findViewById(R.id.btnNext);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setOnClick(){

        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (!isValidEmail(email)) {
                etEmail.setError("Email không hợp lệ");
                etEmail.requestFocus();
                return;
            }

            fbRepo.checkEmailExists(email, (emailExists, message, e) -> {
                if (e != null) {
                    Toast.makeText(this, message + e.getMessage(), Toast.LENGTH_LONG).show();

                } else if (emailExists) {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, Login.class));
                    finish();

                } else {
                    Intent i = new Intent(this, Register.class);
                    i.putExtra("email", email);
                    startActivity(i);
                }
            });
        });

        btnGoogle.setOnClickListener(v -> googlePicker.launch(googleClient.getSignInIntent()));
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
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
