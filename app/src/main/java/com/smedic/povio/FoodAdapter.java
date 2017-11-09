package com.smedic.povio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smedic.povio.model_demo.Food;

import java.util.List;
import java.util.Locale;

/**
 * Created by smedic on 17.2.17..
 * Adapter used for menu tab in restaurant details
 */

public class FoodAdapter extends ArrayAdapter<Food> {

    public FoodAdapter(Context context, List<Food> menu) {
        super(context, 0, menu);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Food food = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_food, parent, false);
        }
        // Lookup view for data population
        TextView foodName = (TextView) convertView.findViewById(R.id.food_name);
        TextView foodPrice = (TextView) convertView.findViewById(R.id.food_price);
        // Populate the data into the template view using the data object
        if (food != null) {
            foodName.setText(food.getName());
            foodPrice.setText(String.format(Locale.getDefault(), "%.2f", food.getPrice()));
        }
        // Return the completed view to render on screen
        return convertView;
    }

}
