package com.example.samparka;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class HelpAssistantActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;

    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_assistant);

        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Add welcome message
        addBotMessage("Hello! How can I help you today?", "10:30 AM");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text, "10:31 AM");
                etMessage.setText("");
                // Demo: respond for known message (no translation)
                if (text.equalsIgnoreCase("How do I report a complaint?")) {
                    addBotMessage(
                            "To report a complaint, tap the \"Report Issue\" button on the home screen. Then upload a photo, select the issue type, and provide a description. Your location will be automatically tagged.",
                            "10:31 AM"
                    );
                }
            }
        });

        Button btnReportIssue = findViewById(R.id.btnReportIssue);
        Button btnTrackComplaint = findViewById(R.id.btnTrackComplaint);
        Button btnContact = findViewById(R.id.btnContact);

        btnReportIssue.setOnClickListener(v -> {
            etMessage.setText("How do I report a complaint?");
            etMessage.setSelection(etMessage.getText().length());
        });

        btnTrackComplaint.setOnClickListener(v -> {
            etMessage.setText("How can I track my complaint?");
            etMessage.setSelection(etMessage.getText().length());
        });

        btnContact.setOnClickListener(v -> {
            etMessage.setText("Contact Panchayat");
            etMessage.setSelection(etMessage.getText().length());
        });
    }

    // Helper to add bot message
    private void addBotMessage(String message, String time) {
        View view = getLayoutInflater().inflate(R.layout.item_bot_message, chatContainer, false);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);
        ((TextView) view.findViewById(R.id.txtTime)).setText(time);
        chatContainer.addView(view);
    }

    // Helper to add user message
    private void addUserMessage(String message, String time) {
        View view = getLayoutInflater().inflate(R.layout.item_user_message, chatContainer, false);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);
        ((TextView) view.findViewById(R.id.txtTime)).setText(time);
        chatContainer.addView(view);
    }
}
