package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 1. Views ko initialize karein
        ImageView chefHat = findViewById(R.id.img_chef_hat);
        ImageView logo = findViewById(R.id.img_logo);
        ImageView utensilLeft = findViewById(R.id.img_utensil_left);
        ImageView utensilRight = findViewById(R.id.img_utensil_right);

        // 2. Animations load karein (Inbuilt zoom aur fade use kar rahe hain)
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);

        // Views par animation apply karein
        chefHat.startAnimation(fadeIn);
        logo.startAnimation(fadeIn);
        utensilLeft.startAnimation(fadeIn);
        utensilRight.startAnimation(fadeIn);

        // 3. 2 seconds ke baad MainActivity par switch karein
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Finish zaroori hai taaki back button dabane par splash screen dobara na dikhe
            finish();
        }, 2000);
    }
}