package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    public HomeFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fabAddTask = view.findViewById(R.id.btnAddTask);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTaskActivity.class);
            startActivity(intent);
        });

        FloatingActionButton fabAiChat = view.findViewById(R.id.btnAIChat);
        fabAiChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatHistoryActivity.class);
            startActivity(intent);
        });

        return view;
    }

}
