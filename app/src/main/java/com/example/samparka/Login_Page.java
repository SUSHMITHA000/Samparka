package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login_Page extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private SignInButton googleSignInButton;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    // Google Sign-In launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseGoogleAuth(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // ---------------- GOOGLE SIGN-IN -----------------
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // must match google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // ---------------- EMAIL + PASSWORD LOGIN -----------------
        loginButton.setOnClickListener(v -> loginWithEmail());
    }

    // ===================== MANUAL EMAIL LOGIN =====================
    private void loginWithEmail() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try LOGIN first
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    saveToFirestore(email);
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                })
                .addOnFailureListener(e -> {

                    // IF LOGIN FAILS → Auto Create Account
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                saveToFirestore(email);
                                Toast.makeText(this, "Account Created & Logged In!", Toast.LENGTH_SHORT).show();
                                goToDashboard();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(this, "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    // ===================== SAVE USER TO FIRESTORE =====================
    private void saveToFirestore(String email) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(a -> {})
                .addOnFailureListener(e -> {});
    }

    // ===================== GOOGLE LOGIN =====================
    private void firebaseGoogleAuth(String idToken) {

        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    String email = firebaseAuth.getCurrentUser().getEmail();
                    saveToFirestore(email);
                    Toast.makeText(this, "Google Login Successful!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ===================== NAVIGATE TO DASHBOARD =====================
    private void goToDashboard() {
        Intent intent = new Intent(Login_Page.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
