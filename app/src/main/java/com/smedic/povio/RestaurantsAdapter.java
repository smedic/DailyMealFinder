package com.smedic.povio;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.smedic.povio.interfaces.OnRecycleViewItemClickListener;
import com.smedic.povio.model.Restaurant;
import com.smedic.povio.model_demo.Food;
import com.smedic.povio.utils.CircleTransform;
import com.smedic.povio.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.smedic.povio.Config.PHOTOS_URL;

/**
 * Created by smedic on 17.2.17..
 */

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.ViewHolder>
        implements View.OnClickListener {

    private static final String TAG = "RestaurantsAdapter";

    private OnRecycleViewItemClickListener<Restaurant> itemClickListener;
    private List<Restaurant> restaurantList;
    private List<Restaurant> restaurantListCopy;
    private Context context;
    private String searchTerm;
    private TextAppearanceSpan highlightTextSpan;

    public RestaurantsAdapter(Context context, List<Restaurant> restaurantList) {
        if (restaurantList != null) {
            this.restaurantList = restaurantList;
            this.context = context;
            restaurantListCopy = new ArrayList<>();
            restaurantListCopy.addAll(restaurantList);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, null);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int i) {

        Restaurant restaurant = restaurantList.get(i);

        // highlighting searched text
        final int startIndex = indexOfSearchQuery(restaurant.getName());
        if (startIndex == -1) {
            holder.title.setText(restaurant.getName());
        } else {
            String name = restaurant.getName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final SpannableString highlightedName = new SpannableString(restaurant.getName());
                highlightedName.setSpan(highlightTextSpan, startIndex, startIndex + searchTerm.length(), 0);
                holder.title.setText(highlightedName);
            } else {
                holder.title.setText(name);
            }
        }

        holder.type.setText(Utils.capitalizeWord(restaurant.getTypes().get(0)));

        if (restaurant.getOpeningHours() != null) {
            holder.status.setVisibility(View.VISIBLE);
            if (restaurant.getOpeningHours().getOpenNow()) {
                holder.status.setText(R.string.open);
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.greenOpened, null));
            } else {
                holder.status.setText(R.string.closed);
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.redClosed, null));
            }
        } else {
            holder.status.setVisibility(View.GONE);
        }

        if (restaurant.getRating() != null) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(restaurant.getRating());
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {

            String photoUrl = String.format(PHOTOS_URL, restaurant.getPhotos().get(0).getPhotoReference(),
                    Config.API_KEY);
            //Render image using Picasso library
            Picasso.with(context).load(photoUrl)
                    .error(R.drawable.placeholder_circle)
                    .placeholder(R.drawable.placeholder_circle)
                    .transform(new CircleTransform())
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_circle);
        }

        holder.itemView.setTag(restaurant);
    }

    @Override
    public int getItemCount() {
        return (null != restaurantList ? restaurantList.size() : 0);
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            Restaurant item = (Restaurant) v.getTag();
            itemClickListener.onRecycleViewItemClick(v, item);
        }
    }

    /**
     * Identifying start of search query
     *
     * @param name name of the restaurant
     * @return starting position of a search string
     */
    private int indexOfSearchQuery(String name) {
        if (!TextUtils.isEmpty(searchTerm)) {
            return name.toLowerCase(Locale.getDefault()).indexOf(
                    searchTerm.toLowerCase(Locale.getDefault()));
        }
        return -1;
    }

    public void setOnItemClickListener(OnRecycleViewItemClickListener<Restaurant> listener) {
        this.itemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.thumbnail)
        ImageView imageView;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.type)
        TextView type;
        @BindView(R.id.status)
        TextView status;
        @BindView(R.id.rating)
        RatingBar ratingBar;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Filter restaurants by name, food and address
     *
     * @param text filter
     */
    public void filter(String text) {
        restaurantList.clear();
        this.searchTerm = text;
        if (text.isEmpty()) {
            restaurantList.addAll(restaurantListCopy);
        } else {
            text = text.toLowerCase();
            for (Restaurant item : restaurantListCopy) {
                if (item.getName().toLowerCase().contains(text)
                        || checkAddressMatch(item, text)
                        || checkFoodMatch(item, text)) {
                    restaurantList.add(item);
                }
            }
        }
        Collections.sort(restaurantList,
                new Comparator<Restaurant>() {
                    public int compare(Restaurant f1, Restaurant f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });
        notifyDataSetChanged();
    }

    /**
     * Checks if filter matches any food name
     * DEMO function (fake json)
     *
     * @param restaurant source
     * @param foodName   food filter
     * @return true if matches, false otherwise
     */
    private boolean checkFoodMatch(Restaurant restaurant, String foodName) {
        if (restaurant.getMenu() == null) { //if null, version is not demo
            return false;
        }
        for (Food food : restaurant.getMenu()) {
            if (food.getName().toLowerCase().contains(foodName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if filter matches address
     * DEMO function (fake json)
     *
     * @param restaurant source
     * @param address    filter
     * @return true if matches, false otherwise
     */
    private boolean checkAddressMatch(Restaurant restaurant, String address) {
        return restaurant.getAddress() != null //if null, version is not demo
                && restaurant.getAddress().toLowerCase().contains(address.toLowerCase());
    }
}
