package com.hanson.android.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;

/**
 * Updated by Gemini on 2026-01-03.
 * Optimized with ViewHolder Pattern and safety checks.
 */
public class MainRecipeAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<RecipeItem> recipeList;
    private int layout;
    private ImageHelper imageHelper = new ImageHelper();

    public MainRecipeAdapter(Context context, ArrayList<RecipeItem> recipeList, int layout) {
        if (context != null) {
            this.inflater = LayoutInflater.from(context);
        }
        this.recipeList = recipeList;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return (recipeList != null) ? recipeList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (recipeList != null) ? recipeList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) return null;

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);

            holder = new ViewHolder();
            // Match these IDs with your fragment_home_recipeitem.xml
            holder.imgRecipe = convertView.findViewById(R.id.img_mainListItem);
            holder.txtName = convertView.findViewById(R.id.txt_mainListItem);
            holder.txtRating = convertView.findViewById(R.id.txt_rating);
            holder.txtCategory = convertView.findViewById(R.id.txt_category);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RecipeItem recipeItem = recipeList.get(position);

        if (recipeItem != null) {
            // 1. Image Loading with fallback
            if (recipeItem.get_thumbnail() != null && recipeItem.get_thumbnail().length > 0) {
                holder.imgRecipe.setImageBitmap(imageHelper.getBitmapFromByteArray(recipeItem.get_thumbnail()));
            } else {
                // Default placeholder image
                holder.imgRecipe.setImageResource(R.drawable.recipesideasmain);
            }

            // 2. Data Binding
            if (holder.txtName != null) {
                holder.txtName.setText(recipeItem.get_recipeName());
            }

            // Category fetch using the standardized method we added earlier
            if (holder.txtCategory != null) {
                holder.txtCategory.setText(recipeItem.get_recipeCategory());
            }

            // Score/Rating binding
            if (holder.txtRating != null) {
                holder.txtRating.setText("⭐ " + recipeItem.get_recipeScore());
            }
        }

        return convertView;
    }

    // Performance Optimization: ViewHolder pattern avoids repeated findViewById() calls
    static class ViewHolder {
        ImageView imgRecipe;
        TextView txtName, txtRating, txtCategory;
    }
}