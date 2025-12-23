package com.example.login_signup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.example.login_signup.classes.AvatarUtils;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.log_sign.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView tvName, tvEmail, tvChangePass, tvEditAvatar;
    private CircleImageView ivAvatar;
    private Button btnLogout;
    private FirebaseRepo fbRepo;
    private ActivityResultLauncher<String> selectImageLauncher;
    private Uri replaceAvatar;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fbRepo = new FirebaseRepo();
        
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvEditAvatar = view.findViewById(R.id.tv_edit_avatar);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvChangePass = view.findViewById(R.id.tvChangePass);
        btnLogout = view.findViewById(R.id.btnLogout);

        
        loadUserProfile();


        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            handleImageSelection(uri);
                        }
                    }
                }
        );
        
        View.OnClickListener avatarClickListener = v -> {
            selectImageLauncher.launch("image/*");
        };
        ivAvatar.setOnClickListener(avatarClickListener);
        tvEditAvatar.setOnClickListener(avatarClickListener);

        
        tvChangePass.setOnClickListener(v -> {
            if (fbRepo.getCurrentUser() != null) {
                startActivity(new Intent(requireContext(), ChangePassword.class));
            }
        });
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void handleImageSelection(Uri uri) {
        try {
            String convert = AvatarUtils.convertImageToBase64Resized(getContext(), uri);

            fbRepo.updateAvatar(convert, (message, e) -> {
                if(e == null){
                    Bitmap bitmap = AvatarUtils.convertBase64ToBitmap(convert);
                    ivAvatar.setImageBitmap(bitmap);
                    Toast.makeText(getContext(), "Cập nhật avatar thành công.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Không thể cập nhật avatar.", Toast.LENGTH_LONG).show();
                    loadUserProfile();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error converting image", e);
            Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        fbRepo.loadCurrentUserProfile((user, e) ->{
            if (getContext() == null) return;

            if (user == null || (e != null && "User not logged in".equals(e.getMessage()))) {
                goToLoginActivity();
                return;
            }

            if (e != null) {
                Log.w(TAG, "Lỗi: " + e.getMessage());
                return;
            }

            tvEmail.setText(user.getEmail());
            String displayName = user.getName();
            tvName.setText((displayName == null || displayName.isEmpty()) ? "Name not set" : displayName);

            String avatarId = user.getAvatarId();
            if (avatarId != null) {
                if (avatarId.equals("anh1")) {
                    int avatarResId = AvatarUtils.getAvatarResourceId(getContext(), avatarId);
                    ivAvatar.setImageResource(avatarResId);
                } else {
                    Bitmap bitmap = AvatarUtils.convertBase64ToBitmap(avatarId);
                    ivAvatar.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void logoutUser() {
        fbRepo.signOut();
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
