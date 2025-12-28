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

public class Register extends AppCompatActivity {

    private EditText etName, etOldPass, etConfirm;
    private Button btnConfirm;
    private FirebaseRepo fbRepo;
    private String emailFromSignUp;

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

        emailFromSignUp = getIntent().getStringExtra("email");
        if (TextUtils.isEmpty(emailFromSignUp) ||
                !Patterns.EMAIL_ADDRESS.matcher(emailFromSignUp).matches()) {
            Toast.makeText(this, "Missing or incorrect email, please go back and re-enter.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConfirm.setOnClickListener(v -> doRegister());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String name = val(etName);
        String pass = val(etOldPass);
        String cf   = val(etConfirm);

        if (TextUtils.isEmpty(name))
        {
            etName.setError("Enter name");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass))
        {
            etOldPass.setError("Enter password");
            etOldPass.requestFocus();
            return;
        }
        if (pass.length() < 6)
        {
            etOldPass.setError("Password must be at least 6 characters");
            etOldPass.requestFocus(); return;
        }
        if (!TextUtils.equals(pass, cf))
        {
            etConfirm.setError("Passwords do not match");
            etConfirm.requestFocus();
            return;
        }

        fbRepo.createUserWithEmailAndPassword(name, emailFromSignUp, pass, new FirebaseRepo.OnRegisterListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onAuthFailure(Exception e) {
                Toast.makeText(Register.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDbFailure(Exception e) {
                Toast.makeText(Register.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}