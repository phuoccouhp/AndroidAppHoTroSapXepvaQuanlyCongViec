package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import android.widget.ProgressBar;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 3000;
    Animation bottomToTopAnim;
    TextView lbTodo, textWelcome, textQuote;
    ImageView imageBusiness;
    ProgressBar progressBar;


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
            Intent intent = new Intent(MainActivity.this, Sign_up.class);
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
        startSplashScreen();
    }
}