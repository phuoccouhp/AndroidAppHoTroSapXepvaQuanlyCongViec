package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;

    // State machine for conversation flow
    private enum ConversationState { IDLE, AWAITING_NAME, AWAITING_TYPE, AWAITING_NOTE, AWAITING_REMINDER_CHOICE, AWAITING_DATETIME }
    private ConversationState currentState = ConversationState.IDLE;
    private HashMap<String, String> taskDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        recyclerView.setAdapter(chatAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            String userInput = messageEditText.getText().toString().trim();
            if (userInput.isEmpty()) return;

            addToChat(userInput, true); // Add user's message to chat
            messageEditText.setText("");
            processUserInput(userInput); // Process the input based on current state
        });

        // Initial welcome message
        addToChat("Hello! You can ask me to 'create a new task' to get started.", false);
    }

    void addToChat(String text, boolean isUser) {
        runOnUiThread(() -> {
            messageList.add(new Message(text, isUser));
            chatAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
        });
    }

    private void processUserInput(String input) {
        switch (currentState) {
            case IDLE:
                if (input.toLowerCase().contains("create") && input.toLowerCase().contains("task")) {
                    currentState = ConversationState.AWAITING_NAME;
                    addToChat("Great! What should I name the task?", false);
                } else {
                    addToChat("Sorry, I can only help with creating tasks right now. Try 'create a new task'.", false);
                }
                break;
            case AWAITING_NAME:
                taskDetails.put("name", input);
                currentState = ConversationState.AWAITING_TYPE;
                addToChat("Got it. What type of task is this? (e.g., Work, Personal, Shopping)", false);
                break;
            case AWAITING_TYPE:
                taskDetails.put("type", input);
                currentState = ConversationState.AWAITING_NOTE;
                addToChat("Any notes for this task? If not, just say 'no'.", false);
                break;
            case AWAITING_NOTE:
                taskDetails.put("note", input.equalsIgnoreCase("no") ? "" : input);
                currentState = ConversationState.AWAITING_REMINDER_CHOICE;
                addToChat("Would you like to set a reminder? (yes/no)", false);
                break;
            case AWAITING_REMINDER_CHOICE:
                if (input.equalsIgnoreCase("yes")) {
                    currentState = ConversationState.AWAITING_DATETIME;
                    addToChat("When should I remind you? (e.g., Tomorrow at 5pm)", false);
                } else {
                    finalizeTaskCreation();
                }
                break;
            case AWAITING_DATETIME:
                taskDetails.put("reminder", input);
                finalizeTaskCreation();
                break;
        }
    }

    private void finalizeTaskCreation() {
        addToChat("Okay, I'm setting up the task details for you now.", false);

        // Use a Handler to delay navigation for a better user experience
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(ChatActivity.this, AddTaskActivity.class);
            intent.putExtra("taskName", taskDetails.get("name"));
            intent.putExtra("taskType", taskDetails.get("type"));
            intent.putExtra("taskNote", taskDetails.get("note"));
            intent.putExtra("taskReminder", taskDetails.get("reminder"));
            startActivity(intent);

            // Reset state for the next conversation
            currentState = ConversationState.IDLE;
            taskDetails.clear();
        }, 1500); // 1.5-second delay
    }
}
