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
import android.widget.Toast;

// Import Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // Khai báo các View
    private TextView tvName, tvEmail;
    private Button btnLogout; // Sử dụng Button hoặc androidx.appcompat.widget.AppCompatButton

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Load layout "fragment_profile.xml"
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các View từ layout
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Tải thông tin người dùng
        loadUserProfile();

        // Gắn sự kiện click cho nút Logout
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 1. Lấy Email (có sẵn từ FirebaseAuth)
            String email = currentUser.getEmail();
            tvEmail.setText(email);

            // 2. Lấy Tên (Your Name) - Thường được lưu trong Firestore/Realtime Database
            // Chúng ta cần lấy UID của user để truy vấn database
            String uid = currentUser.getUid();

            // *** QUAN TRỌNG: Giả sử bạn lưu tên user trong collection "users"
            //     với document ID là UID của họ, và field tên là "fullName".
            //     Hãy thay đổi "users" và "fullName" cho đúng với cấu trúc database của bạn.
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Lấy tên từ field "fullName" (hoặc "name", "yourName",...)
                    String name = documentSnapshot.getString("fullName");
                    tvName.setText(name);
                } else {
                    Log.d(TAG, "No such document");
                    tvName.setText("Name not set"); // Hiển thị mặc định nếu không tìm thấy
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user details", e);
                tvName.setText("Error loading name");
            });

        } else {
            // Người dùng chưa đăng nhập, xử lý (ví dụ: quay về Login)
            // (Mặc dù về lý thuyết, không thể vào fragment này nếu chưa login)
            goToLoginActivity();
        }
    }

    private void logoutUser() {
        // Đăng xuất khỏi Firebase
        mAuth.signOut();

        // Chuyển về màn hình Đăng nhập (LoginActivity)
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        // Đảm bảo fragment còn "attached" vào Activity
        if (getActivity() == null) {
            return;
        }

        // *** QUAN TRỌNG: Thay thế "LoginActivity.class" bằng tên Activity Đăng nhập của bạn
        Intent intent = new Intent(getActivity(), Login.class);

        // Cờ này sẽ xóa tất cả các Activity trước đó (như MainActivity) khỏi back stack
        // và tạo một Task mới cho LoginActivity.
        // Điều này ngăn người dùng nhấn "Back" để quay lại app sau khi đã logout.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Kết thúc Activity hiện tại (ví dụ: MainActivity)
        getActivity().finish();
    }
}