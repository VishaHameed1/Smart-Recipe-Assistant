package com.hanson.android.recipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Model.RecipeItem;
import com.hanson.android.recipe.Model.SearchResultItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class SearchFragment extends Fragment implements View.OnClickListener {

    private EditText editText;
    private Button addButton, buttonSearch;
    private GridView gridView;
    private TextView txtEmptyState; // XML se sync karne ke liye

    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int clickCounter = 0;
    private Client genAiClient;

    public SearchFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // UI Initialization
        editText = view.findViewById(R.id.typeIngredient);
        addButton = view.findViewById(R.id.addIngredient);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        gridView = view.findViewById(R.id.gridSearch);
        txtEmptyState = view.findViewById(R.id.txt_empty_ingredients);

        // Adapter setup
        adapter = new ArrayAdapter<>(requireContext(), R.layout.edit_text_custom_for_ingredients, listItems);
        gridView.setAdapter(adapter);

        // Delete ingredient on click
        gridView.setOnItemClickListener((parent, v, position, id) -> {
            listItems.remove(position);
            clickCounter--;
            adapter.notifyDataSetChanged();
            updateEmptyState();
            if (clickCounter < 20) addButton.setClickable(true);
        });

        addButton.setOnClickListener(this);
        buttonSearch.setOnClickListener(this);

        updateEmptyState();
        return view;
    }

    private void updateEmptyState() {
        if (txtEmptyState != null) {
            txtEmptyState.setVisibility(listItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String input = editText.getText().toString().trim();

        if (id == R.id.addIngredient) {
            if (input.toLowerCase().startsWith("/ai")) {
                hideKeyboard();
                String prompt = input.length() > 3 ? input.substring(3).trim() : "";
                askGemini(prompt);
            } else {
                handleAddIngredient(input);
            }
        } else if (id == R.id.buttonSearch) {
            hideKeyboard();
            handleSearch();
        }
    }

    private void askGemini(String prompt) {
        if (prompt.isEmpty()) {
            Toast.makeText(requireContext(), "Please ask something...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (genAiClient == null) {
            // Replace with your actual API Key
            genAiClient = Client.builder().apiKey("AIzaSyAb6TwlKcZk5WsUYUzyme3lULMMEqstpzc").build();
        }

        Toast.makeText(requireContext(), "Gemini is thinking...", Toast.LENGTH_SHORT).show();

        String systemInstruction = "You are a specialized Cooking Assistant and Cost Estimator. " +
                "1. ONLY answer questions related to food recipes, cooking techniques, ingredients, and ingredient prices. " +
                "2. When providing recipes, always estimate calories and the total cost based on local currencies (e.g., PKR, INR, USD). " +
                "3. If the user asks about anything else, reply: 'I can only help you with food recipes.'";

        String finalPrompt = systemInstruction + "\n\nUser Question: " + prompt;

        new Thread(() -> {
            try {
                GenerateContentResponse response = genAiClient.models.generateContent(
                        "gemini-2.5-flash", // Check availability of 2.5, using 2.0 for stability
                        finalPrompt,
                        null
                );

                String aiText = response.text();

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (aiText != null && !aiText.isEmpty()) {
                            Intent intent = new Intent(getActivity(), SearchResult.class);
                            intent.putExtra("ai_response", aiText);
                            intent.putExtra("is_ai", true);
                            startActivity(intent);
                            editText.setText("");
                        } else {
                            Toast.makeText(requireContext(), "Empty AI response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("GEMINI_ERROR", "Error: " + e.getMessage());
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Gemini Error: Check API Key or Connection", Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }

    private void handleAddIngredient(String input) {
        if (input.isEmpty()) return;

        if (clickCounter < 20) {
            if (!listItems.contains(input)) {
                listItems.add(input);
                adapter.notifyDataSetChanged();
                clickCounter++;
                editText.setText("");
                updateEmptyState();
            } else {
                Toast.makeText(requireContext(), "Already added", Toast.LENGTH_SHORT).show();
            }
        } else {
            addButton.setClickable(false);
            Toast.makeText(requireContext(), "Limit reached (20)", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSearch() {
        if (listItems.isEmpty()) {
            Toast.makeText(requireContext(), "Add ingredients first!", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);
        ArrayList<SearchResultItem> resultList = dbHelper.ingredients_selectRecipeByIngredientName(listItems);

        if (resultList == null || resultList.isEmpty()) {
            ArrayList<Integer> advices = findBestRecipes();
            Intent intent = new Intent(getActivity(), RecipeListActivity.class);
            intent.putExtra("title", "Popular Recipes:");
            intent.putIntegerArrayListExtra("list", advices);
            startActivity(intent);
        } else {
            ArrayList<Integer> idRecipes = new ArrayList<>();
            ArrayList<Integer> matches = new ArrayList<>();
            for (SearchResultItem item : resultList) {
                idRecipes.add(item.get_recipeId());
                matches.add(item.get_ingrCount());
            }
            Intent intent = new Intent(getActivity(), SearchResult.class);
            intent.putIntegerArrayListExtra("idrecipes", idRecipes);
            intent.putIntegerArrayListExtra("matchesNoDuplicates", removeDuplicates(matches));
            intent.putIntegerArrayListExtra("matches", matches);
            intent.putExtra("is_ai", false);
            startActivity(intent);
        }
        dbHelper.close();
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public ArrayList<Integer> findBestRecipes() {
        ArrayList<Integer> bestRecipes = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);
        ArrayList<RecipeItem> results = dbHelper.recipes_SelectBest();
        if (results != null) {
            for (RecipeItem item : results) bestRecipes.add(item.get_id());
        }
        dbHelper.close();
        return bestRecipes;
    }

    public ArrayList<Integer> removeDuplicates(ArrayList<Integer> array) {
        Set<Integer> set = new LinkedHashSet<>(array);
        return new ArrayList<>(set);
    }
}