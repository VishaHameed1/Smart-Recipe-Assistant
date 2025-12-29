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
import android.view.MenuItem;
import android.view.View;
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
    private int currentStepIndex = 0;
    private boolean isAiVoiceMode = false;
    private String currentAiResponse = "";

    private Button btnNextStep, btnPrevStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        boolean isAi = intent.getBooleanExtra("is_ai", false);
        String aiResponse = intent.getStringExtra("ai_response");

        if (isAi && aiResponse != null) {
            this.currentAiResponse = aiResponse;
            setupAiUI(aiResponse);
            checkVoicePermission();
        } else {
            setupNormalUI(intent);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(isAi ? "Gemini AI Recipe" : "Search Results");
        }

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

    private void checkVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void setupTTSListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                if (isAiVoiceMode) {
                    runOnUiThread(() -> speechRecognizer.startListening(speechIntent));
                }
            }

            @Override
            public void onError(String utteranceId) {}
        });
    }

    private void setupAiUI(String response) {
        setContentView(R.layout.activity_ai_display);
        TextView tv = findViewById(R.id.txt_ai_content);
        FloatingActionButton fabVoice = findViewById(R.id.fab_ai_voice);
        Button btnSave = findViewById(R.id.btn_save_ai);

        btnNextStep = findViewById(R.id.btn_next_step);
        btnPrevStep = findViewById(R.id.btn_prev_step);

        // Splitting by lines for better step-by-step navigation
        aiSteps = response.split("\n");

        if (tv != null) {
            // Remove markdown symbols for better display
            String cleanText = response.replace("**", "")
                    .replace("* ", "• ")
                    .replace("\n", "<br>");
            tv.setText(Html.fromHtml(cleanText));
        }

        if (fabVoice != null) {
            fabVoice.setOnClickListener(v -> {
                if (!isAiVoiceMode) {
                    isAiVoiceMode = true;
                    currentStepIndex = 0;
                    readNextAiStep();
                } else {
                    stopVoiceMode();
                }
            });
        }

        // Manual Navigation
        if (btnNextStep != null) {
            btnNextStep.setOnClickListener(v -> {
                if (currentStepIndex < aiSteps.length - 1) {
                    currentStepIndex++;
                    readNextAiStep();
                }
            });
        }

        if (btnPrevStep != null) {
            btnPrevStep.setOnClickListener(v -> {
                if (currentStepIndex > 0) {
                    currentStepIndex--;
                    readNextAiStep();
                }
            });
        }

        // Updated Save Logic with Dish Name
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String fileName = "AI_Recipe_" + System.currentTimeMillis();

                if (aiSteps != null && aiSteps.length > 0) {
                    // Extract first line as filename
                    String firstLine = aiSteps[0].replaceAll("[^a-zA-Z0-9\\s]", "").trim();
                    if (!firstLine.isEmpty()) {
                        fileName = firstLine.replaceAll("\\s+", "_"); // Underscore for storage safety
                        if (fileName.length() > 40) fileName = fileName.substring(0, 40);
                    }
                }
                saveRecipeToFile(fileName, currentAiResponse);
            });
        }
    }

    private void readNextAiStep() {
        if (aiSteps != null && currentStepIndex < aiSteps.length) {
            String stepText = aiSteps[currentStepIndex].trim();
            // Skip headers or empty lines
            if (stepText.length() < 3) {
                currentStepIndex++;
                readNextAiStep();
                return;
            }

            Toast.makeText(this, "Reading Step " + (currentStepIndex + 1), Toast.LENGTH_SHORT).show();
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "AiStepID");

            // Clean markdown before speaking
            String speakableText = stepText.replace("**", "").replace("*", "");
            tts.speak(speakableText, TextToSpeech.QUEUE_FLUSH, params, "AiStepID");
        }
    }

    private void stopVoiceMode() {
        isAiVoiceMode = false;
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.stopListening();
        Toast.makeText(this, "Voice Mode Off", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && isAiVoiceMode) {
            String command = matches.get(0).toLowerCase();
            if (command.contains("next") || command.contains("agla") || command.contains("shuru")) {
                currentStepIndex++;
                readNextAiStep();
            } else if (command.contains("back") || command.contains("piche") || command.contains("previous")) {
                if (currentStepIndex > 0) {
                    currentStepIndex--;
                    readNextAiStep();
                }
            } else if (command.contains("stop") || command.contains("band")) {
                stopVoiceMode();
            } else {
                // Not recognized, listen again
                speechRecognizer.startListening(speechIntent);
            }
        }
    }

    @Override
    public void onError(int error) {
        if (isAiVoiceMode) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(isAiVoiceMode) speechRecognizer.startListening(speechIntent);
            }, 1000);
        }
    }

    private void saveRecipeToFile(String fileName, String content) {
        try {
            FileOutputStream fos = openFileOutput(fileName + ".txt", Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Recipe saved as " + fileName.replace("_", " "), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Required overrides
    @Override public void onReadyForSpeech(Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onPartialResults(Bundle partialResults) {}
    @Override public void onEvent(int eventType, Bundle params) {}

    private void setupNormalUI(Intent intent) {
        setContentView(R.layout.activity_search_result);
        final ArrayList<Integer> receiveRecipeList = intent.getIntegerArrayListExtra("idrecipes");
        final ArrayList<Integer> receiveMatches = intent.getIntegerArrayListExtra("matches");
        final ArrayList<Integer> receiveMatchesNoDuplicates = intent.getIntegerArrayListExtra("matchesNoDuplicates");
        this.savedReceiveMatches = receiveMatches;
        this.savedRecipeList = receiveRecipeList;
        if (receiveMatchesNoDuplicates != null) {
            GridView gridView = findViewById(R.id.idGridSearchResult);
            if (gridView != null) {
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
}