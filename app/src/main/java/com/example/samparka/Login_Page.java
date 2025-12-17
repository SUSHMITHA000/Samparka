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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Login_Page extends AppCompatActivity {

    // Phone OTP
    private EditText phoneEditText, otpEditText;
    private Button sendOtpButton, verifyOtpButton;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    // Google Sign-In
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

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

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Phone OTP views
        phoneEditText = findViewById(R.id.phoneEditText);
        otpEditText = findViewById(R.id.otpEditText);
        sendOtpButton = findViewById(R.id.btnSendOtp);
        verifyOtpButton = findViewById(R.id.btnVerifyOtp);

        // Google
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Send OTP
        sendOtpButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString().trim();

            if (phone.isEmpty() || phone.length() < 10) {
                Toast.makeText(this,
                        "Enter valid phone number",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            sendOtp("+91" + phone);
        });

        // Verify OTP
        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(this,
                        "Enter OTP",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationId, otp);

            signInWithPhoneAuthCredential(credential);
        });
    }

    // ---------------- PHONE OTP ----------------
    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Toast.makeText(Login_Page.this,
                                        "OTP Failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(String vid,
                                                   PhoneAuthProvider.ForceResendingToken token) {
                                verificationId = vid;
                                resendToken = token;

                                Toast.makeText(Login_Page.this,
                                        "OTP Sent",
                                        Toast.LENGTH_SHORT).show();

                                // ✅ SHOW OTP INPUT & VERIFY BUTTON
                                otpEditText.setVisibility(View.VISIBLE);
                                verifyOtpButton.setVisibility(View.VISIBLE);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    savePhoneUser();
                    goToDashboard();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "OTP Verification Failed",
                                Toast.LENGTH_SHORT).show());
    }

    // ---------------- GOOGLE LOGIN ----------------
    private void firebaseGoogleAuth(String idToken) {
        firebaseAuth.signInWithCredential(
                        GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    saveGoogleUser();
                    goToDashboard();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Firebase Auth Failed",
                                Toast.LENGTH_SHORT).show());
    }

    // ---------------- SAVE USERS ----------------
    private void savePhoneUser() {
        String uid = firebaseAuth.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("phone", firebaseAuth.getCurrentUser().getPhoneNumber());

        db.collection("users").document(uid).set(userData);
    }

    private void saveGoogleUser() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (acc == null) return;

        String uid = firebaseAuth.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", acc.getDisplayName());
        userData.put("email", acc.getEmail());
        userData.put("photoUrl",
                acc.getPhotoUrl() != null ? acc.getPhotoUrl().toString() : "");

        db.collection("users").document(uid).set(userData);
    }

    // ---------------- DASHBOARD ----------------
    private void goToDashboard() {
        Intent intent = new Intent(Login_Page.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
