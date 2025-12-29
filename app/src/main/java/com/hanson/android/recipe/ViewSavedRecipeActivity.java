package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class ViewSavedRecipeActivity extends AppCompatActivity implements RecognitionListener {

    private TextView tvContent;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private String[] recipeSteps;
    private int currentStepIndex = 0;
    private boolean isVoiceMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reuse the AI display layout to include FAB and Navigation buttons
        setContentView(R.layout.activity_ai_display);

        tvContent = findViewById(R.id.txt_ai_content);
        FloatingActionButton fabVoice = findViewById(R.id.fab_ai_voice);
        Button btnNext = findViewById(R.id.btn_next_step);
        Button btnPrev = findViewById(R.id.btn_prev_step);

        String fileName = getIntent().getStringExtra("FILE_NAME");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileName != null ? fileName.replace("_", " ") : "Saved Recipe");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load Content from Local Storage
        if (fileName != null) {
            String content = loadFileContent(fileName + ".txt");
            recipeSteps = content.split("\n");
            tvContent.setText(Html.fromHtml(content.replace("**", "").replace("\n", "<br>")));
        }

        // Initialize Text To Speech
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
                setupTTSListener();
            }
        });

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // UI Listeners
        if (fabVoice != null) fabVoice.setOnClickListener(v -> toggleVoiceMode());
        if (btnNext != null) btnNext.setOnClickListener(v -> moveNext());
        if (btnPrev != null) btnPrev.setOnClickListener(v -> moveBack());
    }

    private String loadFileContent(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error loading recipe.";
        }
        return sb.toString();
    }

    private void toggleVoiceMode() {
        if (!isVoiceMode) {
            isVoiceMode = true;
            currentStepIndex = 0;
            speakCurrentStep();
            Toast.makeText(this, "Voice Mode On", Toast.LENGTH_SHORT).show();
        } else {
            stopVoiceMode();
        }
    }

    private void stopVoiceMode() {
        isVoiceMode = false;
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.stopListening();
        Toast.makeText(this, "Voice Mode Off", Toast.LENGTH_SHORT).show();
    }

    private void speakCurrentStep() {
        if (recipeSteps != null && currentStepIndex < recipeSteps.length) {
            String step = recipeSteps[currentStepIndex].trim();
            if (step.length() < 3) { // Skip headers/empty lines
                currentStepIndex++;
                speakCurrentStep();
                return;
            }
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "StepID");
            tts.speak(step, TextToSpeech.QUEUE_FLUSH, params, "StepID");
        }
    }

    private void moveNext() {
        if (recipeSteps != null && currentStepIndex < recipeSteps.length - 1) {
            currentStepIndex++;
            speakCurrentStep();
        }
    }

    private void moveBack() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            speakCurrentStep();
        }
    }

    private void setupTTSListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) {
                if (isVoiceMode) {
                    runOnUiThread(() -> speechRecognizer.startListening(speechIntent));
                }
            }
            @Override public void onError(String utteranceId) {}
        });
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && isVoiceMode) {
            String command = matches.get(0).toLowerCase();
            if (command.contains("next") || command.contains("agla")) moveNext();
            else if (command.contains("back") || command.contains("piche")) moveBack();
            else if (command.contains("stop") || command.contains("band")) stopVoiceMode();
            else speechRecognizer.startListening(speechIntent);
        }
    }

    @Override
    public void onError(int error) {
        if (isVoiceMode) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isVoiceMode) speechRecognizer.startListening(speechIntent);
            }, 1000);
        }
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
    @Override public void onReadyForSpeech(Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle partialResults) {}
    @Override public void onEvent(int eventType, Bundle params) {}

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }
}