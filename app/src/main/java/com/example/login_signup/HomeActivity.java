package com.example.login_signup;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private List<ImageButton> navButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.nav_home_button));
        navButtons.add(findViewById(R.id.nav_calendar_button));
        navButtons.add(findViewById(R.id.nav_documents_button));
        navButtons.add(findViewById(R.id.nav_settings_button));

        // Set click listeners
        navButtons.get(0).setOnClickListener(v -> loadFragment(new HomeFragment(), v));
        navButtons.get(1).setOnClickListener(v -> loadFragment(new CalendarFragment(), v));
        navButtons.get(2).setOnClickListener(v -> loadFragment(new DocumentsFragment(), v));
        navButtons.get(3).setOnClickListener(v -> loadFragment(new ProfileFragment(), v));

        // Load initial fragment and set initial state
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), navButtons.get(0));
        }
    }

    private void loadFragment(Fragment fragment, View selectedButton) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateNavButtons(selectedButton);
    }

    private void updateNavButtons(View selectedButton) {
        for (ImageButton button : navButtons) {
            boolean isSelected = (button == selectedButton);
            button.setSelected(isSelected);

            // Animate scale
            if (isSelected) {
                button.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
            } else {
                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }
    }
}
