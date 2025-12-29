package com.hanson.android.recipe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // --- FIXED LOGIN DETAILS ---
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "visha123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Admin Login");
        }

        final EditText etRegAcc = findViewById(R.id.etRegAcc);
        final EditText etRegPass = findViewById(R.id.etRegPass);
        final Button buttonRegLogin = findViewById(R.id.bRegLogin);
        final TextView registerLink = findViewById(R.id.tvRegLink);

        // Hide registration link (Hardcoded admin access only)
        if (registerLink != null) registerLink.setVisibility(View.GONE);

        buttonRegLogin.setOnClickListener(v -> {
            String inputUser = etRegAcc.getText().toString().trim();
            String inputPass = etRegPass.getText().toString();

            if (inputUser.equals(ADMIN_USER) && inputPass.equals(ADMIN_PASS)) {

                Toast.makeText(this, "Welcome Back, Visha!", Toast.LENGTH_SHORT).show();

                // IMPORTANT: 'UserLogin' use karein taake SearchResult ko login status mil sake
                SharedPreferences pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userID", ADMIN_USER);
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                // Delay to show toast then close login screen
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000);

            } else {
                Toast.makeText(this, "Access Denied! Incorrect Username or Password.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}