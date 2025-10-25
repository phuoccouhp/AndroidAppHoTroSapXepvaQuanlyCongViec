package com.example.login_signup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Thêm import
import android.widget.TextView; // Thêm import

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Load layout "fragment_profile.xml"
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // (Bạn có thể thêm code cho nút Logout, v.v... ở đây)
        // Ví dụ:
        // Button btnLogout = view.findViewById(R.id.btn_logout);
        // btnLogout.setOnClickListener(v -> {
        //    // Xử lý logout
        // });

        return view;
    }
}