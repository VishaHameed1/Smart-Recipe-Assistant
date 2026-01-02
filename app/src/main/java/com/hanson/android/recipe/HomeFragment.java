package com.hanson.android.recipe;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private final ImageHelper imageHelper = new ImageHelper();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DBHelper dbHelper;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. GridViews setup
        GridView newGridView = view.findViewById(R.id.GridView_New);
        GridView bestGridView = view.findViewById(R.id.GridView_Best);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            newGridView.setNestedScrollingEnabled(false);
            bestGridView.setNestedScrollingEnabled(false);
        }

        // 2. Search FAB
        FloatingActionButton fab = view.findViewById(R.id.fab_home_search);
        fab.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Database & UI Load
        loadData(view, newGridView, bestGridView);
    }

    private void loadData(View rootView, GridView newGrid, GridView bestGrid) {
        // Safe context capture
        Context context = getContext();
        if (context == null) return;

        executorService.execute(() -> {
            // Background Task
            dbHelper = new DBHelper(context, "Recipes.db", null, 1);

            // Seed only if empty
            if (dbHelper.recipes_SelectAll().isEmpty()) {
                seedDefaultData(context);
            }

            ArrayList<RecipeItem> newList = dbHelper.recipes_SelectNew();
            ArrayList<RecipeItem> bestList = dbHelper.recipes_SelectBest();
            int totalCount = dbHelper.recipes_SelectAll().size();

            // UI Thread Update
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Update New Recipes
                    if (newList != null) {
                        newGrid.setAdapter(new MainRecipeAdapter(context, newList, R.layout.fragment_home_recipeitem));
                        newGrid.setOnItemClickListener((p, v, pos, id) -> openRecipe(newList.get(pos).get_recipeName()));
                    }

                    // Update Best Recipes
                    if (bestList != null) {
                        bestGrid.setAdapter(new MainRecipeAdapter(context, bestList, R.layout.fragment_home_recipeitem));
                        bestGrid.setOnItemClickListener((p, v, pos, id) -> openRecipe(bestList.get(pos).get_recipeName()));
                    }

                    // Update Count
                    TextView recipeStat = rootView.findViewById(R.id.stat_recipes_count);
                    if (recipeStat != null) recipeStat.setText(totalCount + "+");
                });
            }
        });
    }

    private void seedDefaultData(Context context) {
        // Zyada recipes add karein taake UI bhara hua lage
        insertRecipe(context, "Italian", "Spaghetti Carbonara", "Chef Visha", "Boil pasta...",
                "Classic Italian creamy pasta.", R.drawable.recipesideasmain, new String[]{"Pasta", "Egg", "Cheese"}, 5);

        insertRecipe(context, "Indian", "Butter Chicken", "Chef Hanson", "Cook chicken in gravy...",
                "Rich and creamy Indian chicken.", R.drawable.recipesideasmain, new String[]{"Chicken", "Butter", "Tomato"}, 5);
    }

    private void insertRecipe(Context context, String cat, String name, String author, String howto, String desc, int imgRes, String[] ingredients, int score) {
        Drawable d = ContextCompat.getDrawable(context, imgRes);
        if (d != null && dbHelper != null) {
            byte[] img = imageHelper.getByteArrayFromDrawable(d);
            dbHelper.recipes_Insert(cat, name, author, new Date().toString(), howto, desc, img, img, score);
            int id = dbHelper.recipes_GetIdByName(name);
            for (String ing : ingredients) {
                dbHelper.ingredients_Insert(id, ing);
            }
        }
    }

    private void openRecipe(String name) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), RecipeActivity.class);
            intent.putExtra("recipe", name);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}