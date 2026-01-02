package com.hanson.android.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanson.android.recipe.Helper.ImageHelper;
import com.hanson.android.recipe.Model.CategoryItem;

import java.util.ArrayList;

public class category_adapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CategoryItem> categoryList;
    private int layout;
    private ImageHelper imageHelper = new ImageHelper();

    public category_adapter(Context context, ArrayList<CategoryItem> categoryList, int layout) {
        this.inflater = LayoutInflater.from(context);
        this.categoryList = categoryList;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.categoryitem_img);
            holder.name = convertView.findViewById(R.id.categoryitem_text);
            holder.count = convertView.findViewById(R.id.categoryitem_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoryItem categoryItem = categoryList.get(position);

        // Data binding
        if (categoryItem.get_mainImg() != null) {
            holder.icon.setImageBitmap(imageHelper.getBitmapFromByteArray(categoryItem.get_mainImg()));
        }

        holder.name.setText(categoryItem.get_category());

        // Note: Agar aapne model mein count nahi rakha toh ise hide ya static kar sakte hain
        // holder.count.setText("🍽️ 15 Recipes");

        return convertView;
    }

    // Performance ke liye ViewHolder
    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView count; // New field from your XML
    }
}