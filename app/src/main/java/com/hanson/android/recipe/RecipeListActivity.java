package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Model.RecipeItem;
import java.util.ArrayList;

public class RecipeListActivity extends AppCompatActivity {

    private ArrayList<RecipeItem> recipes = new ArrayList<>();
    private TextView txtTitle, txtRecipeCount;
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter adapter; // Naya Adapter
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // 1. Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(""); // Custom title layout mein handle ho raha hai
        }

        // 2. Initialize Views
        txtTitle = findViewById(R.id.txt_recipeListTitle);
        txtRecipeCount = findViewById(R.id.txt_recipeCount);
        recyclerView = findViewById(R.id.listview_recipelist);

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DBHelper(this, "Recipes.db", null, 1);

        // 4. FAB Logic (Optional: Add Recipe)
        findViewById(R.id.fab_add_recipe).setOnClickListener(v -> {
            // Toast.makeText(this, "Add Recipe Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }

    private void refreshList() {
        recipes.clear();
        Intent intent = getIntent();

        if (intent.hasExtra("category")) {
            String category = intent.getStringExtra("category");
            txtTitle.setText("🍳 " + category);
            recipes = dbHelper.recipes_SelectByCategory(category);
        }
        else if (intent.hasExtra("title")) {
            String searchTitle = intent.getStringExtra("title");
            txtTitle.setText("🔍 " + searchTitle);
            ArrayList<Integer> ids = intent.getIntegerArrayListExtra("list");
            if (ids != null) {
                for (Integer id : ids) {
                    RecipeItem r = dbHelper.recipes_SelectById(id);
                    if (r != null) recipes.add(r);
                }
            }
        }

        // Recipe Count Update
        txtRecipeCount.setText(recipes.size() + " Recipes Found");

        // 5. Set Adapter (Interface implementation for click)
        adapter = new RecipeRecyclerAdapter(this, recipes, item -> {
            Intent recipeIntent = new Intent(RecipeListActivity.this, RecipeActivity.class);
            recipeIntent.putExtra("recipe", item.get_recipeName());
            startActivity(recipeIntent);
        });
        recyclerView.setAdapter(adapter);
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