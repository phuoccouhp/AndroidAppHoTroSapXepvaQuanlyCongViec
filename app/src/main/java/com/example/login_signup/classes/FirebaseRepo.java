package com.example.login_signup.classes;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRepo {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    //Interface to report success/failure
    public interface OnCompleteCallback {
        void onComplete(String message, Exception e);
    }

    public interface OnEmailCheckListener {
        void onComplete(boolean emailExists, String message, Exception e);
    }

    public interface OnRegisterListener {
        void onSuccess();
        void onAuthFailure(Exception e);
        void onDbFailure(Exception e);
    }

    public interface onLoadedUserListener {
        void onComplete(User user, Exception e);
    }

    public FirebaseRepo() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }

    public void signInWithEmailAndPassword(String email, String password, OnCompleteCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Vui lòng nhập email và mật khẩu"));
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete("Đăng nhập thành công!", null);
                    } else {
                        callback.onComplete("Đăng nhập thất bại: ", task.getException());
                    }
                });
    }

    public void firebaseAuthWithGoogle(String idToken, OnCompleteCallback callback){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       callback.onComplete("Đăng nhập thành công!", null);
                   } else {
                       callback.onComplete(null, task.getException());
                   }
                });
    }

    public void checkEmailExists(String email, OnEmailCheckListener listener) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        listener.onComplete(true, "Email đã tồn tại. Vui lòng đăng nhập.", null);
                    } else {
                        listener.onComplete(false, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onComplete(false, "Không kiểm tra được email: ", e);
                });
    }

    public void createUserWithEmailAndPassword(String name, String email, String pass, OnRegisterListener listener){
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if(listener == null){
                        return;
                    }
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null){
                            listener.onAuthFailure(new Exception("Không lấy được người dùng sau khi tạo"));
                            return;
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("avatarId", "anh1");
                        data.put("name", name);
                        data.put("email", email);

                        db.collection("users").document(user.getUid())
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    listener.onDbFailure(e);
                                });
                    }
                    else{
                        if(task.getException() != null){
                            listener.onAuthFailure(task.getException());
                        }
                        else{
                            listener.onAuthFailure(new Exception("Đăng ký thất bại"));
                        }
                    }
                });

    }

    public void loadCurrentUserProfile(onLoadedUserListener listener){
        User user = new User();
        FirebaseUser currentUser = this.getCurrentUser();
        if(currentUser != null){
            user.setEmail(currentUser.getEmail());
            user.setUid(currentUser.getUid());

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(listener == null) { return; }
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String avatarId = documentSnapshot.getString("avatarId");

                            user.setName(name != null ? name : "Name not set");
                            user.setAvatarId(avatarId);
                            listener.onComplete(user, null);
                        } else {
                            listener.onComplete(user, new Exception("Không tìm thấy thông tin người dùng."));
                        }
                    })
                    .addOnFailureListener(e -> {
                        listener.onComplete(user, e);
                    });
        }
        else{
            listener.onComplete(null, new Exception("User not logged in"));
        }
    }

    public void updateAvatar(String base64string, OnCompleteCallback callback){
        FirebaseUser currentUser = this.getCurrentUser();
        if(currentUser != null){
            db.collection("users").document(currentUser.getUid())
                    .update("avatarId", base64string)
                    .addOnSuccessListener(aVoid -> callback.onComplete("success", null))
                    .addOnFailureListener(e -> {
                        callback.onComplete(null, e);
                    });
        }

    }
}
