package com.hanson.android.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hanson.android.recipe.Helper.DBHelper;
import com.hanson.android.recipe.Model.CategoryItem;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_category, container, false);

        ListView listView = view.findViewById(R.id.listVeiw_category);
        TextView emptyText = view.findViewById(R.id.txt_empty_categories);

        // Database logic
        DBHelper dbHelper = new DBHelper(requireContext(), "Recipes.db", null, 1);
        final ArrayList<CategoryItem> categoryList = dbHelper.recipes_SelectCategory();

        // Check if data exists
        if (categoryList == null || categoryList.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);

            listView.setAdapter(new category_adapter(requireContext(), categoryList, R.layout.fragment_category_item));
        }

        // On Click Listener
        listView.setOnItemClickListener((parent, v, position, id) -> {
            CategoryItem selectCategory = categoryList.get(position);
            Intent intent = new Intent(requireActivity(), RecipeListActivity.class);
            intent.putExtra("category", selectCategory.get_category());
            startActivity(intent);
        });

        return view;
    }
}