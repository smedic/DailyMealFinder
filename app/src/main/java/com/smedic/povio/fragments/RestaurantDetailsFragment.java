package com.smedic.povio.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smedic.povio.Config;
import com.smedic.povio.FoodAdapter;
import com.smedic.povio.MainActivity;
import com.smedic.povio.R;
import com.smedic.povio.model.Location;
import com.smedic.povio.model.Restaurant;
import com.smedic.povio.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.smedic.povio.Config.PHOTOS_URL;

/**
 * Created by Stevan Medic on 17.2.17..
 * Fragment that displays list of restaurants
 */

public class RestaurantDetailsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "RestaurantDetailsFrag";
    private static final String KEY_MAP_TAB_SELECTED = "MAP_SELECTED";

    private Context context;
    private Restaurant restaurant;
    private GoogleMap map;
    private boolean isMapShown = true; //default state

    @BindView(R.id.map_view)
    MapView mapView;

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.type)
    TextView type;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.open_closed)
    TextView openClosed;
    @BindView(R.id.photo)
    ImageView imageView;
    @BindView(R.id.rating)
    RatingBar ratingBar;

    @BindView(R.id.location_screen)
    RelativeLayout locationScreen;
    @BindView(R.id.menu_screen)
    FrameLayout menuScreen;
    @BindView(R.id.location_button)
    Button locationButton;
    @BindView(R.id.menu_button)
    Button menuButton;

    @BindView(R.id.food_menu)
    ListView foodMenu;
    @BindView(R.id.notification)
    TextView notification;

    @OnClick({R.id.location_button, R.id.menu_button})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_button:
                showMapTab();
                break;
            case R.id.menu_button:
                showMenuTab();
                break;
            default:
                break;
        }
    }

    /**
     * Preferred way for creating fragment instance
     *
     * @return fragment instance
     */
    public static RestaurantDetailsFragment newInstance() {
        return new RestaurantDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restaurant, null);
        ButterKnife.bind(this, rootView);

        restaurant = getArguments().getParcelable(MainActivity.KEY_RESTAURANT_PARCELABLE);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.onResume();

        if (savedInstanceState != null) { //read tab selected before screen orientation
            isMapShown = savedInstanceState.getBoolean(KEY_MAP_TAB_SELECTED);
            if (isMapShown) {
                showMapTab();
            } else {
                showMenuTab();
            }
        } else {
            locationButton.setSelected(true);
        }

        name.setText(restaurant.getName());
        type.setText(Utils.capitalizeWord(restaurant.getTypes().get(0))); //use 1st type (highest priority)
        address.setText(getAddress(restaurant.getGeometry().getLocation()));

        if (restaurant.getOpeningHours() != null) {
            openClosed.setVisibility(View.VISIBLE);
            if (restaurant.getOpeningHours().getOpenNow()) {
                openClosed.setText(R.string.open);
                openClosed.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.greenOpened, null));
            } else {
                openClosed.setText(R.string.closed);
                openClosed.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.redClosed, null));
            }
        } else {
            openClosed.setVisibility(View.GONE);
        }

        if (restaurant.getRating() != null) {
            ratingBar.setRating(restaurant.getRating());
        }

        if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {
            String photoUrl = String.format(PHOTOS_URL,
                    restaurant.getPhotos().get(0).getPhotoReference(), Config.API_KEY);
            Picasso.with(context).load(photoUrl)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }

        /////////////// demo food menu /////////////
        if (restaurant.getMenu() != null) {
            foodMenu.setAdapter(new FoodAdapter(context, restaurant.getMenu()));
        } else {
            notification.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void showMapTab() {
        locationButton.setSelected(true);
        menuButton.setSelected(false);
        locationScreen.setVisibility(View.VISIBLE);
        menuScreen.setVisibility(View.GONE);
        isMapShown = true;
    }

    private void showMenuTab() {
        locationButton.setSelected(false);
        menuButton.setSelected(true);
        locationScreen.setVisibility(View.GONE);
        menuScreen.setVisibility(View.VISIBLE);
        isMapShown = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            this.context = context;
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_MAP_TAB_SELECTED, isMapShown);
        super.onSaveInstanceState(outState);
    }

    private String getAddress(Location location) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(location.getLat(), location.getLng(), 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                Log.d(TAG, "getAddress: address str: " + addressStr);
                builder.append(addressStr);
                builder.append(" ");
            }
            return builder.toString(); //This is the complete address.

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity());
        double latitude = restaurant.getGeometry().getLocation().getLat();
        double longitude = restaurant.getGeometry().getLocation().getLng();
        LatLng location = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions()
                .position(location).title(restaurant.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(false).visible(true)).showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }
}
