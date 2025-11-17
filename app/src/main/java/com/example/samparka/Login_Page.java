package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.android.gms.common.SignInButton;


public class Login_Page extends AppCompatActivity {

    private EditText emailEditText;
    private Button loginButton;
    private com.google.android.gms.common.SignInButton googleSignInButton;


    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    // Google Sign-In Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseGoogleAuth(account.getIdToken());
                    } catch (ApiException e) {
                        Log.d("Error fatal0", e.toString());
                        Toast.makeText(this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        firebaseAuth = FirebaseAuth.getInstance();

        // ------------ GOOGLE SIGN-IN CONFIG ------------
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // MUST MATCH google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ------------ EMAIL LOGIN BUTTON ------------
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(Login_Page.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Login_Page.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(Login_Page.this, "Login successful!", Toast.LENGTH_SHORT).show();
            goToDashboard();
        });

        // ------------ GOOGLE LOGIN BUTTON ------------
        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // ------------ FIREBASE AUTH WITH GOOGLE TOKEN ------------
    private void firebaseGoogleAuth(String idToken) {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Google Login Successful!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ------------ MOVE TO DASHBOARD ------------
    private void goToDashboard() {
        Intent intent = new Intent(Login_Page.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
