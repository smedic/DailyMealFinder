package com.smedic.povio.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smedic.povio.MainActivity;
import com.smedic.povio.R;
import com.smedic.povio.interfaces.WorkflowCallbacks;
import com.smedic.povio.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by smedic on 18.2.17..
 */

public class RestaurantsMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback, MainActivity.OnRestaurantsListActionListener {

    private static final int ZOOM_LEVEL = 17;

    private Context context;
    private GoogleMap map;
    private WorkflowCallbacks workflowCallbacks;
    private List<Restaurant> restaurantsList;
    private LatLng currentLatLng;

    @BindView(R.id.map_view)
    MapView mapView;

    /**
     * Preferred way for creating fragment instance
     *
     * @param restaurantsList list to display
     * @param currentLatLng   current location
     * @return fragment instance
     */
    public static RestaurantsMapFragment newInstance(List<Restaurant> restaurantsList, LatLng currentLatLng) {
        RestaurantsMapFragment fragment = new RestaurantsMapFragment();
        fragment.currentLatLng = currentLatLng;
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

    /**
     * Sets location from activity
     *
     * @param currentLatLng current location
     */
    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            this.context = context;
            workflowCallbacks = (MainActivity) context;
        }
    }

    /**
     * onAttach for older devices
     *
     * @param context activity context
     */
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            workflowCallbacks = (MainActivity) context;
            this.context = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (context != null) {
            ((MainActivity) context).setMapCallback(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (context != null) {
            ((MainActivity) context).setMapCallback(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.getPosition().equals(currentLatLng)) {
            workflowCallbacks.onRestaurantSelected((String) marker.getTag());
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(context);
        updateMap();
    }


    private void updateMap() {
        if (map == null) {
            return;
        }

        map.clear(); //clear all markers

        if (restaurantsList != null) {
            for (Restaurant restaurant : restaurantsList) {
                double latitude = restaurant.getGeometry().getLocation().getLat();
                double longitude = restaurant.getGeometry().getLocation().getLng();
                LatLng location = new LatLng(latitude, longitude);
                map.setOnMarkerClickListener(RestaurantsMapFragment.this);
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(location).title(restaurant.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(false).visible(true));
                marker.setTag(restaurant.getId());
            }
        }
        if (currentLatLng != null) {
            map.addMarker(new MarkerOptions()
                    .position(currentLatLng).title(getString(R.string.me))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(false).visible(true)).showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM_LEVEL));
        }
    }

    @Override
    public void updateList(List<Restaurant> restaurantList) {
        if (this.restaurantsList != null) {
            this.restaurantsList.clear();
            this.restaurantsList.addAll(restaurantList);
        }
    }

    @Override
    public void filter(String filter) {
        //do nothing for now
    }

    @Override
    public void updateLocation(LatLng location) {
        currentLatLng = location;
        updateMap();
    }
}
