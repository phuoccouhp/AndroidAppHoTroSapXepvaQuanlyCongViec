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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements AvatarPickerDialogFragment.AvatarPickerListener {

    private static final String TAG = "ProfileFragment";

    private TextView tvName, tvEmail, tvChangePass, tvEditAvatar;
    private CircleImageView ivAvatar;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvEditAvatar = view.findViewById(R.id.tv_edit_avatar);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvChangePass = view.findViewById(R.id.tvChangePass);
        btnLogout = view.findViewById(R.id.btnLogout);

        
        loadUserProfile();

        
        View.OnClickListener avatarClickListener = v -> {
            AvatarPickerDialogFragment dialog = new AvatarPickerDialogFragment();
            dialog.show(getChildFragmentManager(), "AvatarPicker");
        };
        ivAvatar.setOnClickListener(avatarClickListener);
        tvEditAvatar.setOnClickListener(avatarClickListener);

        
        tvChangePass.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(requireContext(), ChangePassword.class));
            }
        });
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            String uid = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(uid);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullName");
                    String avatarId = documentSnapshot.getString("avatarId");

                    tvName.setText(name != null ? name : "Name not set");

                    
                    int avatarResId = AvatarUtils.getAvatarResourceId(getContext(), avatarId);
                    ivAvatar.setImageResource(avatarResId);
                } else {
                    Log.d(TAG, "Không tìm thấy thông tin người dùng.");
                    tvName.setText("Name not set");
                    ivAvatar.setImageResource(R.drawable.ic_avatar_1); 
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi khi lấy thông tin người dùng", e);
            });
        } else {
            goToLoginActivity();
        }
    }

    @Override
    public void onAvatarSelected(String avatarId) {
        
        int avatarResId = AvatarUtils.getAvatarResourceId(getContext(), avatarId);
        ivAvatar.setImageResource(avatarResId);

        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .update("avatarId", avatarId)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Cập nhật avatar thành công."))
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Lỗi khi cập nhật avatar", e);
                        Toast.makeText(getContext(), "Không thể cập nhật avatar.", Toast.LENGTH_SHORT).show();
                        loadUserProfile(); 
                    });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
