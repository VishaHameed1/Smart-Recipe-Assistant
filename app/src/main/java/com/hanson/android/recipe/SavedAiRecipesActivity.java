package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class SavedAiRecipesActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> recipeFiles; // Asli file names (.txt ke saath)
    ArrayList<String> displayNames; // User ko dikhane ke liye (Names without underscores)
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_ai_recipes);

        listView = findViewById(R.id.lv_saved_recipes);
        recipeFiles = new ArrayList<>();
        displayNames = new ArrayList<>();

        loadSavedFiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        listView.setAdapter(adapter);

        // Click to View
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFileName = recipeFiles.get(position); // Full file name
            Intent intent = new Intent(this, ViewSavedRecipeActivity.class);
            intent.putExtra("FILE_NAME", selectedFileName);
            startActivity(intent);
        });

        // Long Click to Delete
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void loadSavedFiles() {
        recipeFiles.clear();
        displayNames.clear();
        File directory = getFilesDir();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".txt")) {
                    String rawName = file.getName().replace(".txt", "");
                    recipeFiles.add(rawName); // Save raw name for logic

                    // Display name: Underscores hata kar spaces lagayein
                    displayNames.add(rawName.replace("_", " "));
                }
            }
        }
    }

    private void showDeleteDialog(int position) {
        String rawName = recipeFiles.get(position);
        String displayName = displayNames.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Delete '" + displayName + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    File file = new File(getFilesDir(), rawName + ".txt");
                    if (file.delete()) {
                        recipeFiles.remove(position);
                        displayNames.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}