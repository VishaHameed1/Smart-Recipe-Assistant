package com.hanson.android.recipe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button; // Added
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout; // Added
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeActivity extends AppCompatActivity implements RecognitionListener {

    private final ImageHelper imageHelper = new ImageHelper();
    private RecipeItem recipeItem;
    private ScrollView scrollView;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private String[] recipeSteps;
    private int currentStepIndex = 0;
    private boolean isCookingMode = false;

    // Manual Buttons
    private LinearLayout voiceControlPanel;
    private Button btnNextStep, btnPrevStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        final DBHelper dbHelper = new DBHelper(this, "Recipes.db", null, 1);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences pref = getSharedPreferences("Login", Activity.MODE_PRIVATE);
        final String userID = pref.getString("userID", "");

        // Bind Views
        scrollView = findViewById(R.id.recipe_scroll_view);
        TextView recipeName = findViewById(R.id.txt_recipeName);
        TextView author = findViewById(R.id.txt_recipeAuthor);
        TextView uploadDate = findViewById(R.id.txt_recipeUploaddate);
        TextView country = findViewById(R.id.txt_recipeCountry);
        TextView ingredients = findViewById(R.id.txt_recipeIngredients);
        TextView description = findViewById(R.id.txt_recipeDescription);
        TextView howto = findViewById(R.id.txt_recipeHowto);
        ImageView mainImg = findViewById(R.id.img_recipeMainImg);
        CheckBox like = findViewById(R.id.chk_recipeLike);
        FloatingActionButton btnVoice = findViewById(R.id.btn_start_cooking);

        // Bind Manual Buttons from XML
        voiceControlPanel = findViewById(R.id.voice_control_panel);
        btnNextStep = findViewById(R.id.btn_next_step);
        btnPrevStep = findViewById(R.id.btn_prev_step);

        // Get intent data
        Intent intent = getIntent();
        String name = intent.getStringExtra("recipe");
        recipeItem = dbHelper.recipes_SelectByName(name);

        if (recipeItem != null) {
            mainImg.setImageBitmap(imageHelper.getBitmapFromByteArray(recipeItem.get_mainImg()));
            recipeName.setText(recipeItem.get_recipeName());
            author.setText(recipeItem.get_author());
            uploadDate.setText(recipeItem.get_uploadDate());
            country.setText(recipeItem.get_category());
            description.setText(recipeItem.get_Description());
            howto.setText(recipeItem.get_howTo());

            ArrayList<String> ingredientList = dbHelper.ingredients_SelectByRecipeId(recipeItem.get_id());
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < ingredientList.size(); i++) {
                builder.append(ingredientList.get(i));
                if (i != (ingredientList.size() - 1)) builder.append(" / ");
            }
            ingredients.setText(builder.toString());

            if (!userID.isEmpty()) {
                like.setChecked(dbHelper.like_GetLikeYNByUserId(userID, recipeItem.get_id()));
            }
        }

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
                setupTTSProgressListener();
            }
        });

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        btnVoice.setOnClickListener(v -> {
            if (!isCookingMode) {
                startCookingMode();
            } else {
                stopCookingMode();
            }
        });

        // Manual Button Listeners
        btnNextStep.setOnClickListener(v -> {
            if (currentStepIndex < recipeSteps.length - 1) {
                currentStepIndex++;
                readStepAndListen();
            } else {
                Toast.makeText(this, "Last Step!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevStep.setOnClickListener(v -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                readStepAndListen();
            }
        });

        like.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (userID.isEmpty()) {
                startActivity(new Intent(this, LoginActivity.class));
                buttonView.setChecked(false);
            } else {
                if (isChecked) dbHelper.recipes_AddLike(userID, recipeItem.get_id());
                else dbHelper.recipes_MinusLike(userID, recipeItem.get_id());
            }
        });
    }

    private void setupTTSProgressListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                // Mic starts only after TTS finishes speaking
                if (isCookingMode) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        speechRecognizer.startListening(speechIntent);
                    });
                }
            }

            @Override
            public void onError(String utteranceId) {}
        });
    }

    private void startCookingMode() {
        if (recipeItem != null && recipeItem.get_howTo() != null) {
            recipeSteps = recipeItem.get_howTo().split("\\.");
            currentStepIndex = 0;
            isCookingMode = true;

            // Show bottom manual controls
            if (voiceControlPanel != null) voiceControlPanel.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Voice Mode Started! Say 'Next' or use buttons.", Toast.LENGTH_SHORT).show();
            readStepAndListen();
        }
    }

    private void readStepAndListen() {
        if (isCookingMode && recipeSteps != null && currentStepIndex < recipeSteps.length) {
            String stepText = recipeSteps[currentStepIndex].trim();
            if (stepText.length() < 2) {
                currentStepIndex++;
                readStepAndListen();
                return;
            }

            // UI Feedback: Toast for current step number
            Toast.makeText(this, "Step " + (currentStepIndex + 1), Toast.LENGTH_SHORT).show();

            // Auto-Scroll to bottom to show instructions
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "RecipeStepID");
            tts.speak("Step " + (currentStepIndex + 1) + ": " + stepText, TextToSpeech.QUEUE_FLUSH, params, "RecipeStepID");
        } else if (isCookingMode) {
            tts.speak("Recipe completed. Enjoy your meal!", TextToSpeech.QUEUE_FLUSH, null, "FinishID");
            stopCookingMode();
        }
    }

    private void stopCookingMode() {
        isCookingMode = false;
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.stopListening();
        if (voiceControlPanel != null) voiceControlPanel.setVisibility(View.GONE);
        Toast.makeText(this, "Voice Mode Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && isCookingMode) {
            String command = matches.get(0).toLowerCase();
            if (command.contains("next") || command.contains("agla")) {
                currentStepIndex++;
                readStepAndListen();
            } else if (command.contains("back") || command.contains("piche") || command.contains("previous")) {
                if (currentStepIndex > 0) {
                    currentStepIndex--;
                    readStepAndListen();
                }
            } else if (command.contains("repeat") || command.contains("dobara")) {
                readStepAndListen();
            } else if (command.contains("stop") || command.contains("exit")) {
                stopCookingMode();
            } else {
                // Not a valid command? Keep listening.
                speechRecognizer.startListening(speechIntent);
            }
        }
    }

    @Override
    public void onError(int error) {
        if (isCookingMode) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(isCookingMode) speechRecognizer.startListening(speechIntent);
            }, 1500);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }

    // Required Callbacks
    @Override public void onReadyForSpeech(Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle partialResults) {}
    @Override public void onEvent(int eventType, Bundle params) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}