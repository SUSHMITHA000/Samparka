package com.example.samparka;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HelpAssistantActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_assistant);

        initViews();
        setupRecyclerView();
        setupSession();
        setupClickListeners();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);
    }

    private void setupSession() {
        sessionId = UUID.randomUUID().toString();
        addBotMessage("Hello! Your AI chatbot is ready. Try 'pothole near my house'");
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        findViewById(R.id.btnReportIssue).setOnClickListener(v ->
                sendPredefinedMessage("How do I report a complaint?"));

        findViewById(R.id.btnTrackComplaint).setOnClickListener(v ->
                sendPredefinedMessage("How can I track my complaint?"));

        findViewById(R.id.btnContact).setOnClickListener(v ->
                sendPredefinedMessage("Contact Panchayat"));
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            addUserMessage(text);
            etMessage.setText("");
            sendToBackend(text);
        }
    }

    private void sendPredefinedMessage(String message) {
        addUserMessage(message);
        sendToBackend(message);
    }

    private void sendToBackend(String text) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject json = new JSONObject();
                json.put("text", text);
                json.put("sessionId", sessionId);

                try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
                    wr.write(json.toString());
                }

                int responseCode = conn.getResponseCode();
                String reply = "";

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader rd = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()))) {
                        reply = rd.readLine();
                    }
                } else {
                    reply = "{\"reply\": \"Server error: " + responseCode + "\"}";
                }

                JSONObject responseJson = new JSONObject(reply);
                String botReply = responseJson.optString("reply", "Sorry, no response");

                runOnUiThread(() -> addBotMessage(botReply.replace("\\n", "\n")));

            } catch (Exception e) {
                runOnUiThread(() ->
                        addBotMessage("Connection failed: " + e.getMessage()));
            }
        }).start();
    }

    private void addBotMessage(String message) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, false));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        });
    }

    private void addUserMessage(String message) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, true));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        });
    }
}
