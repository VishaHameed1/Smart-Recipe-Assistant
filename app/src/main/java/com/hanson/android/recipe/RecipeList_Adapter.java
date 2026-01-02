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

public class RecipeList_Adapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final ArrayList<RecipeItem> recipeList;
    private final int layout;
    private final ImageHelper imageHelper = new ImageHelper();

    public RecipeList_Adapter(Context context, ArrayList<RecipeItem> recipeList, int layout){
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.recipeList = recipeList;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Layout inflate karna
            convertView = inflater.inflate(layout, parent, false);

            // ViewHolder initialize karna
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.listItem_image);
            holder.txtTitle = convertView.findViewById(R.id.listItem_title);
            holder.txtAuthor = convertView.findViewById(R.id.listItem_author);
            holder.txtLikeCount = convertView.findViewById(R.id.listItem_likecount);

            convertView.setTag(holder);
        } else {
            // Purana view reuse karna
            holder = (ViewHolder) convertView.getTag();
        }

        RecipeItem recipeItem = recipeList.get(position);

        if (recipeItem != null) {
            // Title aur Author set karna
            holder.txtTitle.setText(recipeItem.get_recipeName());
            holder.txtAuthor.setText("👨‍🍳 " + recipeItem.get_author());
            holder.txtLikeCount.setText(String.valueOf(recipeItem.get_likeCount()));

            // Image handling (Null check ke saath)
            if (recipeItem.get_thumbnail() != null) {
                holder.imgThumbnail.setImageBitmap(imageHelper.getBitmapFromByteArray(recipeItem.get_thumbnail()));
            } else {
                // Default image agar recipe image na ho
                holder.imgThumbnail.setImageResource(R.drawable.gradient_border_card);
            }
        }

        return convertView;
    }

    // --- ViewHolder Pattern for Smooth Scrolling ---
    static class ViewHolder {
        ImageView imgThumbnail;
        TextView txtTitle;
        TextView txtAuthor;
        TextView txtLikeCount;
    }
}