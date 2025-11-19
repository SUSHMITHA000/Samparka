package com.example.samparka;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class authorityportal extends AppCompatActivity {

    TextView tabRegister, tabLogin;
    LinearLayout layoutRegister, layoutLogin;

    // Register Fields
    EditText etFullName, etEmail, etPassword;

    // Login Fields
    EditText etLoginAuthorityId, etLoginPassword;
    ImageView iconPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authority_portal);

        // Tabs
        tabRegister = findViewById(R.id.tabRegister);
        tabLogin = findViewById(R.id.tabLogin);

        // Layouts
        layoutRegister = findViewById(R.id.layoutRegister);
        layoutLogin = findViewById(R.id.layoutLogin);

        // Register inputs
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Login inputs
        etLoginAuthorityId = findViewById(R.id.etLoginAuthorityId);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        iconPasswordLogin = findViewById(R.id.iconPasswordLogin);

        // Default → Register
        showRegister();

        // Tab click listeners
        tabRegister.setOnClickListener(v -> showRegister());
        tabLogin.setOnClickListener(v -> showLogin());

        // PASSWORD TOGGLE
        iconPasswordLogin.setOnClickListener(v -> {

            if (etLoginPassword.getInputType() ==
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                // Show password
                etLoginPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            } else {

                // Hide password
                etLoginPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });
    }

    private void showRegister() {
        layoutRegister.setVisibility(View.VISIBLE);
        layoutLogin.setVisibility(View.GONE);

        tabRegister.setBackgroundResource(R.drawable.tab_selected_bg);
        tabLogin.setBackgroundResource(R.drawable.tab_unselected_bg);
    }

    private void showLogin() {
        layoutRegister.setVisibility(View.GONE);
        layoutLogin.setVisibility(View.VISIBLE);

        tabLogin.setBackgroundResource(R.drawable.tab_selected_bg);
        tabRegister.setBackgroundResource(R.drawable.tab_unselected_bg);
    }
}
