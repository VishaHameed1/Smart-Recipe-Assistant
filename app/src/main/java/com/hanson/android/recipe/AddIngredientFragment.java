package com.hanson.android.recipe;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class AddIngredientFragment extends Fragment {
    private TextView emptyStateView;

    private EditText txt_ingredient;
    private ListView list_ingredient;
    private Button btn_ingredient;
    private InputMethodManager imm;
    private ArrayList<String> ingredientList;
    private AddIngredientAdapter adapter;

    // AddIngredientFragment ke andar ye method zaroor rakhen
    public ArrayList<String> getIngredientList() {
        return ingredientList != null ? ingredientList : new ArrayList<>();
    }

    public AddIngredientFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ingredient, container, false);

        // IDs Initialization
        btn_ingredient = view.findViewById(R.id.btn_Add_IngredientAdd);
        txt_ingredient = view.findViewById(R.id.txt_Add_IngredientAdd);
        list_ingredient = view.findViewById(R.id.ListView_Add_Ingredient);
        emptyStateView = view.findViewById(R.id.emptyStateView);

        if (getActivity() != null) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        ingredientList = new ArrayList<>();
        // Adapter setup
        adapter = new AddIngredientAdapter(requireContext(), ingredientList, R.layout.fragment_add_ingredientitem);
        list_ingredient.setAdapter(adapter);
        list_ingredient.setEmptyView(emptyStateView);

        // Add Button Logic
        btn_ingredient.setOnClickListener(v -> {
            String input = txt_ingredient.getText().toString().trim();
            if (!input.isEmpty()) {
                ingredientList.add(input);
                txt_ingredient.setText("");
                adapter.notifyDataSetChanged();
                if (imm != null) imm.hideSoftInputFromWindow(txt_ingredient.getWindowToken(), 0);
            } else {
                Toast.makeText(getContext(), "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            }
        });


        // Delete Item Logic
        list_ingredient.setOnItemClickListener((parent, v, position, id) -> {
            ingredientList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Ingredient removed", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}