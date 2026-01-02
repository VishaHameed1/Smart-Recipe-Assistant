package com.hanson.android.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.RecipeItem;

import java.util.ArrayList;

public class RecipeRecyclerAdapter extends RecyclerView.Adapter<RecipeRecyclerAdapter.RecipeViewHolder> {

    private Context context;
    private ArrayList<RecipeItem> recipeList;
    private OnItemClickListener listener;
    private ImageHelper imageHelper = new ImageHelper();

    public RecipeRecyclerAdapter(Context context, ArrayList<RecipeItem> recipeList, OnItemClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_recipe_list_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeItem item = recipeList.get(position);
        holder.title.setText(item.get_recipeName());
        holder.author.setText("Chef " + item.get_author());
        holder.likes.setText(String.valueOf(item.get_likeCount()));

        if (item.get_thumbnail() != null) {
            holder.image.setImageBitmap(imageHelper.getBitmapFromByteArray(item.get_thumbnail()));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(RecipeItem item);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, author, likes;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.listItem_image);
            title = itemView.findViewById(R.id.listItem_title);
            author = itemView.findViewById(R.id.listItem_author);
            likes = itemView.findViewById(R.id.listItem_likecount);
        }
    }
}