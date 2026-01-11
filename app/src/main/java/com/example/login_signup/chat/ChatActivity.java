package com.example.login_signup.chat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.NotificationHelper;
import com.example.login_signup.R;
import com.example.login_signup.alarm.TaskReminderReceiver;
import com.example.login_signup.classes.Task;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

// Activity trò chuyện và quản lý công việc với trợ lý ảo AI (Gemini)
public class ChatActivity extends AppCompatActivity {
    public static final String AI_USER_ID = "AI";
    private static final String GEMINI_API_KEY = "AIzaSyDoIOAu5Nlu9IDin4-Q8QcntTwuR4-y43o";

    // AiState: Định nghĩa các trạng thái của cuộc hội thoại để xử lý logic từng bước
    private enum AiState { 
        IDLE, // Chờ lệnh mới
        AWAITING_TASK_TITLE, AWAITING_TASK_CATEGORY, AWAITING_TASK_NOTE, AWAITING_TASK_DATE, AWAITING_TASK_TIME, AWAITING_REMINDER, // Tạo task mới
        AWAITING_CLARIFICATION, // Chờ xác nhận khi tìm thấy nhiều task trùng tên
        AWAITING_DELETE_SELECTION, // Xóa task
        AWAITING_COMPLETE_SELECTION, // Task hoàn thành
        AWAITING_EDIT_SELECTION, AWAITING_EDIT_FIELD, AWAITING_NEW_VALUE // Chỉnh sửa task
    }

    private String sessionId; // ID phiên chat
    private GenerativeModelFutures model; // Model AI Gemini
    private Executor mainExecutor; // Executor chính để xử lý trên UI thread
    private AiState currentAiState = AiState.IDLE; // Trạng thái hiện tại của AI

    private Map<String, Object> context = new HashMap<>(); // Lưu dữ liệu tạm thời khi đang tạo/sửa task
    private List<ChatMessage> messageList = new ArrayList<>(); // Danh sách tin nhắn chat
    private ChatAdapter adapter; // Adapter cho RecyclerView chat

    // Các thành phần giao diện
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView btnSend;

    // Các đối tượng Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private CollectionReference messagesRef; // Tham chiếu đến Collection tin nhắn
    private CollectionReference tasksRef; // Tham chiếu đến Collection công việc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy ID phiên chat và cấu hình Toolbar
        sessionId = getIntent().getStringExtra("CHAT_SESSION_ID");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("CHAT_SESSION_NAME"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messagesRef = db.collection("chat_sessions").document(sessionId).collection("messages");
        tasksRef = db.collection("tasks");

        // Khởi tạo các thành phần giao diện
        rvChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.buttonSend);

        setupRecyclerView(); // Cấu hình RecyclerView
        loadMessages(); // Tải lịch sử tin nhắn

        // Xử lý sự kiện gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMessage());

        // Gửi tin nhắn chào mừng nếu là phòng chat mới
        messagesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                sendAiMessage("Chào bạn! Tôi là trợ lý ảo của bạn. Bạn có thể yêu cầu tôi:\n- Tạo công việc mới\n- Xem, sửa hoặc xóa công việc\n- Đánh dấu hoàn thành công việc");
            }
        });

        // Cấu hình Model AI Gemini
        GenerativeModel gm = new GenerativeModel(
                "gemma-3-27b-it",
                GEMINI_API_KEY,
                null,
                null
        );

        model = GenerativeModelFutures.from(gm); // Khởi tạo model
        mainExecutor = ContextCompat.getMainExecutor(this); // Executor chính
    }

    // Cấu hình RecyclerView
    private void setupRecyclerView() {
        // Khởi tạo adapter với tham số là danh sách tin nhắn
        adapter = new ChatAdapter(messageList);

        // Sử dụng Linear Layout cho RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Cuộn xuống cuối khi có tin nhắn mới

        // Đặt layout manager và adapter cho RecyclerView
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);
    }

    // Gửi tin nhắn của người dùng và yêu cầu AI xử lý
    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim(); // Lấy nội dung tin nhắn của người dùng
        FirebaseUser currentUser = auth.getCurrentUser(); // Lấy thông tin người dùng hiện tại

        if (messageText.isEmpty() || currentUser == null) return;

        // Tạo đối tượng cho tin nhắn của người dùng
        ChatMessage userMessage = new ChatMessage(messageText, currentUser.getUid());

        // Thêm tin nhắn vào Collection tin nhắn
        messagesRef.add(userMessage).addOnSuccessListener(docRef -> {
            etMessage.setText(""); // Xóa nội dung ở Edit Text sau khi gửi
            processMessageForAI(userMessage); // Gửi tin nhắn cho AI để xử lý
        });

        // Cập nhật phiên chat
        updateSession(messageText);
    }

    // Tải lịch sử tin nhắn từ Firestore theo thời gian thực.
    private void loadMessages() {
        // Sắp xếp tin nhắn theo thời gian tạo giảm dần
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if (error != null) return;
            messageList.clear();

            if (value != null) {
                // Lặp qua từng đoạn chat trong Collection tin nhắn
                for (QueryDocumentSnapshot doc : value) {
                    messageList.add(doc.toObject(ChatMessage.class));
                }
            }

            adapter.notifyDataSetChanged(); // Thông báo adapter đã cập nhật
            rvChat.scrollToPosition(messageList.size() - 1); // Cuộn xuống cuối khi có tin nhắn mới
        });
    }

     // Hiển thị tin nhắn của AI vào giao diện chat
    private void sendAiMessage(String messageText) {
        // Sử dụng Handler để thực thi lệnh sau một khoảng thời gian
        new Handler(getMainLooper()).postDelayed(() -> {
            ChatMessage aiMessage = new ChatMessage(messageText, AI_USER_ID);
            messagesRef.add(aiMessage); // Thêm tin nhắn AI vào Collection tin nhắn
            updateSession(messageText); // Cập nhật phiên chat
        }, 500);
    }

    // Cập nhật tin nhắn cuối cùng và thời gian tương tác vào phiên chat.
    private void updateSession(String lastMessage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastUpdated", FieldValue.serverTimestamp());
        db.collection("chat_sessions").document(sessionId).update(updates);
    }

    // Đưa cuộc hội thoại về trạng thái ban đầu.
    private void resetConversation() {
        currentAiState = AiState.IDLE;
        context.clear();
    }

    // Phân tích tin nhắn người dùng để quyết định hành động.
    private void processMessageForAI(ChatMessage userMessage) {
        String text = userMessage.getMessage();
        String textLower = text.toLowerCase();

        // Xử lý lệnh dừng/hủy
        if (textLower.equals("cancel") || textLower.equals("stop") || textLower.equals("hủy")) {
            sendAiMessage("Đã hủy thao tác hiện tại.");
            resetConversation();
            return;
        }

        // Nếu đang trong quá trình thu thập thông tin
        if (currentAiState != AiState.IDLE) {
            handleActiveStates(text, textLower); // Xử lý logic dựa trên trạng thái hiện tại
            return;
        }

        // Nếu ở trạng thái nghỉ, gửi tới Gemini để phân loại ý định
        callGeminiToClassifyIntent(text);
    }

    // Sử dụng Gemini API để phân loại ý định của người dùng
    private void callGeminiToClassifyIntent(String userText) {
        // Prompt để cho Gemini hiểu ngữ cảnh
        String prompt = "User says: \"" + userText + "\"\n\n" +
                "Instructions: You are a Task Manager AI.\n" +
                "- If user wants to CREATE a task, output exactly: INTENT_ADD\n" +
                "- If user wants to DELETE a task, output exactly: INTENT_DELETE\n" +
                "- If user wants to EDIT a task, output exactly: INTENT_EDIT\n" +
                "- If user wants to COMPLETE a task, output exactly: INTENT_COMPLETE\n" +
                "- If user wants to SHOW/LIST tasks, output exactly: INTENT_LIST\n" +
                "- Otherwise, provide a friendly, helpful conversational reply.";

        Content content = new Content.Builder().addText(prompt).build(); // Tạo nội dung cho request
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content); // Gửi request tới Gemini

        // Xử lý kết quả trả về từ Gemini
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiReply = result.getText().trim();
                handleGeminiResponse(aiReply, userText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                sendAiMessage("Sorry, I'm having trouble connecting to my brain right now.");
            }
        }, mainExecutor);
    }

    // Xử lý kết quả phân loại từ Gemini để chuyển đổi trạng thái AI.
    private void handleGeminiResponse(String aiReply, String originalUserText) {
        if (aiReply.contains("INTENT_ADD")) {
            currentAiState = AiState.AWAITING_TASK_TITLE;
            sendAiMessage("Sure! What is the title of the new task?");
        } else if (aiReply.contains("INTENT_DELETE")) {
            currentAiState = AiState.AWAITING_DELETE_SELECTION;
            sendAiMessage("I can help delete. What is the task title?");
        } else if (aiReply.contains("INTENT_EDIT")) {
            currentAiState = AiState.AWAITING_EDIT_SELECTION;
            sendAiMessage("Okay. Which task do you want to edit?");
        } else if (aiReply.contains("INTENT_COMPLETE")) {
            currentAiState = AiState.AWAITING_COMPLETE_SELECTION;
            sendAiMessage("Good job! Which task did you finish?");
        } else if (aiReply.contains("INTENT_LIST")) {
            showTasksFromFirestore();
        } else {
            // Nếu không phải lệnh quản lý task, trả về tin nhắn trò chuyện bình thường
            sendAiMessage(aiReply);
        }
    }

    // Điều hướng xử lý dựa trên trạng thái hiện tại của AI.
    private void handleActiveStates(String text, String textLower) {
        if (currentAiState == AiState.AWAITING_CLARIFICATION) {
            handleClarification(text);
            return;
        }

        switch (currentAiState) {
            case AWAITING_TASK_TITLE:
                context.put("title", text); askForCategory();
                break;
            case AWAITING_TASK_CATEGORY:
                String normalizedCategory = normalizeCategory(textLower);
                context.put("category", normalizedCategory);
                askForNote();
                break;
            case AWAITING_TASK_NOTE:
                context.put("note", text.equalsIgnoreCase("skip") ? "" : text);
                askForDate();
                break;
            case AWAITING_TASK_DATE:
                handleDate(textLower);
                break;
            case AWAITING_TASK_TIME:
                handleTime(textLower);
                break;
            case AWAITING_REMINDER:
                handleReminder(textLower);
                break;
            case AWAITING_DELETE_SELECTION:
                findTaskByTitle(text, "delete"); break;
            case AWAITING_COMPLETE_SELECTION:
                findTaskByTitle(text, "complete"); break;
            case AWAITING_EDIT_SELECTION:
                findTaskByTitle(text, "edit");
                break;
            case AWAITING_EDIT_FIELD:
                handleEditFieldSelection(textLower);
                break;
            case AWAITING_NEW_VALUE:
                handleNewValue(text);
                break;
            default: break;
        }
    }

    // Chuẩn hóa danh mục công việc từ tiếng Việt/Anh sang tiếng Anh để lưu trữ
    private String normalizeCategory(String input) {
        input = input.toLowerCase();
        if (input.contains("work") || input.contains("công việc") || input.contains("công ty")) {
            return "Work";
        } else if (input.contains("personal") || input.contains("cá nhân")) {
            return "Personal";
        } else if (input.contains("health") || input.contains("sức khỏe") || input.contains("y tế")) {
            return "Health";
        } else if (input.contains("shop") || input.contains("mua sắm")) {
            return "Shopping";
        }
        return "Personal"; 
    }

    // Các hàm hỏi thông tin tiếp theo trong quy trình tạo task
    private void askForCategory() {
        currentAiState = AiState.AWAITING_TASK_CATEGORY; sendAiMessage("Got it. What category is the task \"" + context.get("title") + "\"? (e.g., Work, Personal)");
    }
    private void askForNote() {
        currentAiState = AiState.AWAITING_TASK_NOTE; sendAiMessage("Okay. Any additional notes? (or say 'skip')");
    }
    private void askForDate() {
        currentAiState = AiState.AWAITING_TASK_DATE; sendAiMessage("When is it due? (e.g., 'today', 'tomorrow', 'next Friday', or '25/12')");
    }
    private void askForTime() {
        currentAiState = AiState.AWAITING_TASK_TIME; sendAiMessage("Got it. And at what time? (e.g., '9am', '5:30pm', 'noon')");
    }
    private void askForReminder() {
        currentAiState = AiState.AWAITING_REMINDER; sendAiMessage("One last thing. Set a reminder? (yes/no)");
    }

    // Xử lý định dạng ngày tháng từ ngôn ngữ tự nhiên
    private void handleDate(String text) {
        Calendar cal = Calendar.getInstance();
        boolean dateParsed = false;
        if (text.equals("today")||text.equals("hôm nay")||text.equals("nay")) {
            dateParsed = true;
        } else if (text.equals("tomorrow")||text.equals("mai")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            dateParsed = true;
        } else if (text.startsWith("next ")) {
            String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
            for (int i = 0; i < days.length; i++) {
                if (text.contains(days[i])) {
                    int targetDay = i + 1;
                    int currentDay = cal.get(Calendar.DAY_OF_WEEK);
                    int daysToAdd = targetDay - currentDay;
                    if (daysToAdd <= 0) { daysToAdd += 7; }
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    dateParsed = true;
                    break;
                }
            }
        } else {
            try {
                SimpleDateFormat sdf = text.length() > 5 ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) : new SimpleDateFormat("dd/MM", Locale.getDefault());
                Date parsedDate = sdf.parse(text);
                if (text.length() <= 5) {
                    Calendar parsedCal = Calendar.getInstance();
                    parsedCal.setTime(parsedDate);
                    parsedCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                    if(parsedCal.before(cal)){
                        parsedCal.add(Calendar.YEAR, 1);
                    }
                    cal.setTime(parsedCal.getTime());
                } else {
                    cal.setTime(parsedDate);
                }
                dateParsed = true;
            } catch (ParseException e) {  }
        }

        if (dateParsed) {
            context.put("date", cal.getTime());
            askForTime();
        } else {
            sendAiMessage("I couldn't understand that date. Please try saying 'today', 'tomorrow', 'next Monday', or a date like '25/12'.");
        }
    }

    // Xử lý định dạng thời gian từ ngôn ngữ tự nhiên
    private void handleTime(String text) {
        Date date = (Date) context.get("date");
        if (date == null) { resetConversation(); sendAiMessage("Something went wrong. Let's start over."); return; }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        boolean timeParsed = false;
        try {
            if (text.equals("noon")) {
                cal.set(Calendar.HOUR_OF_DAY, 12); cal.set(Calendar.MINUTE, 0);
                timeParsed = true;
            } else if (text.endsWith("am") || text.endsWith("pm")) {
                String numPart = text.replaceAll("[^0-9:]", "");
                int hour = 0, minute = 0;
                if(numPart.contains(":")) { String[] parts = numPart.split(":"); hour = Integer.parseInt(parts[0]); minute = Integer.parseInt(parts[1]); } else { hour = Integer.parseInt(numPart); }
                if (text.endsWith("pm") && hour < 12) hour += 12;
                if (text.endsWith("am") && hour == 12) hour = 0;
                cal.set(Calendar.HOUR_OF_DAY, hour); cal.set(Calendar.MINUTE, minute);
                timeParsed = true;
            } else if (text.contains(":")) {
                String[] parts = text.split(":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                timeParsed = true;
            }

            if (timeParsed) {
                cal.set(Calendar.SECOND, 0);
                context.put("finalDate", cal.getTime());
                askForReminder();
            } else {
                sendAiMessage("I'm sorry, I didn't get that time. Please use a format like '9am', '5:30pm', or '17:30'.");
            }
        } catch (Exception e) {
            sendAiMessage("I'm sorry, I had trouble understanding that time. Please use a format like '9am', '5:30pm', or '17:30'.");
        }
    }

    // Xử lý thông báo về việc đặt nhở
    private void handleReminder(String text) {
        context.put("reminder", text.contains("có") || text.contains("yes") || text.contains("đặt"));
        addTaskToFirestore();
    }

    // Thêm công việc vào Firestore
    private void addTaskToFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        Task newTask = new Task();
        newTask.setUid(currentUser.getUid());
        newTask.setTitle((String) context.get("title"));
        newTask.setCategory((String) context.get("category"));
        newTask.setNote((String) context.get("note"));
        newTask.setCompleted(false);
        newTask.setTaskDate((Date) context.get("finalDate"));
        newTask.setReminder((boolean) context.getOrDefault("reminder", false));
        newTask.setVibration("Default");

        tasksRef.add(newTask).addOnSuccessListener(docRef -> {
            sendAiMessage("Excellent! I've added the task \"" + newTask.getTitle() + "\" to your list.");
            if (newTask.isReminder()) {
                scheduleTaskReminderFromChat(docRef.getId(), newTask);
            }
            resetConversation();
        }).addOnFailureListener(e -> {
            sendAiMessage("I'm sorry, there was an error adding the task.");
            resetConversation();
        });
    }

    // Tìm công việc theo tiêu đề
    private void findTaskByTitle(String inputTitle, String action) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { resetConversation(); return; }

        tasksRef.whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        sendAiMessage("You don't have any tasks to " + action + ".");
                        resetConversation();
                        return;
                    }

                    List<DocumentSnapshot> matchedDocs = new ArrayList<>();
                    String searchKey = inputTitle.toLowerCase().trim();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String dbTitle = doc.getString("title");
                        if (dbTitle != null) {
                            String dbTitleLower = dbTitle.toLowerCase();

                            if (dbTitleLower.contains(searchKey)) {
                                matchedDocs.add(doc);
                            }
                        }
                    }

                    if (matchedDocs.isEmpty()) {
                        sendAiMessage("I couldn't find any task containing \"" + inputTitle + "\". Please try again.");
                        resetConversation();
                    } else if (matchedDocs.size() == 1) {
                        performActionOnTask(matchedDocs.get(0), action);
                    } else {
                        currentAiState = AiState.AWAITING_CLARIFICATION;
                        context.put("action", action);
                        context.put("clarificationDocs", matchedDocs);

                        StringBuilder sb = new StringBuilder("I found multiple tasks matching \"" + inputTitle + "\". Which one?\n");
                        for (int i = 0; i < matchedDocs.size(); i++) {
                            DocumentSnapshot doc = matchedDocs.get(i);
                            Date date = doc.getDate("taskDate");
                            String dateStr = (date != null) ? formatDate(date) : "No date";

                            sb.append((i + 1)).append(". ")
                                    .append(doc.getString("title"))
                                    .append(" (due ").append(dateStr).append(")\n");
                        }
                        sb.append("Reply with the number (e.g., '1').");
                        sendAiMessage(sb.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    sendAiMessage("Error searching for tasks: " + e.getMessage());
                    resetConversation();
                });
    }

    // Xử lý lựa chọn số thứ tự từ người dùng.
    private void handleClarification(String text) {
        try {
            int choice = Integer.parseInt(text.trim()) - 1;
            List<DocumentSnapshot> docs = (List<DocumentSnapshot>) context.get("clarificationDocs");
            String action = (String) context.get("action");

            if (docs != null && choice >= 0 && choice < docs.size()) {
                performActionOnTask(docs.get(choice), action);
            } else {
                sendAiMessage("That's not a valid number. Please try again.");
            }
        } catch (NumberFormatException e) {
            sendAiMessage("Please reply with just the number.");
        }
    }

    // Thực thi hành động cuối cùng (Xóa/Hoàn thành/Sửa) trên một Document cụ thể.
    private void performActionOnTask(DocumentSnapshot doc, String action) {
        switch (action) {
            case "delete":
                cancelScheduledNotifications(doc.getId());
                doc.getReference().delete();
                sendAiMessage("Done. I've deleted the task: \"" + doc.getString("title") + "\".");
                resetConversation();
                break;
            case "complete":
                cancelScheduledNotifications(doc.getId());
                doc.getReference().update("completed", true);
                sendAiMessage("Great work! I've marked \"" + doc.getString("title") + "\" as completed.");
                resetConversation();
                break;
            case "edit":
                currentAiState = AiState.AWAITING_EDIT_FIELD;
                context.put("taskDoc", doc);
                sendAiMessage("Okay, editing \"" + doc.getString("title") + "\". What would you like to change? (title, note, date, or time)");
                break;
        }
    }

    // Xử lý việc chọn trường cần sửa (Title, Note, Date, Time).
    private void handleEditFieldSelection(String field) {
        if (field.matches("title|note|date|time")) {
            currentAiState = AiState.AWAITING_NEW_VALUE;
            context.put("fieldToEdit", field);
            sendAiMessage("Okay, what should the new " + field + " be?");
        } else {
            sendAiMessage("You can only edit the title, note, date, or time. Which one would you like?");
        }
    }

    // Xử lý việc nhập giá trị mới cho trường được chọn
    private void handleNewValue(String value) {
        DocumentSnapshot doc = (DocumentSnapshot) context.get("taskDoc");
        String field = (String) context.get("fieldToEdit");
        if (doc == null || field == null) { resetConversation(); return; }

        Object newValue = value;
        if (field.equals("date") || field.equals("time")) {
            sendAiMessage("For date/time changes, please use the specific prompts. Let's restart the edit. What would you like to change?");
            currentAiState = AiState.AWAITING_EDIT_FIELD;
            return;
        }

        doc.getReference().update(field, newValue).addOnSuccessListener(aVoid -> {
            sendAiMessage("Success! I've updated the " + field + " of your task.");
            if (field.equals("date") || field.equals("time")) {
                cancelScheduledNotifications(doc.getId());
                doc.getReference().get().addOnSuccessListener(updatedDoc -> {
                    Task task = updatedDoc.toObject(Task.class);
                    if (task != null && task.isReminder()) {
                        scheduleTaskReminderFromChat(updatedDoc.getId(), task);
                    }
                });
            }
            resetConversation();
        });
    }
    
    // Đặt báo thức cho công việc
    private void scheduleTaskReminderFromChat(String taskId, Task task) {
        long dueTime = task.getTaskDate().getTime();
        scheduleNotification(taskId, task, dueTime, false);
        
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;
        
        if (dueTime > System.currentTimeMillis() + twentyFourHoursInMillis) {
            scheduleNotification(taskId + "_advance", task, advanceTime, true);
        } else if (dueTime > System.currentTimeMillis()) {
            String taskInfo = "Title: " + task.getTitle() + "\nCategory: " + task.getCategory() + "\nNote: " + task.getNote();
            NotificationHelper.showAdvanceNotification(this, task.getTitle(), taskInfo, (taskId + "_advance").hashCode(), taskId);
        }
    }

    // Hủy bỏ các báo thức đã đăng ký với hệ thống.
    private void cancelScheduledNotifications(String taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        
        Intent mainIntent = new Intent(this, TaskReminderReceiver.class);
        alarmManager.cancel(PendingIntent.getBroadcast(this, taskId.hashCode(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        
        Intent advanceIntent = new Intent(this, TaskReminderReceiver.class);
        alarmManager.cancel(PendingIntent.getBroadcast(this, (taskId + "_advance").hashCode(), advanceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
    }
    
    private void scheduleNotification(String uniqueTaskId, Task task, long time, boolean isAdvance) {
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        intent.putExtra("taskId", uniqueTaskId);
        intent.putExtra("title", task.getTitle());
        intent.putExtra("note", task.getNote());
        intent.putExtra("category", task.getCategory());
        intent.putExtra("isAdvance", isAdvance);
        intent.putExtra("vibration", task.getVibration());

        if (task.getRingtone() != null) {
            intent.putExtra("ringtone", task.getRingtone());
        }

        int requestCode = uniqueTaskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        try {
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } catch (SecurityException se) { }
    }

    // Truy vấn và hiển thị toàn bộ danh sách công việc của người dùng hiện tại
    private void showTasksFromFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) return;
        tasksRef.whereEqualTo("uid", currentUser.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        sendAiMessage("You have no tasks at the moment.");
                    } else {
                        StringBuilder taskListStr = new StringBuilder("Here are your current tasks:\n");
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Task task = doc.toObject(Task.class);
                            taskListStr.append("- ").append(task.getTitle()).append("\n");
                        }
                        sendAiMessage(taskListStr.toString());
                    }
                });
    }

    // Định dạng ngày tháng
    private String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
    }

    // Xử lý sự kiện khi nhấn nút Home
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}