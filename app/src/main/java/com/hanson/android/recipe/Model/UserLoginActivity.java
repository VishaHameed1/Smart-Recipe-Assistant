package com.hanson.android.recipe.Model;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hanson.android.recipe.Helper.UserDBHelper;
import com.hanson.android.recipe.MainActivity;
import com.hanson.android.recipe.R;
import com.hanson.android.recipe.Model.UserRegisterActivity;

public class UserLoginActivity extends AppCompatActivity {
    private UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        dbHelper = new UserDBHelper(this);

        EditText etEmail = findViewById(R.id.et_login_email);
        EditText etPass = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login_user);
        TextView tvGoToRegister = findViewById(R.id.tv_go_to_register);

        // Register Page Link
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(UserLoginActivity.this, UserRegisterActivity.class));
        });

        // Login Action
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkLogin(email, pass)) {
                saveUserSession(email);
                Toast.makeText(this, "Welcome Chef!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(String email) {
        SharedPreferences pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userEmail", email);
        editor.apply();
    }
}