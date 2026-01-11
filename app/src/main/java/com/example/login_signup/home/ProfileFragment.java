package com.example.login_signup.home;

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

import com.example.login_signup.password.ChangePassword;
import com.example.login_signup.R;
import com.example.login_signup.classes.Avatar;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.log_sign.Login;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

// ProfileFragment: Màn hình quản lý thông tin cá nhân của người dùng
public class ProfileFragment extends Fragment {
    private FirebaseRepo fbRepo;
    private static final String TAG = "ProfileFragment";

    // Các đối tượng thành phần giao diện
    private TextView tvName, tvEmail, tvChangePass, tvEditAvatar;
    private CircleImageView ivAvatar;
    private Button btnLogout;

    // Bộ khởi chạy để mở thư viện ảnh và nhận kết quả trả về
    private ActivityResultLauncher<String> selectImageLauncher;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fbRepo = new FirebaseRepo();

        // Ánh xạ các thành phần giao diện
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvEditAvatar = view.findViewById(R.id.tv_edit_avatar);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvChangePass = view.findViewById(R.id.tvChangePass);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Tải thông tin người dùng từ Firebase khi màn hình vừa hiện lên
        loadUserProfile();

        // Đăng ký bộ chọn ảnh từ bộ nhớ máy
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            // Xử lý ảnh sau khi người dùng đã chọn
                            handleImageSelection(uri);
                        }
                    }
                }
        );

        // Mở thư viện ảnh khi người dùng click vào Avatar hoặc EditAvatar
        View.OnClickListener avatarClickListener = v -> {
            selectImageLauncher.launch("image/*");
        };
        ivAvatar.setOnClickListener(avatarClickListener);
        tvEditAvatar.setOnClickListener(avatarClickListener);

        // Chuyển sang Activity đổi mật khẩu
        tvChangePass.setOnClickListener(v -> {
            if (fbRepo.getCurrentUser() != null) {
                startActivity(new Intent(requireContext(), ChangePassword.class));
            }
        });

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // Xử lý việc chuyển đổi ảnh đã chọn sang Base64 và cập nhật lên Firebase
    private void handleImageSelection(Uri uri) {
        try {
            // Nén và chuyển ảnh sang chuỗi Base64
            String convert = Avatar.convertImageToBase64Resized(getContext(), uri);

            fbRepo.updateAvatar(convert, (message, e) -> {
                if(e == null){
                    // Nếu thành công: Hiển thị ảnh mới lên giao diện
                    Bitmap bitmap = Avatar.convertBase64ToBitmap(convert);
                    ivAvatar.setImageBitmap(bitmap);
                    Toast.makeText(getContext(), "Avatar updated successfully.", Toast.LENGTH_SHORT).show();
                }
                else{
                    // Nếu lỗi: Thông báo và tải lại profile cũ
                    Toast.makeText(getContext(), "Could not update avatar.", Toast.LENGTH_LONG).show();
                    loadUserProfile();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error converting image", e);
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    // Tải dữ liệu Profile từ Firebase và hiển thị lên các View
    private void loadUserProfile() {
        fbRepo.loadCurrentUserProfile((user, e) ->{
            if (getContext() == null) return;

            // Kiểm tra trạng thái đăng nhập
            if (user == null || (e != null && "User not logged in".equals(e.getMessage()))) {
                goToLoginActivity();
                return;
            }

            if (e != null) {
                Log.w(TAG, "Error: " + e.getMessage());
                return;
            }

            // Gán thông tin vào giao diện
            tvEmail.setText(user.getEmail());
            String displayName = user.getName();
            tvName.setText((displayName == null || displayName.isEmpty()) ? "Name not set" : displayName);

            // Hiển thị ảnh đại diện
            String avatarId = user.getAvatarId();
            if (avatarId != null) {
                // Nếu là "anh1" thì lấy nguồn sẵn trong Resource
                if (avatarId.equals("anh1")) {
                    int avatarResId = Avatar.getAvatarResourceId(getContext(), avatarId);
                    ivAvatar.setImageResource(avatarResId);
                }
                // Nếu là ảnh Base64 thì chuyển sang Bitmap
                else {
                    Bitmap bitmap = Avatar.convertBase64ToBitmap(avatarId);
                    ivAvatar.setImageBitmap(bitmap);
                }
            }
        });
    }

    // Đăng xuất tài khoản và quay về màn hình đăng nhập
    private void logoutUser() {
        fbRepo.signOut();
        goToLoginActivity();
    }

    // Điều hướng người dùng về màn hình Login và xóa sạch stack các màn hình trước đó
    private void goToLoginActivity() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}