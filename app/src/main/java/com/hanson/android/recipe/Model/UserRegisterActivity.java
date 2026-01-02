package com.hanson.android.recipe.Model;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hanson.android.recipe.Helper.UserDBHelper;
import com.hanson.android.recipe.R;

public class UserRegisterActivity extends AppCompatActivity {
    private UserDBHelper dbHelper;

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
                return;
            }

            if (dbHelper.insertUser(name, email, pass)) {
                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                finish(); // Goes back to Login screen
            } else {
                Toast.makeText(this, "Email is already taken", Toast.LENGTH_SHORT).show();
            }
        });
    }
}