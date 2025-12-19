package com.example.samparka;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Login_Page extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, phoneNumberEditText, otpEditText;
    private Button loginButton, sendOtpButton, verifyOtpButton, emailLoginButton, phoneLoginButton, googleSignInButton;
    private TextView forgotPasswordTextEmail, loginSubtitle;
    private LinearLayout emailLoginForm, phoneLoginForm;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private String verificationId;

    // Google Sign-in result launcher
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        otpEditText = findViewById(R.id.otpEditText);

        loginButton = findViewById(R.id.loginButton);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        emailLoginButton = findViewById(R.id.emailLoginButton);
        phoneLoginButton = findViewById(R.id.phoneLoginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        forgotPasswordTextEmail = findViewById(R.id.forgotPasswordTextEmail);
        loginSubtitle = findViewById(R.id.loginSubtitle);
        emailLoginForm = findViewById(R.id.emailLoginForm);
        phoneLoginForm = findViewById(R.id.phoneLoginForm);

        // GOOGLE SIGN-IN SETUP
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        loginButton.setOnClickListener(v -> manualLogin());
        sendOtpButton.setOnClickListener(v -> sendOtp());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        emailLoginButton.setOnClickListener(v -> showEmailLogin());
        phoneLoginButton.setOnClickListener(v -> showPhoneLogin());

        // ---------------------- FORGOT PASSWORD ----------------------
        forgotPasswordTextEmail.setOnClickListener(v -> sendResetLink());
    }

    private void showEmailLogin() {
        emailLoginForm.setVisibility(View.VISIBLE);
        phoneLoginForm.setVisibility(View.GONE);
        loginSubtitle.setText(R.string.login_subtitle);

        emailLoginButton.setBackgroundResource(R.drawable.selected_bg);
        emailLoginButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        phoneLoginButton.setBackgroundResource(R.drawable.input_bg);
        phoneLoginButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void showPhoneLogin() {
        emailLoginForm.setVisibility(View.GONE);
        phoneLoginForm.setVisibility(View.VISIBLE);
        loginSubtitle.setText("Login using phone number");

        phoneLoginButton.setBackgroundResource(R.drawable.selected_bg);
        phoneLoginButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        emailLoginButton.setBackgroundResource(R.drawable.input_bg);
        emailLoginButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void sendOtp() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                        Toast.makeText(Login_Page.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Login_Page.this.verificationId = verificationId;
                        Toast.makeText(Login_Page.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyOtp() {
        String otp = otpEditText.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // User is signed in.
                    // Check if the user is new or existing.
                    String uid = firebaseAuth.getUid();
                    db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().exists()) {
                            // New user, save data
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("phone", firebaseAuth.getCurrentUser().getPhoneNumber());
                            db.collection("users").document(uid).set(userData);
                        }
                        goToDashboard();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // SEND RESET PASSWORD EMAIL
    private void sendResetLink() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void manualLogin() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    goToDashboard();
                })
                .addOnFailureListener(e -> {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(result -> {

                                String uid = firebaseAuth.getUid();

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("phone", "");
                                userData.put("city", "");
                                userData.put("state", "");
                                userData.put("pin", "");
                                userData.put("location", "");
                                userData.put("photoUrl", "");

                                db.collection("users")
                                        .document(uid)
                                        .set(userData);

                                Toast.makeText(this, "Account Created & Logged In!", Toast.LENGTH_SHORT).show();
                                goToDashboard();
                            })
                            .addOnFailureListener(err ->
                                    Toast.makeText(this, "Login Failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    // GOOGLE SIGN-IN → FIREBASE
    private void firebaseGoogleAuth(String idToken) {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(authResult -> {
                    GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);

                    if (acc != null) {
                        String uid = firebaseAuth.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", acc.getDisplayName());
                        userData.put("email", acc.getEmail());
                        userData.put("phone", "");
                        userData.put("city", "");
                        userData.put("state", "");
                        userData.put("pin", "");
                        userData.put("location", "");
                        userData.put("photoUrl", "");

                        db.collection("users")
                                .document(uid)
                                .set(userData);
                    }

                    goToDashboard();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firebase Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void goToDashboard() {
        Intent intent = new Intent(Login_Page.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
