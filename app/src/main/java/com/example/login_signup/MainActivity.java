package com.example.login_signup;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Random;
import android.widget.ProgressBar;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 3000;
    Animation bottomToTopAnim;
    TextView tvTaskify, tvWelcome, tvSlogan;
    ImageView imgTaskify;
    ProgressBar progressBar;
    private boolean isSplashScreenStarted = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Do nothing here. onResume will handle the logic flow.
            });

    private void Init() {
        tvTaskify = findViewById(R.id.tvTaskify);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSlogan = findViewById(R.id.tvSlogan);
        imgTaskify = findViewById(R.id.imgTaskify);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRandom() {
        String[] quote = getResources().getStringArray(R.array.inspirational_quotes);
        Random random = new Random();
        int randomIndex = random.nextInt(quote.length);
        tvSlogan.setText(quote[randomIndex]);
    }

    private void startAnim() {
        bottomToTopAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        tvTaskify.startAnimation(bottomToTopAnim);
        imgTaskify.startAnimation(bottomToTopAnim);
        tvWelcome.startAnimation(bottomToTopAnim);
        tvSlogan.startAnimation(bottomToTopAnim);
    }

    private void startSplashScreen() {
        if (isSplashScreenStarted) {
            return;
        }
        isSplashScreenStarted = true;
        progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init();
        setupRandom();
        startAnim();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isSplashScreenStarted) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivity(intent);
                }
                return;
            }
        }
        startSplashScreen();
    }
}
