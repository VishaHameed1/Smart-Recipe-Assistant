package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

// CORRECT ANDROIDX IMPORTS
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;

public class RecipeListActivity extends AppCompatActivity {

    private ArrayList<RecipeItem> recipes = new ArrayList<>();
    private TextView txtTitle;
    private ListView listView;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Setup ActionBar with back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Initialize UI elements in onCreate (Best Practice)
        txtTitle = findViewById(R.id.txt_recipeListTitle);
        listView = findViewById(R.id.listview_recipelist);

        // Initialize Database Helper
        dbHelper = new DBHelper(this, "Recipes.db", null, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }

    private void refreshList() {
        recipes.clear();
        String title = "";

        Intent intent = getIntent();

        // Handle Category Filtering
        if (intent.hasExtra("category")) {
            title = intent.getStringExtra("category");
            txtTitle.setText(title);
            recipes = dbHelper.recipes_SelectByCategory(title);
        }
        // Handle Search Results Filtering
        else if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title");
            txtTitle.setText(title);

            ArrayList<Integer> receivedRecipeIds = intent.getIntegerArrayListExtra("list");
            if (receivedRecipeIds != null) {
                for (Integer id : receivedRecipeIds) {
                    RecipeItem getRecipe = dbHelper.recipes_SelectById(id);
                    if (getRecipe != null) {
                        recipes.add(getRecipe);
                    }
                }
            }
        }

        // Set the Adapter with Data
        RecipeList_Adapter adapter = new RecipeList_Adapter(this, recipes, R.layout.activity_recipe_list_item);
        listView.setAdapter(adapter);

        // Set Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                RecipeItem selectRecipe = recipes.get(position);
                Intent recipeIntent = new Intent(RecipeListActivity.this, RecipeActivity.class);
                recipeIntent.putExtra("recipe", selectRecipe.get_recipeName());
                startActivity(recipeIntent);
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