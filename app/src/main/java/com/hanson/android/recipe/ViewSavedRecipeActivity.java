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
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
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
    private ScrollView scrollView;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private String[] recipeSteps;
    private int currentStepIndex = -1;
    private boolean isVoiceMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_display);

        // Views Initialization
        tvContent = findViewById(R.id.txt_ai_content);
        scrollView = findViewById(R.id.scroll_view_ai); // Ensure ID exists in XML
        FloatingActionButton fabVoice = findViewById(R.id.fab_ai_voice);
        Button btnNext = findViewById(R.id.btn_next_step);
        Button btnPrev = findViewById(R.id.btn_prev_step);

        String fileName = getIntent().getStringExtra("FILE_NAME");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileName != null ? fileName.replace("_", " ").replace(".txt", "") : "Recipe View");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 1. Load and Clean Content
        if (fileName != null) {
            String rawContent = loadFileContent(fileName);
            // Steps ko newline se split karein aur khaali lines nikaal dein
            recipeSteps = rawContent.split("\\n+");
            displayOriginalText();
        }

        // 2. TTS & Speech Setup
        initSpeechEngines();

        // 3. Listeners
        if (fabVoice != null) fabVoice.setOnClickListener(v -> toggleVoiceMode());
        if (btnNext != null) btnNext.setOnClickListener(v -> moveNext());
        if (btnPrev != null) btnPrev.setOnClickListener(v -> moveBack());
    }

    private void initSpeechEngines() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
                setupTTSListener();
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }

    private void displayOriginalText() {
        StringBuilder fullText = new StringBuilder();
        for (String step : recipeSteps) {
            fullText.append(step).append("\n\n");
        }
        tvContent.setText(fullText.toString());
    }

    private void toggleVoiceMode() {
        if (!isVoiceMode) {
            isVoiceMode = true;
            currentStepIndex = 0;
            speakCurrentStep();
            Toast.makeText(this, "Hands-Free Mode On", Toast.LENGTH_SHORT).show();
        } else {
            stopVoiceMode();
        }
    }

    private void stopVoiceMode() {
        isVoiceMode = false;
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.stopListening();
        displayOriginalText(); // Remove highlighting
        Toast.makeText(this, "Voice Mode Off", Toast.LENGTH_SHORT).show();
    }

    private void speakCurrentStep() {
        if (recipeSteps != null && currentStepIndex >= 0 && currentStepIndex < recipeSteps.length) {
            String step = recipeSteps[currentStepIndex].trim();

            // Clean text for TTS (Remove markdown symbols)
            String cleanStep = step.replaceAll("[*#_]", "");

            if (cleanStep.length() < 3) {
                moveNext();
                return;
            }

            highlightAndScroll(step);

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "StepID");
            tts.speak(cleanStep, TextToSpeech.QUEUE_FLUSH, params, "StepID");
        }
    }

    private void highlightAndScroll(String currentStepText) {
        String fullText = tvContent.getText().toString();
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(currentStepText);
        int end = start + currentStepText.length();

        if (start != -1) {
            // Yellow Highlight for current step
            spannable.setSpan(new BackgroundColorSpan(0xFFFFF176), start, end, 0);
            tvContent.setText(spannable);

            // Auto-scroll logic
            int line = tvContent.getLayout().getLineForOffset(start);
            int y = tvContent.getLayout().getLineTop(line);
            if (scrollView != null) scrollView.smoothScrollTo(0, y);
        }
    }

    private void moveNext() {
        if (recipeSteps != null && currentStepIndex < recipeSteps.length - 1) {
            currentStepIndex++;
            speakCurrentStep();
        } else {
            stopVoiceMode();
            Toast.makeText(this, "Recipe Finished!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveBack() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            speakCurrentStep();
        }
    }

    private String loadFileContent(String fileName) {
        if (!fileName.endsWith(".txt")) fileName += ".txt";
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = openFileInput(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            return "Recipe not found.";
        }
        return sb.toString();
    }

    // --- Speech Recognition Logic ---

    private void setupTTSListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) {
                if (isVoiceMode) {
                    new Handler(Looper.getMainLooper()).post(() -> speechRecognizer.startListening(speechIntent));
                }
            }
            @Override public void onError(String utteranceId) {}
        });
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && isVoiceMode) {
            String cmd = matches.get(0).toLowerCase();
            if (cmd.contains("next") || cmd.contains("agla") || cmd.contains("aage")) moveNext();
            else if (cmd.contains("back") || cmd.contains("piche") || cmd.contains("previous"))
                moveBack();
            else if (cmd.contains("repeat") || cmd.contains("dobara")) speakCurrentStep();
            else if (cmd.contains("stop") || cmd.contains("khatam")) stopVoiceMode();
            else speechRecognizer.startListening(speechIntent); // Listen again if no match
        }
    }

    @Override
    public void onError(int error) {
        // Automatically restart listening on timeout/error
        if (isVoiceMode) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isVoiceMode) speechRecognizer.startListening(speechIntent);
            }, 1000);
        }
    }

    // Activity Lifecycle
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    // Required recognition callbacks (empty)
    @Override public void onReadyForSpeech(Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle partialResults) {}
    @Override public void onEvent(int eventType, Bundle params) {}
}