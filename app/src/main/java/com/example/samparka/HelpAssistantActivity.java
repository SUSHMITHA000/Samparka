package com.example.samparka;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;


import java.util.ArrayList;
import java.util.Locale;

public class HelpAssistantActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    private LinearLayout chatContainer;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnMic; // Add a field for the mic button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_assistant);

        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnRecordVoice); // Initialize the mic button

        // Add welcome message
        addBotMessage("Hello! How can I help you today?", "10:30 AM");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text, "10:31 AM");
                etMessage.setText("");
                // Demo: respond for known message
                if (text.equalsIgnoreCase("How do I report a complaint?")) {
                    addBotMessage("To report a complaint, tap the \"Report Issue\" button on the home screen. Then upload a photo, select the issue type, and provide a description. Your location will be automatically tagged.", "10:31 AM");
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

        // Add mic button click listener for voice input
        btnMic.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Speech not supported", Toast.LENGTH_SHORT).show();
            }
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

    // Handle speech-to-text result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etMessage.setText(result.get(0));
                etMessage.setSelection(etMessage.getText().length());
            }
        }
    }
}
