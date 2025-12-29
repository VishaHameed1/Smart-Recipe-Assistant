package com.hanson.android.recipe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hanson.android.recipe.Helper.UserDBHelper;

public class UserRegisterActivity extends AppCompatActivity {
    UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        dbHelper = new UserDBHelper(this);
        EditText etName = findViewById(R.id.et_reg_name);
        EditText etEmail = findViewById(R.id.et_reg_email);
        EditText etPass = findViewById(R.id.et_reg_password);
        Button btnRegister = findViewById(R.id.btn_register_user);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = dbHelper.insertUser(name, email, pass);
                if (isInserted) {
                    Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Register ke baad login screen pe wapis jayega
                } else {
                    Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}