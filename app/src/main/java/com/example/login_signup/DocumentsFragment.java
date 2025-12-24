package com.example.login_signup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class DocumentsFragment extends Fragment {

    private MaterialButtonToggleGroup toggleGroup;
    private Button btnTabHome, btnTabAchievements, btnTabAnalysis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_documents, container, false);

        toggleGroup = v.findViewById(R.id.toggleGroup);
        btnTabHome = v.findViewById(R.id.btnTabHome);
        btnTabAchievements = v.findViewById(R.id.btnTabAchievements);
        btnTabAnalysis = v.findViewById(R.id.btnTabAnalysis);

        if (savedInstanceState == null) {
            loadFragment(new TasksFragment());
            updateButtonStyles(btnTabHome);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabHome) {
                    loadFragment(new TasksFragment());
                    updateButtonStyles(btnTabHome);

                } else if (checkedId == R.id.btnTabAchievements) {
                    loadFragment(new AchievementsFragment());
                    updateButtonStyles(btnTabAchievements);

                } else if (checkedId == R.id.btnTabAnalysis) {
                    loadFragment(new AnalysisFragment());
                    updateButtonStyles(btnTabAnalysis);
                }
            }
        });

        toggleGroup.setSelectionRequired(true);
        return v;
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void updateButtonStyles(Button activeButton) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.pastel_blue_dark);
        int inactiveColor = Color.WHITE;
        int activeTextColor = Color.WHITE;
        int inactiveTextColor = Color.parseColor("#64748B");

        setButtonStyle(btnTabHome, inactiveColor, inactiveTextColor);
        setButtonStyle(btnTabAchievements, inactiveColor, inactiveTextColor);
        setButtonStyle(btnTabAnalysis, inactiveColor, inactiveTextColor);

        setButtonStyle(activeButton, activeColor, activeTextColor);
    }

    private void setButtonStyle(Button btn, int backgroundColor, int textColor) {
        btn.setBackgroundColor(backgroundColor);
        btn.setTextColor(textColor);
    }
}