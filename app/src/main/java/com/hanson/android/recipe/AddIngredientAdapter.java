package com.hanson.android.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AddIngredientAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<String> ingredientList;
    private int layout;

    public AddIngredientAdapter(Context context, ArrayList<String> ingredientList, int layout) {
        // Context null check taaki crash na ho
        if (context != null) {
            this.inflater = LayoutInflater.from(context);
        }
        this.ingredientList = ingredientList;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return ingredientList != null ? ingredientList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return ingredientList.get(position);
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

            // ViewHolder setup karna taaki baar baar findViewById na karna paray
            holder = new ViewHolder();
            holder.txtIngredient = convertView.findViewById(R.id.txt_ingredient);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Data set karna
        String ingredientItem = ingredientList.get(position);
        if (ingredientItem != null && holder.txtIngredient != null) {
            holder.txtIngredient.setText(ingredientItem);
        }

        return convertView;
    }

    // Performance behtar karne ke liye ViewHolder use karein
    static class ViewHolder {
        TextView txtIngredient;
    }
}