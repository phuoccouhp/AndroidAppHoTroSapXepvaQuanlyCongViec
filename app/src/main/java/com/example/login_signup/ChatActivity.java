package com.example.login_signup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    public static final String AI_USER_ID = "AI";

    private enum AiState { 
        IDLE, 
        AWAITING_TASK_TITLE, AWAITING_TASK_CATEGORY, AWAITING_TASK_NOTE, AWAITING_TASK_DATE, AWAITING_TASK_TIME, AWAITING_REMINDER,
        AWAITING_CLARIFICATION,
        AWAITING_DELETE_SELECTION,
        AWAITING_COMPLETE_SELECTION,
        AWAITING_EDIT_SELECTION, AWAITING_EDIT_FIELD, AWAITING_NEW_VALUE
    }

    private AiState currentAiState = AiState.IDLE;
    private Map<String, Object> context = new HashMap<>();

    private RecyclerView recyclerViewChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private EditText editTextMessage;
    private ImageView buttonSend;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference messagesRef;
    private CollectionReference tasksRef;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionId = getIntent().getStringExtra("CHAT_SESSION_ID");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("CHAT_SESSION_NAME"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messagesRef = db.collection("chat_sessions").document(sessionId).collection("messages");
        tasksRef = db.collection("tasks");

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        setupRecyclerView();
        loadMessages();

        buttonSend.setOnClickListener(v -> sendMessage());

        messagesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                sendAiMessage("Hello! I am your virtual assistant. You can ask me to:\n- Create a new task\n- Show, edit, or delete tasks\n- Mark a task as completed");
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(adapter);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (messageText.isEmpty() || currentUser == null) return;

        ChatMessage userMessage = new ChatMessage(messageText, currentUser.getUid());
        messagesRef.add(userMessage).addOnSuccessListener(docRef -> {
            editTextMessage.setText("");
            processMessageForAI(userMessage);
        });
        updateSession(messageText);
    }

    private void loadMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if (error != null) return;
            messageList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) { messageList.add(doc.toObject(ChatMessage.class)); }
            }
            adapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
        });
    }

    private void sendAiMessage(String messageText) {
        new Handler(getMainLooper()).postDelayed(() -> {
            ChatMessage aiMessage = new ChatMessage(messageText, AI_USER_ID);
            messagesRef.add(aiMessage);
            updateSession(messageText);
        }, 500);
    }

    private void updateSession(String lastMessage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastUpdated", FieldValue.serverTimestamp());
        db.collection("chat_sessions").document(sessionId).update(updates);
    }

    private void resetConversation() {
        currentAiState = AiState.IDLE;
        context.clear();
    }

    private void processMessageForAI(ChatMessage userMessage) {
        String text = userMessage.getMessage();
        String textLower = text.toLowerCase();

        if (textLower.equals("cancel") || textLower.equals("stop")) {
            sendAiMessage("Okay, I've cancelled the current operation.");
            resetConversation();
            return;
        }

        if (currentAiState == AiState.AWAITING_CLARIFICATION) {
            handleClarification(text);
            return;
        }

        switch (currentAiState) {
            case IDLE:                      handleIdleState(textLower); break;
            case AWAITING_TASK_TITLE:       context.put("title", text); askForCategory(); break;
            case AWAITING_TASK_CATEGORY:
                String normalizedCategory = normalizeCategory(textLower);
                context.put("category", normalizedCategory);
                askForNote();
                break;
            case AWAITING_TASK_NOTE:        context.put("note", text.equalsIgnoreCase("skip") ? "" : text); askForDate(); break;
            case AWAITING_TASK_DATE:        handleDate(textLower); break;
            case AWAITING_TASK_TIME:        handleTime(textLower); break;
            case AWAITING_REMINDER:         handleReminder(textLower); break;
            case AWAITING_DELETE_SELECTION: findTaskByTitle(text, "delete"); break;
            case AWAITING_COMPLETE_SELECTION: findTaskByTitle(text, "complete"); break;
            case AWAITING_EDIT_SELECTION:   findTaskByTitle(text, "edit"); break;
            case AWAITING_EDIT_FIELD:       handleEditFieldSelection(textLower); break;
            case AWAITING_NEW_VALUE:        handleNewValue(text); break;
        }
    }

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

    
    private void handleIdleState(String text) {
        if (text.matches(".*\\b(add|create|new|make)\\b.*")) {
            currentAiState = AiState.AWAITING_TASK_TITLE;
            sendAiMessage("Great! What is the title of the new task?");
        } else if (text.matches(".*\\b(delete|remove|get rid of)\\b.*")) {
            currentAiState = AiState.AWAITING_DELETE_SELECTION;
            sendAiMessage("I can do that. What is the title of the task you want to delete?");
        } else if (text.matches(".*\\b(edit|change|update)\\b.*.*")) {
            currentAiState = AiState.AWAITING_EDIT_SELECTION;
            sendAiMessage("Sure. What is the title of the task you want to edit?");
        } else if (text.matches(".*\\b(complete|finish|done)\\b.*.*")) {
            currentAiState = AiState.AWAITING_COMPLETE_SELECTION;
            sendAiMessage("Excellent! What is the title of the task you completed?");
        } else if (text.matches(".*\\b(show|list|view|what are|tasks)\\b.*.*")) {
            showTasksFromFirestore();
        } else {
            sendAiMessage("Sorry, I don't understand that yet. I can help you add, show, edit, delete or complete tasks.");
        }
    }

    private void askForCategory() { currentAiState = AiState.AWAITING_TASK_CATEGORY; sendAiMessage("Got it. What category is the task \"" + context.get("title") + "\"? (e.g., Work, Personal)"); }
    private void askForNote() { currentAiState = AiState.AWAITING_TASK_NOTE; sendAiMessage("Okay. Any additional notes? (or say 'skip')"); }
    private void askForDate() { currentAiState = AiState.AWAITING_TASK_DATE; sendAiMessage("When is it due? (e.g., 'today', 'tomorrow', 'next Friday', or '25/12')"); }
    private void askForTime() { currentAiState = AiState.AWAITING_TASK_TIME; sendAiMessage("Got it. And at what time? (e.g., '9am', '5:30pm', 'noon')"); }
    private void askForReminder() { currentAiState = AiState.AWAITING_REMINDER; sendAiMessage("One last thing. Set a reminder? (yes/no)"); }

    private void handleDate(String text) {
        Calendar cal = Calendar.getInstance();
        boolean dateParsed = false;
        if (text.equals("today")) {
            dateParsed = true; 
        } else if (text.equals("tomorrow")) {
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

    private void handleReminder(String text) {
        context.put("reminder", text.equalsIgnoreCase("yes"));
        addTaskToFirestore();
    }

    private void addTaskToFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        com.example.login_signup.Task newTask = new com.example.login_signup.Task();
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

    private void findTaskByTitle(String title, String action) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { resetConversation(); return; }

        tasksRef.whereEqualTo("uid", user.getUid()).whereGreaterThanOrEqualTo("title", title).whereLessThanOrEqualTo("title", title + "\uf8ff")
        .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                sendAiMessage("I couldn't find any task with a similar title. Please try again.");
                resetConversation();
            } else if (queryDocumentSnapshots.size() == 1) {
                performActionOnTask(queryDocumentSnapshots.getDocuments().get(0), action);
            } else {
                currentAiState = AiState.AWAITING_CLARIFICATION;
                context.put("action", action);
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                context.put("clarificationDocs", docs);
                StringBuilder sb = new StringBuilder("I found multiple tasks. Which one did you mean?\n");
                for (int i = 0; i < docs.size(); i++) {
                    sb.append((i + 1)).append(". ").append(docs.get(i).getString("title")).append(" (due ").append(formatDate(docs.get(i).getDate("taskDate"))).append(")\n");
                }
                sb.append("Please reply with the number (e.g., '1').");
                sendAiMessage(sb.toString());
            }
        });
    }

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

    private void handleEditFieldSelection(String field) {
        if (field.matches("title|note|date|time")) {
            currentAiState = AiState.AWAITING_NEW_VALUE;
            context.put("fieldToEdit", field);
            sendAiMessage("Okay, what should the new " + field + " be?");
        } else {
            sendAiMessage("You can only edit the title, note, date, or time. Which one would you like?");
        }
    }

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
                    com.example.login_signup.Task task = updatedDoc.toObject(com.example.login_signup.Task.class);
                    if (task != null && task.isReminder()) {
                        scheduleTaskReminderFromChat(updatedDoc.getId(), task);
                    }
                });
            }
            resetConversation();
        });
    }
    
    private void scheduleTaskReminderFromChat(String taskId, com.example.login_signup.Task task) {
         long dueTime = task.getTaskDate().getTime();
        scheduleNotification(taskId, task, dueTime, false);
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;
        if (dueTime > System.currentTimeMillis() + twentyFourHoursInMillis) {
            scheduleNotification(taskId + "_advance", task, advanceTime, true);
        } else if (dueTime > System.currentTimeMillis()) {
            String taskInfo = "Title: " + task.getTitle() + "\nCategory: " + task.getCategory() + "\nNote: " + task.getNote();
            NotificationHelper.showAdvanceNotification(this, task.getTitle(), taskInfo, (taskId + "_advance").hashCode());
        }
    }

    private void cancelScheduledNotifications(String taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        Intent mainIntent = new Intent(this, TaskReminderReceiver.class);
        alarmManager.cancel(PendingIntent.getBroadcast(this, taskId.hashCode(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        Intent advanceIntent = new Intent(this, TaskReminderReceiver.class);
        alarmManager.cancel(PendingIntent.getBroadcast(this, (taskId + "_advance").hashCode(), advanceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        Log.d(TAG, "Cancelled alarms for taskId: " + taskId);
    }
    
    private void scheduleNotification(String uniqueTaskId, com.example.login_signup.Task task, long time, boolean isAdvance) {
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
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException: Could not schedule exact alarm.", se);
        }
    }

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
                        com.example.login_signup.Task task = doc.toObject(com.example.login_signup.Task.class);
                        taskListStr.append("- ").append(task.getTitle()).append("\n");
                    }
                    sendAiMessage(taskListStr.toString());
                }
            });
    }
    private String formatDate(Date date) { return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date); }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) { if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; } return super.onOptionsItemSelected(item); }
}