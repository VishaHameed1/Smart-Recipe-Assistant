package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

//import com.google.ai.client.generativeai.GenerativeModel;
//import com.google.ai.client.generativeai.java.GenerativeModelFutures;
//import com.google.ai.client.generativeai.type.Content;
//import com.google.ai.client.generativeai.type.GenerateContentResponse;
//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
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

    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int clickCounter = 0;

//    private GenerativeModelFutures model;
    private Client genAiClient;


    public SearchFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // 1. Gemini Initialize (Paste your API Key here)
//        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyA1dYTy1uFqiVz-UPeFJ9_XLL99OhPWgzg");
//        model = GenerativeModelFutures.from(gm);
//        genAiClient = Client.builder()
//                .apiKey("AIzaSyA1dYTy1uFqiVz-UPeFJ9_XLL99OhPWgzg")
//                .build();


        editText = view.findViewById(R.id.typeIngredient);
        addButton = view.findViewById(R.id.addIngredient);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        gridView = view.findViewById(R.id.gridSearch);

        adapter = new ArrayAdapter<>(requireContext(), R.layout.edit_text_custom_for_ingredients, listItems);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, v, position, id) -> {
            listItems.remove(position);
            clickCounter--;
            adapter.notifyDataSetChanged();
            if (clickCounter < 20) addButton.setClickable(true);
        });

        addButton.setOnClickListener(this);
        buttonSearch.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String input = editText.getText().toString().trim();

        if (id == R.id.addIngredient) {
            if (input.toLowerCase().startsWith("/ai")) {
                String prompt = input.length() > 3 ? input.substring(3).trim() : "";
                askGemini(prompt);
            } else {
                handleAddIngredient(input);
            }
        } else if (id == R.id.buttonSearch) {
            handleSearch();
        }
    }

//    private void askGemini(String prompt) {
//        if (prompt.isEmpty()) {
//            Toast.makeText(requireContext(), "Please ask something...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Toast.makeText(requireContext(), "Gemini is thinking...", Toast.LENGTH_SHORT).show();
//
//        Content content = new Content.Builder().addText(prompt).build();
//        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
//
//        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
//            @Override
//            public void onSuccess(GenerateContentResponse result) {
//                String aiText = result.getText(); // Result yahan milta hai
//
//                requireActivity().runOnUiThread(() -> {
//                    if (aiText != null && !aiText.isEmpty()) {
//                        // ✅ FIX: Data ko SearchResult activity mein bhejna
//                        Intent intent = new Intent(requireActivity(), SearchResult.class);
//                        intent.putExtra("ai_response", aiText);
//                        intent.putExtra("is_ai", true);
//                        startActivity(intent);
//                        editText.setText("");
//                    } else {
//                        Toast.makeText(requireContext(), "No answer from AI (Safety Filter)", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(@NonNull Throwable t) {
//                Log.e("GEMINI_ERROR", "API Failed: " + t.getMessage());
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(requireContext(), "Connection Error: " + t.getMessage(), Toast.LENGTH_LONG).show()
//                );
//            }
//        }, ContextCompat.getMainExecutor(requireContext()));
//    }

    private void askGemini(String prompt) {

        if (prompt.isEmpty()) {
            Toast.makeText(requireContext(), "Please ask something...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (genAiClient == null) {
            genAiClient = Client.builder()
                    .apiKey("AIzaSyA1dYTy1uFqiVz-UPeFJ9_XLL99OhPWgzg")
                    .build();
        }

        Toast.makeText(requireContext(), "Gemini is thinking...", Toast.LENGTH_SHORT).show();

        String systemInstruction =
                "You are a specialized Cooking Assistant and Cost Estimator. " +
                        "1. ONLY answer questions related to food recipes, cooking techniques, ingredients, and ingredient prices. " +
                        "2. When providing recipes, always estimate the total cost based on the user's country or the country specified. " +
                        "3. Use local currencies (e.g., PKR for Pakistan, INR for India, USD for USA, etc.) based on the context. " +
                        "4. If the user asks about anything else, reply: " +
                        "'I can only help you with food recipes, cooking tips, and budget estimations.'";
        String finalPrompt = systemInstruction + "\n\nUser Question: " + prompt;

        new Thread(() -> {
            try {
                GenerateContentResponse response =
                        genAiClient.models.generateContent(
                                "gemini-2.5-flash",
                                finalPrompt,
                                null
                        );

                String aiText = response.text();

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (aiText != null && !aiText.isEmpty()) {
                        Intent intent = new Intent(requireActivity(), SearchResult.class);
                        intent.putExtra("ai_response", aiText);
                        intent.putExtra("is_ai", true);
                        startActivity(intent);
                        editText.setText("");
                    } else {
                        Toast.makeText(requireContext(), "Empty AI response", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("GEMINI_ERROR", e.getMessage(), e);

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Gemini Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }






    // --- Baki database methods (handleAddIngredient, handleSearch etc.) same raheinge ---

    private void handleAddIngredient(String input) {
        if (clickCounter < 20 && !input.isEmpty()) {
            if (!listItems.contains(input)) {
                listItems.add(input);
                adapter.notifyDataSetChanged();
                clickCounter++;
                editText.setText("");
            } else {
                Toast.makeText(requireContext(), "Already added", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSearch() {
        if (listItems.isEmpty()) return;
        DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);
        ArrayList<SearchResultItem> resultList = dbHelper.ingredients_selectRecipeByIngredientName(listItems);

        if (resultList == null || resultList.isEmpty()) {
            ArrayList<Integer> advices = findBestRecipes();
            Intent intent = new Intent(requireActivity(), RecipeListActivity.class);
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
            Intent intent = new Intent(requireActivity(), SearchResult.class);
            intent.putIntegerArrayListExtra("idrecipes", idRecipes);
            intent.putIntegerArrayListExtra("matchesNoDuplicates", removeDuplicates(matches));
            intent.putIntegerArrayListExtra("matches", matches);
            intent.putExtra("is_ai", false);
            startActivity(intent);
        }
        dbHelper.close();
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