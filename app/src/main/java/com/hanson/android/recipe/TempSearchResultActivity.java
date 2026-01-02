package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class TempSearchResultActivity extends AppCompatActivity {

    private ArrayList<Integer> sendRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_search_result);

        // 1. Action Bar Styling
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Search Recipes");
        }

        // 2. Dummy Data (Replace with your actual search logic/DB query)
        generateDummyData();

        // 3. Button Implementation
        MaterialButton btnSend = findViewById(R.id.btn_sendRecipeList);
        btnSend.setOnClickListener(v -> {
            if (sendRecipeList.isEmpty()) {
                Toast.makeText(this, "No recipes found matching these ingredients!", Toast.LENGTH_SHORT).show();
            } else {
                navigateToResults();
            }
        });
    }

    private void generateDummyData() {
        sendRecipeList.clear();
        sendRecipeList.add(1);
        sendRecipeList.add(2);
        sendRecipeList.add(3);
        sendRecipeList.add(4);
    }

    private void navigateToResults() {
        Intent intent = new Intent(TempSearchResultActivity.this, RecipeListActivity.class);

        // Pass title and list of IDs to the list displayer
        intent.putExtra("title", "Matching Results");
        intent.putIntegerArrayListExtra("list", sendRecipeList);

        startActivity(intent);
        // Optional: finish(); if you don't want the user to come back to this temp screen
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}