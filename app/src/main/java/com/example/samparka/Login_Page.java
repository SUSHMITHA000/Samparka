package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login_Page extends AppCompatActivity {

    EditText nameEdit, emailEdit, passwordEdit;
    Button loginButton;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {

        String name = nameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String pass = passwordEdit.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to SIGN IN first
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> saveToFirestore(name, email))
                .addOnFailureListener(e -> {

                    // If login fails → Create new account
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(r -> saveToFirestore(name, email))
                            .addOnFailureListener(er ->
                                    Toast.makeText(this, "Failed: " + er.getMessage(), Toast.LENGTH_LONG).show());
                });
    }

    private void saveToFirestore(String name, String email) {
        String uid = auth.getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", "");
        data.put("pin", "");
        data.put("city", "");
        data.put("state", "");
        data.put("location", "");
        data.put("photoUrl", "");

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(a -> {
                    // Navigate only after Firestore is ready
                    startActivity(new Intent(Login_Page.this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
