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
    ArrayList<String> recipeFiles;
    ArrayList<String> displayNames;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_ai_recipes);

        listView = findViewById(R.id.lv_saved_recipes);
        recipeFiles = new ArrayList<>();
        displayNames = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFile = recipeFiles.get(position);
            Intent intent = new Intent(this, ViewSavedRecipeActivity.class);
            intent.putExtra("FILE_NAME", selectedFile + ".txt"); // Full extension ke saath bhejein
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedFiles(); // Screen par wapas aate hi list refresh hogi
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
                    recipeFiles.add(rawName);
                    displayNames.add(rawName.replace("_", " "));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(int position) {
        String rawName = recipeFiles.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Kya aap '" + displayNames.get(position) + "' ko delete karna chahte hain?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    File file = new File(getFilesDir(), rawName + ".txt");
                    if (file.delete()) {
                        loadSavedFiles();
                        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}