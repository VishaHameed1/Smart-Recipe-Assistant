package com.hanson.android.recipe;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private final ImageHelper imageHelper = new ImageHelper();
    private ArrayList<RecipeItem> bestList;
    private ArrayList<RecipeItem> newList;
    private final Date today = new Date();

    // Background threads ke liye Executor
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HomeFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Search FAB Setup
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
            if (navigationView != null) {
                navigationView.setCheckedItem(R.id.nav_search);
            }

            FragmentManager manager = requireActivity().getSupportFragmentManager();
            SearchFragment searchFragment = new SearchFragment();
            manager.beginTransaction()
                    .replace(R.id.root_layout, searchFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 2. GridView Initialization
        GridView newGridView = view.findViewById(R.id.GridView_New);
        GridView bestGridView = view.findViewById(R.id.GridView_Best);

        // 3. Background Database Work
        executorService.execute(() -> {
            // Check if fragment is still attached before database call
            if (!isAdded() || getContext() == null) return;

            DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);
            ArrayList<RecipeItem> allRecipes = dbHelper.recipes_SelectAll();

            // Seed data only if DB is empty
            if (allRecipes == null || allRecipes.isEmpty()) {
                seedDefaultData(dbHelper);
            }

            // Fetch specific lists
            newList = dbHelper.recipes_SelectNew();
            bestList = dbHelper.recipes_SelectBest();

            // Switch to Main Thread to update UI
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                if (newList != null) {
                    newGridView.setAdapter(new MainRecipeAdapter(requireContext(), newList, R.layout.fragment_home_recipeitem));
                    newGridView.setOnItemClickListener((parent, v, position, id) ->
                            openRecipeActivity(newList.get(position).get_recipeName()));
                }

                if (bestList != null) {
                    bestGridView.setAdapter(new MainRecipeAdapter(requireContext(), bestList, R.layout.fragment_home_recipeitem));
                    bestGridView.setOnItemClickListener((parent, v, position, id) ->
                            openRecipeActivity(bestList.get(position).get_recipeName()));
                }
            });
        });

        return view;
    }

    private void openRecipeActivity(String recipeName) {
        Intent intent = new Intent(getActivity(), RecipeActivity.class);
        intent.putExtra("recipe", recipeName);
        startActivity(intent);
    }

    private void seedDefaultData(DBHelper dbHelper) {
        insertRecipe(dbHelper, "Korea", "Bibimbap", "shyjoo",
                "1. rice \n 2. hubs and egg \n 3. minx", "Korean traditional food",
                R.drawable.bibimbap, new String[]{"rice", "egg", "sesame oil", "gochujang", "carrot"}, 0);

        insertRecipe(dbHelper, "Korea", "Bulgogi", "shyjoo",
                "1. Slice beef. 2. Marinade. 3. Grill.", "Korean grilled beef",
                R.drawable.bulgogi, new String[]{"soy sauce", "brown sugar", "beef"}, 4);

        insertRecipe(dbHelper, "Italy", "Bolognese", "shyjoo",
                "1. Fry onion. 2. Add meat. 3. Serve.", "Classic Italian pasta",
                R.drawable.bolognese, new String[]{"onion", "spaghetti", "beef"}, 6);
    }

    private void insertRecipe(DBHelper db, String cat, String name, String author, String howto, String desc, int imgRes, String[] ingredients, int score) {
        if (!isAdded() || getContext() == null) return;

        Drawable d = ContextCompat.getDrawable(requireContext(), imgRes);
        if (d != null) {
            byte[] img = imageHelper.getByteArrayFromDrawable(d);
            db.recipes_Insert(cat, name, author, today.toString(), howto, desc, img, img, score);

            int id = db.recipes_GetIdByName(name);
            for (String ing : ingredients) {
                db.ingredients_Insert(id, ing);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Memory Leak aur background crash se bachne ke liye shutdown zaroori hai
        executorService.shutdownNow();
    }
}