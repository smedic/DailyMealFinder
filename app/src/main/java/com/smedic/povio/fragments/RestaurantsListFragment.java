package com.smedic.povio.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.smedic.povio.MainActivity;
import com.smedic.povio.R;
import com.smedic.povio.RestaurantsAdapter;
import com.smedic.povio.interfaces.OnRecycleViewItemClickListener;
import com.smedic.povio.interfaces.WorkflowCallbacks;
import com.smedic.povio.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Stevan Medic on 17.2.17..
 */

public class RestaurantsListFragment extends Fragment implements OnRecycleViewItemClickListener<Restaurant>,
        MainActivity.OnRestaurantsListActionListener {

    private Context context;
    private WorkflowCallbacks workflowCallbacks;
    private RestaurantsAdapter adapter;
    private List<Restaurant> restaurantsList;

    @BindView(R.id.restaurants_recycler_view)
    RecyclerView recyclerView;

    /**
     * Preferred way for creating fragment instance
     *
     * @param restaurantsList list to display
     * @return fragment instance
     */
    public static RestaurantsListFragment newInstance(List<Restaurant> restaurantsList) {
        RestaurantsListFragment fragment = new RestaurantsListFragment();
        fragment.restaurantsList = new ArrayList<>();
        fragment.restaurantsList.addAll(restaurantsList);
        return fragment;
    }

    /**
     * Sets list from activity
     *
     * @param restaurantsList list
     */
    public void setList(List<Restaurant> restaurantsList) {
        this.restaurantsList.clear();
        this.restaurantsList.addAll(restaurantsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        if (restaurantsList == null) {
            restaurantsList = new ArrayList<>();
        }

        if (adapter == null) {
            adapter = new RestaurantsAdapter(context, restaurantsList);
            adapter.setOnItemClickListener(RestaurantsListFragment.this);
        }
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            workflowCallbacks = (MainActivity) context;
            this.context = context;
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            workflowCallbacks = (MainActivity) context;
            this.context = context;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (context != null) {
            ((MainActivity) context).setListCallback(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (context != null) {
            ((MainActivity) context).setListCallback(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        workflowCallbacks = null;
        context = null;
    }

    @Override
    public void onRecycleViewItemClick(View view, Restaurant restaurant) {
        workflowCallbacks.onRestaurantSelected(restaurant.getId());
    }


    /**
     * Filter list by using search view
     *
     * @param filter string to filter
     */
    @Override
    public void filter(String filter) {
        if (adapter != null) {
            adapter.filter(filter);
        }
    }

    /**
     * Updates list on location change
     *
     * @param restaurantsList restaurants
     */
    @Override
    public void updateList(List<Restaurant> restaurantsList) {
        if (this.restaurantsList != null) { //TODO check
            this.restaurantsList.clear();
            this.restaurantsList.addAll(restaurantsList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateLocation(LatLng location) {
        //do nothing for now
    }
}
