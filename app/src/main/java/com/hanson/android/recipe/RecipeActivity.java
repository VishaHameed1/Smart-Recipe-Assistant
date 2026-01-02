package com.hanson.android.recipe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeActivity extends AppCompatActivity implements RecognitionListener {

    private final ImageHelper imageHelper = new ImageHelper();
    private RecipeItem recipeItem;
    private ScrollView scrollView;
    private TextView txtHowTo;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private String[] recipeSteps;
    private int currentStepIndex = 0;
    private boolean isCookingMode = false;

    private LinearLayout voiceControlPanel;
    private Button btnNextStep, btnPrevStep;
    private ExtendedFloatingActionButton btnStartCooking;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        DBHelper dbHelper = new DBHelper(this, "Recipes.db", null, 1);

        // Toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        String name = getIntent().getStringExtra("recipe");
        recipeItem = dbHelper.recipes_SelectByName(name);

        if (recipeItem != null) {
            if (actionBar != null) actionBar.setTitle(recipeItem.get_recipeName());
            populateData(dbHelper);
        }

        initVoiceEngines();
        checkPermissions();
    }

    private void initViews() {
        scrollView = findViewById(R.id.recipe_scroll_view);
        txtHowTo = findViewById(R.id.txt_recipeHowto);
        voiceControlPanel = findViewById(R.id.voice_control_panel);
        btnNextStep = findViewById(R.id.btn_next_step);
        btnPrevStep = findViewById(R.id.btn_prev_step);
        btnStartCooking = findViewById(R.id.btn_start_cooking);
        statusText = findViewById(R.id.tv_voice_status); // Add this in XML for visual feedback
    }

    private void populateData(DBHelper dbHelper) {
        SharedPreferences pref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        String userID = pref.getString("userID", "");

        TextView recipeName = findViewById(R.id.txt_recipeName);
        TextView author = findViewById(R.id.txt_recipeAuthor);
        ImageView mainImg = findViewById(R.id.img_recipeMainImg);
        TextView ingredientsText = findViewById(R.id.txt_recipeIngredients);
        CheckBox chkLike = findViewById(R.id.chk_recipeLike);

        recipeName.setText(recipeItem.get_recipeName());
        author.setText("Chef: " + recipeItem.get_author());
        mainImg.setImageBitmap(imageHelper.getBitmapFromByteArray(recipeItem.get_mainImg()));
        txtHowTo.setText(recipeItem.get_howTo());

        // Ingredients formatting
        ArrayList<String> ings = dbHelper.ingredients_SelectByRecipeId(recipeItem.get_id());
        StringBuilder sb = new StringBuilder();
        for (String s : ings) sb.append("• ").append(s).append("\n");
        ingredientsText.setText(sb.toString().trim());

        // Like status
        if (!userID.isEmpty()) {
            chkLike.setChecked(dbHelper.like_GetLikeYNByUserId(userID, recipeItem.get_id()));
        }

        btnStartCooking.setOnClickListener(v -> toggleCookingMode());
        btnNextStep.setOnClickListener(v -> moveStep(1));
        btnPrevStep.setOnClickListener(v -> moveStep(-1));
    }

    private void toggleCookingMode() {
        if (!isCookingMode) {
            recipeSteps = recipeItem.get_howTo().split("\\r?\\n|\\.");
            currentStepIndex = 0;
            isCookingMode = true;
            voiceControlPanel.setVisibility(View.VISIBLE);
            btnStartCooking.setText("Stop Cooking");
            btnStartCooking.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel));
            readStepAndListen();
        } else {
            stopCookingMode();
        }
    }

    private void readStepAndListen() {
        if (!isCookingMode || recipeSteps == null || currentStepIndex >= recipeSteps.length) return;

        String step = recipeSteps[currentStepIndex].trim();
        if (step.isEmpty()) {
            moveStep(1);
            return;
        }

        highlightStep(step);

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "STEP_SPEECH");
        tts.speak("Step " + (currentStepIndex + 1) + ": " + step, TextToSpeech.QUEUE_FLUSH, params, "STEP_SPEECH");
    }

    private void highlightStep(String stepContent) {
        String fullText = txtHowTo.getText().toString();
        int startPos = fullText.indexOf(stepContent);
        if (startPos != -1) {
            SpannableString spannable = new SpannableString(fullText);
            spannable.setSpan(new BackgroundColorSpan(Color.YELLOW), startPos, startPos + stepContent.length(), 0);
            txtHowTo.setText(spannable);

            // Auto-scroll to the step
            int y = txtHowTo.getLayout().getLineTop(txtHowTo.getLayout().getLineForOffset(startPos));
            scrollView.smoothScrollTo(0, txtHowTo.getTop() + y - 100);
        }
    }

    private void moveStep(int direction) {
        currentStepIndex += direction;
        if (currentStepIndex >= 0 && currentStepIndex < recipeSteps.length) {
            readStepAndListen();
        } else if (currentStepIndex >= recipeSteps.length) {
            tts.speak("Cooking complete! Enjoy your meal.", TextToSpeech.QUEUE_FLUSH, null, null);
            stopCookingMode();
        } else {
            currentStepIndex = 0;
        }
    }

    private void initVoiceEngines() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.getDefault());
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    statusText.setText("Listening...");
                    speechRecognizer.startListening(speechIntent);
                });
            }

            @Override
            public void onError(String utteranceId) {
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            String cmd = matches.get(0).toLowerCase();
            if (cmd.contains("next") || cmd.contains("agla")) moveStep(1);
            else if (cmd.contains("back") || cmd.contains("piche")) moveStep(-1);
            else if (cmd.contains("repeat") || cmd.contains("dobara")) readStepAndListen();
            else if (cmd.contains("stop")) stopCookingMode();
            else speechRecognizer.startListening(speechIntent);
        }
    }

    private void stopCookingMode() {
        isCookingMode = false;
        tts.stop();
        speechRecognizer.stopListening();
        voiceControlPanel.setVisibility(View.GONE);
        btnStartCooking.setText("Start Cooking");
        txtHowTo.setText(recipeItem.get_howTo()); // clear highlight
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }

    @Override
    public void onError(int error) {
        if (isCookingMode) speechRecognizer.startListening(speechIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        statusText.setText("Listening for 'Next'...");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }

    // Unused callbacks
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle partialResults) {}
    @Override public void onEvent(int eventType, Bundle params) {}
}