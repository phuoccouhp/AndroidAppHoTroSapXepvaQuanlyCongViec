package com.example.login_signup.classes;

import android.util.Log;
import android.widget.Toast;

import com.example.login_signup.chat.ChatSession;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    //Interface for chat history
    public interface OnChatSessionsListener {
        void onSessionsLoaded(List<ChatSession> sessions);
        void onError(Exception e);
    }

    public interface OnCreateSessionListener {
        void onSuccess(ChatSession newSession, String documentId);
        void onFailure(Exception e);
    }

    // Interface for task
    public interface OnTasksLoadedListener {
        void onTasksLoaded(List<Task> tasks);
        void onError(Exception e);
    }

    public FirebaseRepo() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
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

    public void updateAvatar(String base64String, OnCompleteCallback callback){
        FirebaseUser currentUser = this.getCurrentUser();
        if(currentUser != null){
            db.collection("users").document(currentUser.getUid())
                    .update("avatarId", base64String)
                    .addOnSuccessListener(aVoid -> callback.onComplete("success", null))
                    .addOnFailureListener(e -> {
                        callback.onComplete(null, e);
                    });
        }
    }

    public void listenForChatSessions(OnChatSessionsListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("chat_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error);
                        return;
                    }

                    List<ChatSession> sessionList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ChatSession session = doc.toObject(ChatSession.class);
                            session.setId(doc.getId());
                            sessionList.add(session);
                        }
                    }
                    listener.onSessionsLoaded(sessionList);
                });
    }

    public void createNewChatSession(OnCreateSessionListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection("chat_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newChatNumber = queryDocumentSnapshots.size() + 1;
                    String chatName = "Chat-" + newChatNumber;

                    ChatSession newSession = new ChatSession(chatName, currentUser.getUid());
                    newSession.setLastMessage("New chat started...");
                    newSession.setLastUpdated(new Date());

                    db.collection("chat_sessions")
                            .add(newSession)
                            .addOnSuccessListener(documentReference -> {
                                newSession.setId(documentReference.getId());
                                listener.onSuccess(newSession, documentReference.getId());
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getTasksForDate(Date dateToLoad, OnTasksLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        String uid = currentUser.getUid();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String selectedDayString = sdfDate.format(dateToLoad);

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Object rawDate = doc.get("taskDate");

                            if (!(rawDate instanceof Timestamp)) continue;

                            Date taskDate = ((Timestamp) rawDate).toDate();
                            String taskDayString = sdfDate.format(taskDate);

                            if (taskDayString.equals(selectedDayString)) {
                                String id = doc.getId();
                                String title = doc.getString("title");
                                String category = doc.getString("category");

                                String noteContent = doc.getString("note");
                                if (noteContent == null) {
                                    noteContent = doc.getString("notes");
                                }

                                boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                                String timeStr = sdfTime.format(taskDate);
                                String dateStr = sdfDate.format(taskDate);

                                taskList.add(new Task(id, title, category, timeStr, completed, dateStr, noteContent));
                            }
                        }
                        listener.onTasksLoaded(taskList);
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    public void listenToAllTasks(OnTasksLoadedListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("tasks")
                .whereEqualTo("uid", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error);
                        return;
                    }
                    if (value == null) {
                        return;
                    }

                    List<Task> allTasks = new ArrayList<>();
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : value) {
                        Object rawDate = doc.get("taskDate");
                        if (!(rawDate instanceof Timestamp)) continue;

                        Date taskDate = ((Timestamp) rawDate).toDate();

                        String id = doc.getId();
                        String title = doc.getString("title");
                        String category = doc.getString("category");

                        String noteContent = doc.getString("note");
                        if (noteContent == null) {
                            noteContent = doc.getString("notes");
                        }

                        boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                        String timeStr = sdfTime.format(taskDate);
                        String dateStr = sdfDate.format(taskDate);

                        allTasks.add(new Task(id, title, category, timeStr, completed, dateStr, noteContent));
                    }
                    listener.onTasksLoaded(allTasks);
                });
    }

    public void deleteTask(String taskId, OnCompleteCallback callback) {
        if (taskId == null || taskId.isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Task ID is missing"));
            return;
        }

        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onComplete("Task deleted", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }
}
