package com.example.samparka;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    LinearLayout layoutSignUp, layoutLogin;
    Button btnLoginTab, btnSignUpTab, btnCreateAccount, btnSignIn;
    EditText etFullName, etPhone, etEmailPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        layoutSignUp = findViewById(R.id.layoutSignUp);
        layoutLogin = findViewById(R.id.layoutLogin);
        btnLoginTab = findViewById(R.id.btnLoginTab);
        btnSignUpTab = findViewById(R.id.btnSignUpTab);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnSignIn = findViewById(R.id.btnSignIn);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etEmailPhone = findViewById(R.id.etEmailPhone);
        etPassword = findViewById(R.id.etPassword);

        // Default: Show Sign Up
        layoutSignUp.setVisibility(LinearLayout.VISIBLE);
        layoutLogin.setVisibility(LinearLayout.GONE);

        btnSignUpTab.setTextColor(getColor(android.R.color.black));
        btnLoginTab.setTextColor(getColor(android.R.color.darker_gray));

        // Toggle to Login
        btnLoginTab.setOnClickListener(v -> {
            layoutSignUp.setVisibility(LinearLayout.GONE);
            layoutLogin.setVisibility(LinearLayout.VISIBLE);
            btnLoginTab.setTextColor(getColor(android.R.color.black));
            btnSignUpTab.setTextColor(getColor(android.R.color.darker_gray));
        });

        // Toggle to Sign Up
        btnSignUpTab.setOnClickListener(v -> {
            layoutSignUp.setVisibility(LinearLayout.VISIBLE);
            layoutLogin.setVisibility(LinearLayout.GONE);
            btnSignUpTab.setTextColor(getColor(android.R.color.black));
            btnLoginTab.setTextColor(getColor(android.R.color.darker_gray));
        });

        // Create Account
        btnCreateAccount.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account created for " + name, Toast.LENGTH_SHORT).show();
            }
        });

        // Sign In
        btnSignIn.setOnClickListener(v -> {
            String user = etEmailPhone.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Signed in as " + user, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
