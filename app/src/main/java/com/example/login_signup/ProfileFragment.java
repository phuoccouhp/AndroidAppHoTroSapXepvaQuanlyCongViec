package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Đổi thành Button (hoặc AppCompatButton)
import android.widget.TextView;

// Import Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView tvName, tvEmail, tvChangePass;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvChangePass = view.findViewById(R.id.tvChangePass);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserProfile();
        tvChangePass.setOnClickListener(v -> {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(requireContext(), Login.class));
                return;
            }
            startActivity(new Intent(requireContext(), ChangePassword.class));
        });
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            String email = currentUser.getEmail();
            tvEmail.setText(email);

            String uid = currentUser.getUid();

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullName");
                    tvName.setText(name);
                } else {
                    Log.d(TAG, "No such document");
                    tvName.setText("Name not set");
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user details", e);
                tvName.setText("Error loading name");
            });

        } else {
            goToLoginActivity();
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        if (getActivity() == null) {
            return;
        }

        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}