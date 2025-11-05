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
import android.widget.Toast;
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
    TextView lbTodo, textWelcome, textQuote;
    ImageView imageBusiness;
    ProgressBar progressBar;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                // After the user responds, regardless of the choice, proceed to the splash screen.
                startSplashScreen();
            });

    private void Init() {
        lbTodo = findViewById(R.id.lb_todo);
        textWelcome = findViewById(R.id.text_welcome);
        textQuote = findViewById(R.id.text_quote);
        imageBusiness = findViewById(R.id.image_business);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRandom() {
        String[] quote = getResources().getStringArray(R.array.inspirational_quotes);
        Random random = new Random();
        int randomIndex = random.nextInt(quote.length);
        textQuote.setText(quote[randomIndex]);
    }

    private void startAnim() {
        bottomToTopAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        lbTodo.startAnimation(bottomToTopAnim);
        imageBusiness.startAnimation(bottomToTopAnim);
        textWelcome.startAnimation(bottomToTopAnim);
        textQuote.startAnimation(bottomToTopAnim);
    }

    private void startSplashScreen() {
        progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
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

        // Handle permissions first, then start the splash screen timer.
        handlePermissionsAndNavigation();
    }

    private void handlePermissionsAndNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request permission. The launcher's callback will then call startSplashScreen().
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // If permission is already granted or not required for the OS version, proceed directly.
            startSplashScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for exact alarm permission every time the activity is resumed.
        checkExactAlarmPermission();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // This is a simplified check. A better UX would involve a dialog explaining why
                // the permission is needed and guiding the user to the settings.
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivity(intent);
                }
            }
        }
    }
}
