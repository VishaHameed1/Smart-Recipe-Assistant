package com.hanson.android.recipe;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

// CORRECTED ANDROIDX IMPORTS
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class AddIngredientFragment extends Fragment {

    private EditText txt_ingredient;
    private ListView list_ingredient;
    private Button btn_ingredient;
    private ArrayList<String> ingredientList;
    private AddIngredientAdapter adapter;
    private InputMethodManager inputMethodManager;

    public AddIngredientFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_ingredient, container, false);

        // setRetainInstance(true) is deprecated in AndroidX.
        // Fragments are now retained automatically by the FragmentManager.

        // Initialize keyboard manager safely
        if (getActivity() != null) {
            inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        btn_ingredient = view.findViewById(R.id.btn_Add_IngredientAdd);
        txt_ingredient = view.findViewById(R.id.txt_Add_IngredientAdd);
        list_ingredient = view.findViewById(R.id.ListView_Add_Ingredient);

        ingredientList = new ArrayList<>();

        // Initialize the adapter once
        adapter = new AddIngredientAdapter(requireContext(), ingredientList, R.layout.fragment_add_ingredientitem);
        list_ingredient.setAdapter(adapter);

        btn_ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = txt_ingredient.getText().toString().trim();
                if (input.length() > 0) {
                    ingredientList.add(input);
                    txt_ingredient.setText("");

                    // Refresh the list efficiently
                    adapter.notifyDataSetChanged();

                    // Hide keyboard
                    if (inputMethodManager != null && txt_ingredient.getWindowToken() != null) {
                        inputMethodManager.hideSoftInputFromWindow(txt_ingredient.getWindowToken(), 0);
                    }
                } else {
                    Toast.makeText(getContext(), "Please, Insert your ingredient", Toast.LENGTH_SHORT).show();
                }
            }
        });

        list_ingredient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Remove item and notify adapter instead of re-setting it
                ingredientList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }
}