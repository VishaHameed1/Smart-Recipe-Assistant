package com.hanson.android.recipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Yeh add karein
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hanson.android.recipe.Helper.UserDBHelper;

public class UserLoginActivity extends AppCompatActivity {
    UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        dbHelper = new UserDBHelper(this);

        EditText etEmail = findViewById(R.id.et_login_email);
        EditText etPass = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login_user);

        // --- REGISTER PAGE PAR JANAY KA LOGIC ---
        TextView tvGoToRegister = findViewById(R.id.tv_go_to_register);
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(UserLoginActivity.this, com.hanson.android.recipe.UserRegisterActivity.class);
            startActivity(intent);
        });

        // Login Logic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (dbHelper.checkLogin(email, pass)) {
                SharedPreferences pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userEmail", email);
                editor.apply();

                Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}