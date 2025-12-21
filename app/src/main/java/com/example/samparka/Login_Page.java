package com.example.samparka;

import android.view.View;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

    // Demo OTP
    private EditText phoneEditText, otpEditText, nameEditText;
    private Button sendOtpButton, verifyOtpButton;

    // Google Sign-In
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    // To store entered phone
    private String enteredPhone = "";

    // Google Sign-in launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseGoogleAuth(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this,
                                "Google Sign-In Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        phoneEditText = findViewById(R.id.phoneEditText);
        otpEditText = findViewById(R.id.otpEditText);
        nameEditText = findViewById(R.id.nameEditText);
        sendOtpButton = findViewById(R.id.btnSendOtp);
        verifyOtpButton = findViewById(R.id.btnVerifyOtp);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent())
        );

        // SEND OTP (DEMO)
        sendOtpButton.setOnClickListener(v -> {
            enteredPhone = phoneEditText.getText().toString().trim();

            if (enteredPhone.isEmpty() || enteredPhone.length() < 10) {
                Toast.makeText(this,
                        "Enter valid phone number",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this,
                    "OTP Sent (Demo Mode)",
                    Toast.LENGTH_SHORT).show();

            otpEditText.setVisibility(View.VISIBLE);
            verifyOtpButton.setVisibility(View.VISIBLE);
        });

        // VERIFY OTP (ANONYMOUS FIREBASE AUTH)
        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        String uid = firebaseAuth.getUid();

                        String name = "User";
                        if (nameEditText != null &&
                                !nameEditText.getText().toString().trim().isEmpty()) {
                            name = nameEditText.getText().toString().trim();
                        }

                        savePhoneUser(uid, enteredPhone, name);
                        goToDashboard(uid, name);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    // ---------------- GOOGLE LOGIN ----------------
    private void firebaseGoogleAuth(String idToken) {
        firebaseAuth.signInWithCredential(
                        GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
                    String name = acc != null ? acc.getDisplayName() : "User";

                    saveGoogleUser();
                    goToDashboard(firebaseAuth.getUid(), name);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Firebase Auth Failed",
                                Toast.LENGTH_SHORT).show());
    }

    // ---------------- SAVE DEMO OTP USER ----------------
    private void savePhoneUser(String uid, String phone, String name) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", "+91" + phone);
        userData.put("loginType", "DEMO_OTP");

        db.collection("users")
                .document(uid)
                .set(userData);
    }

    // ---------------- SAVE GOOGLE USER ----------------
    private void saveGoogleUser() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (acc == null) return;

        String uid = firebaseAuth.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", acc.getDisplayName());
        userData.put("email", acc.getEmail());
        userData.put("loginType", "GOOGLE");

        db.collection("users")
                .document(uid)
                .set(userData);
    }

    // ---------------- DASHBOARD ----------------
    private void goToDashboard(String uid, String name) {
        Intent intent = new Intent(Login_Page.this, DashboardActivity.class);
        intent.putExtra("USER_DOC_ID", uid);
        intent.putExtra("USER_NAME", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
