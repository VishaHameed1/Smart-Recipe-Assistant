package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

// CORRECTED ANDROIDX IMPORTS
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Model.CategoryItem;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_category, container, false);

        ListView listView = view.findViewById(R.id.listVeiw_category);

        // Initialize Database Helper
        DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);

        // Fetch category list
        final ArrayList<CategoryItem> categoryList = dbHelper.recipes_SelectCategory();

        // Set the adapter
        // Note: Ensure your 'category_adapter' class imports are also updated to AndroidX
        listView.setAdapter(new category_adapter(requireContext(), categoryList, R.layout.fragment_category_item));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryItem selectCategory = categoryList.get(position);

                // Navigate to RecipeListActivity with selected category
                Intent intent = new Intent(requireActivity(), RecipeListActivity.class);
                intent.putExtra("category", selectCategory.get_category());
                startActivity(intent);
            }
        });

        return view;
    }
}