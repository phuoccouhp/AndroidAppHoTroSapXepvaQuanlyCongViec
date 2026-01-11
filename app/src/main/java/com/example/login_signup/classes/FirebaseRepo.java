package com.example.login_signup.classes;

import android.util.Log;

import com.example.login_signup.chat.ChatSession;
import com.example.login_signup.history.TaskLog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.EmailAuthProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

// Lớp FirebaseRepo quản lý tập trung các tương tác với Firebase (Auth & Firestore)
public class FirebaseRepo {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Khởi tạo đối tượng FirebaseRepo
    public FirebaseRepo() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    // Phương thức trả về người dùng hiện tại
    public FirebaseUser getCurrentUser(){
        return auth.getCurrentUser();
    }

    // Phương thức đăng xuất
    public void signOut() {
        auth.signOut();
    }

    // --- Các interface trả về kết quả (callback) ---

    // Interface trả về kết quả sau khi thực hiện một hành động
    public interface OnCompleteCallback {
        void onComplete(String message, Exception e);
    }

    // Interface trả về kết quả sau khi kiểm tra Email
    public interface OnEmailCheckListener {
        void onComplete(boolean emailExists, String message, Exception e);
    }

    // Interface trả về kết quả sau khi đăng ký tài khoản
    public interface OnRegisterListener {
        void onSuccess();
        void onAuthFailure(Exception e);
        void onDbFailure(Exception e);
    }

    // Interface trả về kết quả sau khi tải thông tin người dùng
    public interface onLoadedUserListener {
        void onComplete(User user, Exception e);
    }

    // Interface trả về kết quả sau khi tải danh sách phiên trò chuyện
    public interface OnChatSessionsListener {
        void onSessionsLoaded(List<ChatSession> sessions);
        void onError(Exception e);
    }

    // Interface trả về kết quả sau khi tạo phiên trò chuyện
    public interface OnCreateSessionListener {
        void onSuccess(ChatSession newSession, String documentId);
        void onFailure(Exception e);
    }

    // Interface trả về kết quả sau khi tải danh sách lịch sử hành động
    public interface OnLogLoadedListener {
        void onLogsLoaded(List<TaskLog> logs);
        void onError(Exception e);
    }

    // Interface trả về kết quả sau khi thêm công việc
    public interface OnAddTaskListener {
        void onSuccess(String taskId);
        void onFailure(Exception e);
    }

    // Interface trả về kết quả sau khi tải danh sách công việc
    public interface OnTasksLoadedListener {
        void onTasksLoaded(List<Task> tasks);
        void onError(Exception e);
    }

    // Interface trả về kết quả sau khi tải chi tiết công việc
    public interface OnTaskDetailLoadedListener {
        void onTaskLoaded(Task task);
        void onError(Exception e);
    }

    // Interface trả về kết quả sau khi tải số ngày liên tiếp (Streak)
    public interface OnStreakLoadedListener {
        void onStreakLoaded(int currentStreak);
        void onError(Exception e);
    }

    // Interface trả về kết quả sau khi tải thống kê số lượng công việc
    public interface OnTaskStatsLoadedListener {
        void onStatsLoaded(int totalCreated, int totalCompleted);
        void onError(Exception e);
    }

    // -- Quản lý việc đăng nhập/đăng ký tài khoản (AUTH) --

    // Đăng nhập bằng Email/Password
    public void signInWithEmailAndPassword(String email, String password, OnCompleteCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Please enter email and password"));
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete("Login successful!", null);
                    } else {
                        callback.onComplete("Login failed: ", task.getException());
                    }
                });
    }

    // Xác thực với Google
    public void firebaseAuthWithGoogle(String idToken, OnCompleteCallback callback){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        callback.onComplete("Login successful!", null);
                    } else {
                        callback.onComplete(null, task.getException());
                    }
                });
    }

    // Kiểm tra Email đã tồn tại trong hệ thống chưa
    public void checkEmailExists(String email, OnEmailCheckListener listener) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        listener.onComplete(true, "Email already exists. Please login.", null);
                    } else {
                        listener.onComplete(false, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onComplete(false, "Could not check email: ", e);
                });
    }

    // Tạo tài khoản mới và lưu thông tin vào Firestore
    public void createUserWithEmailAndPassword(String name, String email, String pass, OnRegisterListener listener){
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if(listener == null){
                        return;
                    }
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null){
                            listener.onAuthFailure(new Exception("Could not get user after creation"));
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
                            listener.onAuthFailure(new Exception("Registration failed"));
                        }
                    }
                });

    }

    // Gửi Email khôi phục mật khẩu
    public void sendPasswordResetEmail(String email, OnCompleteCallback callback) {
        if (email == null || email.isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Email cannot be empty"));
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onComplete("Reset email sent", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Đổi mật khẩu (yêu cầu xác thực lại người dùng)
    public void changePassword(String oldPassword, String newPassword, OnCompleteCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onComplete(null, new Exception("User not logged in"));
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            callback.onComplete(null, new Exception("Cannot determine user email"));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> callback.onComplete("Password changed successfully!", null))
                            .addOnFailureListener(e -> callback.onComplete(null, e));
                })
                .addOnFailureListener(e -> {
                    callback.onComplete(null, new Exception("Incorrect old password"));
                });
    }

    // Cập nhật mật khẩu (không yêu cầu xác thực lại người dùng)
    public void updatePassword(String newPassword, OnCompleteCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onComplete(null, new Exception("User not logged in"));
            return;
        }

        user.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> callback.onComplete("Password updated", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Tải thông tin Profile của người dùng
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
                            listener.onComplete(user, new Exception("User information not found."));
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

    // Cập nhật ảnh đại diện (Base64)
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

    // --- Quản lý cuộc trò chuyện (CHAT) ---

    // Lắng nghe các thay đổi trong danh sách phiên chat theo thời gian thực
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

    // Tạo một phiên chat mới
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


    // Đổi tên phiên chat
    public void renameChatSession(String sessionId, String newName, OnCompleteCallback callback) {
        if (sessionId == null) return;
        db.collection("chat_sessions").document(sessionId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> callback.onComplete("Renamed successfully", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Xóa phiên chat.
    public void deleteChatSession(String sessionId, OnCompleteCallback callback) {
        if (sessionId == null) return;
        db.collection("chat_sessions").document(sessionId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onComplete("Deleted successfully", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // --- Quản lý công việc (TASK) ---

    // Thêm công việc mới
    public void addTask(Map<String, Object> taskData, OnAddTaskListener listener) {
        db.collection("tasks").add(taskData)
                .addOnSuccessListener(documentReference -> {
                    listener.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(listener::onFailure);
    }


    // Xóa công việc
    public void deleteTask(String taskId, String taskTitle, OnCompleteCallback callback) {
        if (taskId == null || taskId.isEmpty()) {
            callback.onComplete(null, new IllegalArgumentException("Task ID is missing"));
            return;
        }

        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    logTaskAction(taskId, taskTitle, "DELETED");

                    callback.onComplete("Task deleted", null);
                })
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Tải chi tiết một công việc
    public void getTaskDetails(String taskId, OnTaskDetailLoadedListener listener) {
        if (taskId == null) {
            listener.onError(new Exception("Task ID is null"));
            return;
        }

        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Task task = new Task();
                            task.setId(documentSnapshot.getId());
                            task.setTitle(documentSnapshot.getString("title"));
                            task.setCategory(documentSnapshot.getString("category"));
                            task.setVibration(documentSnapshot.getString("vibration"));
                            task.setNote(documentSnapshot.getString("notes"));

                            task.setTaskDate(documentSnapshot.getDate("taskDate"));
                            task.setRingtone(documentSnapshot.getString("ringtone"));

                            Boolean reminder = documentSnapshot.getBoolean("reminder");
                            task.setReminder(reminder != null && reminder);

                            listener.onTaskLoaded(task);
                        } catch (Exception e) {
                            listener.onError(e);
                        }
                    } else {
                        listener.onError(new Exception("Task not found"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    // Cập nhật công việc
    public void updateTask(String taskId, Map<String, Object> taskUpdates, OnCompleteCallback callback) {
        if (taskId == null) {
            callback.onComplete(null, new Exception("Task ID is null"));
            return;
        }

        db.collection("tasks").document(taskId)
                .update(taskUpdates)
                .addOnSuccessListener(aVoid -> callback.onComplete("Task updated successfully", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Tải tất cả công việc của người dùng hiện tại
    public void loadTasksForUser(OnTasksLoadedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("tasks")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Object rawDate = doc.get("taskDate");
                            if (!(rawDate instanceof com.google.firebase.Timestamp)) continue;

                            Date taskDate = ((com.google.firebase.Timestamp) rawDate).toDate();

                            String id = doc.getId();
                            String title = doc.getString("title");
                            String category = doc.getString("category");
                            boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");

                            String noteContent = doc.getString("note");
                            if (noteContent == null) {
                                noteContent = doc.getString("notes");
                            }

                            String priority = doc.getString("priority");
                            if (priority == null) priority = "Basic";

                            String timeStr = sdfTime.format(taskDate);
                            String dateStr = sdfDate.format(taskDate);

                            Task t = new Task(id, title, category, timeStr, completed, dateStr, noteContent, priority);
                            t.setTaskDate(taskDate);
                            t.setUid(user.getUid());

                            t.setVibration(doc.getString("vibration"));
                            t.setRingtone(doc.getString("ringtone"));

                            tasks.add(t);
                        } catch (Exception e) {
                            Log.e("FirebaseRepo", "Error parsing task: " + doc.getId(), e);
                        }
                    }
                    listener.onTasksLoaded(tasks);
                })
                .addOnFailureListener(listener::onError);
    }

    // Cập nhật trường dữ liệu cho công việc
    public void updateTaskField(String taskId, String field, Object value, OnCompleteCallback callback) {
        if (taskId == null) return;

        db.collection("tasks").document(taskId)
                .update(field, value)
                .addOnSuccessListener(aVoid -> callback.onComplete("Updated", null))
                .addOnFailureListener(e -> callback.onComplete(null, e));
    }

    // Tải danh sách nhắc nhở (Reminders)
    public void loadReminders(OnTasksLoadedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("tasks")
                .whereEqualTo("uid", user.getUid())
                .whereEqualTo("reminder", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Task t = doc.toObject(Task.class);
                            t.setId(doc.getId());

                            if (t.getTaskDate() == null && doc.get("taskDate") instanceof com.google.firebase.Timestamp) {
                                t.setTaskDate(doc.getTimestamp("taskDate").toDate());
                            }

                            tasks.add(t);
                        } catch (Exception e) {
                            Log.e("FirebaseRepo", "Error parsing reminder task: " + doc.getId(), e);
                        }
                    }
                    listener.onTasksLoaded(tasks);
                })
                .addOnFailureListener(listener::onError);
    }


    // --- Lịch sử hành động (LOG) ---

    // Lưu log hành động thực hiện trên Task
    public void logTaskAction(String taskId, String taskTitle, String action) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        TaskLog log = new TaskLog(
                user.getUid(),
                taskId,
                taskTitle,
                action,
                new Date()
        );

        db.collection("task_logs").add(log);
    }

    // Lấy danh sách lịch sử log của Task
    public void getTaskLogs(OnLogLoadedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("task_logs")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        TaskLog log = doc.toObject(TaskLog.class);
                        log.setLogId(doc.getId());
                        logs.add(log);
                    }
                    listener.onLogsLoaded(logs);
                })
                .addOnFailureListener(listener::onError);
    }

    // --- Thành tích (ACHIEVEMENTS) ---

    // Tải số ngày liên tiếp (Streak) người dùng
    public void getUserStreak(OnStreakLoadedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    int streak = 0;
                    if (documentSnapshot.exists()) {
                        Long s = documentSnapshot.getLong("streak");
                        streak = (s != null) ? s.intValue() : 0;
                    }
                    listener.onStreakLoaded(streak);
                })
                .addOnFailureListener(listener::onError);
    }

    // Cập nhật số ngày liên tiếp (Streak) người dùng
    public void updateStreak() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    long currentStreak = 0;
                    if (documentSnapshot.contains("streak")) {
                        currentStreak = documentSnapshot.getLong("streak");
                    }

                    Date lastStreakDate = null;
                    if (documentSnapshot.contains("lastStreakDate")) {
                        lastStreakDate = documentSnapshot.getDate("lastStreakDate");
                    }

                    Calendar today = Calendar.getInstance();
                    resetTime(today);

                    Calendar lastDate = Calendar.getInstance();
                    if (lastStreakDate != null) {
                        lastDate.setTime(lastStreakDate);
                        resetTime(lastDate);
                    } else {
                        lastDate.setTimeInMillis(0);
                    }

                    if (lastStreakDate == null) {
                        updateUserStreakField(user.getUid(), 1);
                    } else if (today.compareTo(lastDate) == 0) {
                        Log.d("Streak", "Already updated for today");
                    } else {
                        lastDate.add(Calendar.DAY_OF_YEAR, 1);
                        if (today.compareTo(lastDate) == 0) {
                            updateUserStreakField(user.getUid(), currentStreak + 1);
                        } else {
                            updateUserStreakField(user.getUid(), 1);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Streak", "Error calculating streak", e));
    }

    // Cập nhật Streak cho người dùng
    private void updateUserStreakField(String uid, long newStreak) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("streak", newStreak);
        updates.put("lastStreakDate", new Date());

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Streak", "Streak updated to: " + newStreak));
    }

    // --- Thống kê (STATISTICS) ---

    // Lấy thống kê số lượng Task (Tổng số và Đã hoàn thành)
    public void getTaskStatistics(OnTaskStatsLoadedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        db.collection("tasks")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCreated = queryDocumentSnapshots.size();
                    int totalCompleted = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean completed = doc.getBoolean("completed");
                        if (completed != null && completed) {
                            totalCompleted++;
                        }
                    }
                    listener.onStatsLoaded(totalCreated, totalCompleted);
                })
                .addOnFailureListener(listener::onError);
    }

    // Hàm reset thời gian
    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
