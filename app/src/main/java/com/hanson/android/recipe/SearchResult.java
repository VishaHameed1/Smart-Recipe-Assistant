package com.hanson.android.recipe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SearchResult extends AppCompatActivity implements RecognitionListener {

    private ArrayList<Integer> savedReceiveMatches;
    private ArrayList<Integer> savedRecipeList;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private String[] aiSteps;
    private int currentStepIndex = -1;
    private boolean isAiVoiceMode = false;
    private String currentAiResponse = "";

    private TextView txtAiContent;
    private Button btnNextStep, btnPrevStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean isAi = intent.getBooleanExtra("is_ai", false);
        String aiResponse = intent.getStringExtra("ai_response");

        // Logic check: AI vs Normal UI
        if (isAi && aiResponse != null) {
            this.currentAiResponse = aiResponse;
            setContentView(R.layout.activity_ai_display);
            setupAiUI(aiResponse);
            checkVoicePermission();
        } else {
            setContentView(R.layout.activity_search_result);
            setupNormalUI(intent);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(isAi ? "Gemini AI Recipe" : "Search Results");
        }

        initSpeechEngines();
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
    }

    private void setupAiUI(String response) {
        txtAiContent = findViewById(R.id.txt_ai_content);
        FloatingActionButton fabVoice = findViewById(R.id.fab_ai_voice);
        Button btnSave = findViewById(R.id.btn_save_ai);
        btnNextStep = findViewById(R.id.btn_next_step);
        btnPrevStep = findViewById(R.id.btn_prev_step);

        // Splitting by paragraphs for logical steps
        aiSteps = response.split("\\n+");

        if (txtAiContent != null) {
            txtAiContent.setText(Html.fromHtml(response.replace("**", "").replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT));
        }

        fabVoice.setOnClickListener(v -> toggleVoiceMode());

        btnNextStep.setOnClickListener(v -> moveNext());
        btnPrevStep.setOnClickListener(v -> moveBack());

        btnSave.setOnClickListener(v -> {
            String title = (aiSteps.length > 0) ? aiSteps[0].replaceAll("[^a-zA-Z0-9]", "_") : "AI_Recipe";
            saveRecipeToFile(title, currentAiResponse);
        });
    }

    private void toggleVoiceMode() {
        if (!isAiVoiceMode) {
            isAiVoiceMode = true;
            currentStepIndex = 0;
            speakStep();
        } else {
            stopVoiceMode();
        }
    }

    private void speakStep() {
        if (aiSteps != null && currentStepIndex >= 0 && currentStepIndex < aiSteps.length) {
            String step = aiSteps[currentStepIndex].trim();
            if (step.length() < 3) { // Skip short lines
                moveNext();
                return;
            }

            highlightStep(step);

            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "StepID");
            tts.speak(step.replace("*", ""), TextToSpeech.QUEUE_FLUSH, params, "StepID");
        }
    }

    private void highlightStep(String stepText) {
        String fullText = txtAiContent.getText().toString();
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf(stepText);
        if (start != -1) {
            spannable.setSpan(new BackgroundColorSpan(0xFFFFF176), start, start + stepText.length(), 0);
            txtAiContent.setText(spannable);
        }
    }

    private void moveNext() {
        if (currentStepIndex < aiSteps.length - 1) {
            currentStepIndex++;
            speakStep();
        } else {
            stopVoiceMode();
            Toast.makeText(this, "Recipe Finished!", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveBack() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            speakStep();
        }
    }

    private void stopVoiceMode() {
        isAiVoiceMode = false;
        tts.stop();
        speechRecognizer.stopListening();
        txtAiContent.setText(Html.fromHtml(currentAiResponse.replace("**", "").replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT));
        Toast.makeText(this, "Voice Control Off", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && isAiVoiceMode) {
            String cmd = matches.get(0).toLowerCase();
            if (cmd.contains("next") || cmd.contains("agla") || cmd.contains("aage")) moveNext();
            else if (cmd.contains("back") || cmd.contains("piche") || cmd.contains("previous"))
                moveBack();
            else if (cmd.contains("stop") || cmd.contains("band")) stopVoiceMode();
            else if (cmd.contains("repeat") || cmd.contains("dobara")) speakStep();
            else speechRecognizer.startListening(speechIntent); // Listen again if no match
        }
    }

    private void setupTTSListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if (isAiVoiceMode) {
                    new Handler(Looper.getMainLooper()).post(() -> speechRecognizer.startListening(speechIntent));
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
    }

    @Override
    public void onError(int error) {
        if (isAiVoiceMode) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(isAiVoiceMode) speechRecognizer.startListening(speechIntent);
            }, 1000); // Restart listening on error
        }
    }

    private void saveRecipeToFile(String fileName, String content) {
        try (FileOutputStream fos = openFileOutput(fileName + ".txt", Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Toast.makeText(this, "Saved: " + fileName.replace("_", " "), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error Saving File", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNormalUI(Intent intent) {
        final ArrayList<Integer> receiveRecipeList = intent.getIntegerArrayListExtra("idrecipes");
        final ArrayList<Integer> receiveMatches = intent.getIntegerArrayListExtra("matches");
        final ArrayList<Integer> receiveMatchesNoDuplicates = intent.getIntegerArrayListExtra("matchesNoDuplicates");

        this.savedReceiveMatches = receiveMatches;
        this.savedRecipeList = receiveRecipeList;

        if (receiveMatchesNoDuplicates != null) {
            GridView gridView = findViewById(R.id.idGridSearchResult);
            ArrayList<String> displayStrings = createString(receiveMatchesNoDuplicates);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.searchresult_custom, displayStrings);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener((parent, view, position, id) -> {
                Intent nextIntent = new Intent(SearchResult.this, RecipeListActivity.class);
                int matchValue = receiveMatchesNoDuplicates.get(position);
                nextIntent.putExtra("title", "Matching: " + matchValue + " ingredients");

                ArrayList<Integer> positions = findPositionIdResearch(matchValue);
                ArrayList<Integer> idsToSend = new ArrayList<>();
                if (savedRecipeList != null) {
                    for (int posIndex : positions) idsToSend.add(savedRecipeList.get(posIndex));
                }
                nextIntent.putIntegerArrayListExtra("list", idsToSend);
                startActivity(nextIntent);
            });
        }
    }

    // --- Auxiliary Methods ---
    private void checkVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    public ArrayList<String> createString(ArrayList<Integer> array) {
        ArrayList<String> strings = new ArrayList<>();
        for (Integer count : array) strings.add("Matched\n" + count + "\n" + (count == 1 ? "ingredient" : "ingredients"));
        return strings;
    }

    public ArrayList<Integer> findPositionIdResearch(int matchedCount) {
        ArrayList<Integer> positions = new ArrayList<>();
        if (savedReceiveMatches != null) {
            for (int i = 0; i < savedReceiveMatches.size(); i++) {
                if (savedReceiveMatches.get(i) == matchedCount) positions.add(i);
            }
        }
        return positions;
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) { speechRecognizer.destroy(); }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
}