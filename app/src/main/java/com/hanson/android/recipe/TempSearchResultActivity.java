package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

// CORRECT ANDROIDX IMPORTS
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TempSearchResultActivity extends AppCompatActivity {

    private ArrayList<Integer> sendRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_search_result);

        // Setup ActionBar for consistency with other activities
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Test Search Results");
        }

        // Test dummy data
        sendRecipeList.add(1);
        sendRecipeList.add(2);
        sendRecipeList.add(3);
        sendRecipeList.add(4);

        Button send = findViewById(R.id.btn_sendRecipeList);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RecipeListActivity
                Intent intent = new Intent(TempSearchResultActivity.this, RecipeListActivity.class);

                // Note: Title fixed to "Matching"
                intent.putExtra("title", "Matching 3 ingredients");
                intent.putIntegerArrayListExtra("list", sendRecipeList);

                startActivity(intent);
            }
        });
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