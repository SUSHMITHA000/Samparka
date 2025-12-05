package com.example.samparka;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HelpAssistantActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_assistant);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Add welcome message
        addBotMessage("Hello! How can I help you today?");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                etMessage.setText("");
                respondToUserMessage(text);
            }
        });

        Button btnReportIssue = findViewById(R.id.btnReportIssue);
        Button btnTrackComplaint = findViewById(R.id.btnTrackComplaint);
        Button btnContact = findViewById(R.id.btnContact);

        btnReportIssue.setOnClickListener(v -> {
            String question = "How do I report a complaint?";
            addUserMessage(question);
            respondToUserMessage(question);
        });

        btnTrackComplaint.setOnClickListener(v -> {
            String question = "How can I track my complaint?";
            addUserMessage(question);
            respondToUserMessage(question);
        });

        btnContact.setOnClickListener(v -> {
            String question = "Contact Panchayat";
            addUserMessage(question);
            respondToUserMessage(question);
        });
    }

    private void respondToUserMessage(String message) {
        if (message.equalsIgnoreCase("How do I report a complaint?")) {
            addBotMessage("To report a complaint, tap the \"Report Issue\" button on the home screen. Then upload a photo, select the issue type, and provide a description. Your location will be automatically tagged.");
        } else if (message.equalsIgnoreCase("How can I track my complaint?")) {
            addBotMessage("You can track your complaint status in the 'My Reports' section of the app. It will show you if your complaint is 'In Progress' or 'Resolved'.");
        } else if (message.equalsIgnoreCase("Contact Panchayat")) {
            addBotMessage("You can contact the Panchayat office during working hours. The address is [Panchayat Address] and the phone number is [Panchayat Phone Number].");
        } else {
            addBotMessage("I'm sorry, I don't understand that question.");
        }
    }

    private void addBotMessage(String message) {
        messageList.add(new Message(message, false));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void addUserMessage(String message) {
        messageList.add(new Message(message, true));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }
}
