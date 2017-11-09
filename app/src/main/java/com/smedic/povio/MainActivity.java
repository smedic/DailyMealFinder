package com.smedic.povio;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.smedic.povio.fragments.RestaurantDetailsFragment;
import com.smedic.povio.fragments.RestaurantsListFragment;
import com.smedic.povio.fragments.RestaurantsMapFragment;
import com.smedic.povio.interfaces.MapsApi;
import com.smedic.povio.interfaces.WorkflowCallbacks;
import com.smedic.povio.model.Restaurant;
import com.smedic.povio.model.Result;
import com.smedic.povio.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.smedic.povio.Config.BASE_URL;
import static com.smedic.povio.Config.DEMO_BASE_URL;
import static com.smedic.povio.Config.currentLatitude;
import static com.smedic.povio.Config.currentLongitude;
import static com.smedic.povio.Config.radius;
import static com.smedic.povio.Config.types;

public class MainActivity extends AppCompatActivity implements WorkflowCallbacks, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SMEDIC MainActivity";

    private static final int LIST_FRAGMENT = 0;
    private static final int MAP_FRAGMENT = 1;
    private static final int DETAILS_FRAGMENT = 2;

    public static final String KEY_RESTAURANT_PARCELABLE = "restaurant";

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";
    private static final String KEY_BASE_FRAGMENT = "base_fragment";
    private static final String KEY_SHOULD_SEARCH = "should_search";
    private static final String KEY_RESTAURANTS_LIST = "restaurants_list";
    private static final String KEY_RESTAURANT = "restaurant";
    private static final String KEY_LOCATION = "location";
    private static final String RESTAURANT_FRAGMENT_TAG = "RESTAURANT_CONTAINER";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private OnRestaurantsListActionListener listCallback;
    private OnRestaurantsListActionListener mapCallback;
    private Location currentLocation;
    private boolean isLocationPermissionGranted;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    // A request object to store parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;

    private SearchView searchView;
    private List<Restaurant> restaurantList;

    private RestaurantsListFragment restaurantsListFragment;
    private RestaurantsMapFragment restaurantsMapFragment;

    private int currentFragment = LIST_FRAGMENT;
    private int baseFragment = LIST_FRAGMENT;
    private Restaurant restaurant;
    private boolean shouldntSearch;

    private boolean isDemo;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        updateBackArrowButton();

        isDemo = isDemoPreference();
        Log.d(TAG, "onCreate: DEMO VERSION: " + isDemo);

        restaurantList = new ArrayList<>();

        // Retrieve location and from saved instance state.
        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            currentFragment = savedInstanceState.getInt(KEY_CURRENT_FRAGMENT);
            restaurantList = savedInstanceState.getParcelableArrayList(KEY_RESTAURANTS_LIST);
            restaurant = savedInstanceState.getParcelable(KEY_RESTAURANT);
            baseFragment = savedInstanceState.getInt(KEY_BASE_FRAGMENT);
            shouldntSearch = savedInstanceState.getBoolean(KEY_SHOULD_SEARCH);

            if (currentFragment == LIST_FRAGMENT) {
                showRestaurantsList();
            } else if (currentFragment == MAP_FRAGMENT) {
                showRestaurantsMap();
            } else if (currentFragment == DETAILS_FRAGMENT) {
                navigateToRestaurantDetailsFragment(restaurant);
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragment == LIST_FRAGMENT) {
                    showRestaurantsMap();
                } else if (currentFragment == MAP_FRAGMENT) {
                    showRestaurantsList();
                }
            }
        });

        buildGoogleApiClient();
        mGoogleApiClient.connect();


    }

    /**
     * Get the device location and nearby places when the activity is restored after a pause.
     * Add on back stack change listener
     */
    @Override
    public void onResume() {
        super.onResume();

        if (isDemo != isDemoPreference()) {
            Log.d(TAG, "onResume: option changed! reload...");
            finish();
            startActivity(getIntent());
        }

        progressBar.setVisibility(View.VISIBLE);

        if (mGoogleApiClient.isConnected() && isLocationPermissionGranted) {
            updateDeviceLocation();
        }

        if (!Utils.isLocationEnabled(this)) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.location_unavailable),
                    Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.settings),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            snackbar.show();
        }
        // Whenever the fragment back stack changes, we may need to update the
        // action bar toggle: only top level screens show the hamburger-like icon, inner
        // screens - either Activities or fragments - show the "Up" icon instead.
        getFragmentManager().addOnBackStackChangedListener(backStackChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(backStackChangedListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (currentLocation != null) {
            outState.putParcelable(KEY_LOCATION, currentLocation);
        }
        outState.putInt(KEY_CURRENT_FRAGMENT, currentFragment);
        outState.putParcelableArrayList(KEY_RESTAURANTS_LIST, (ArrayList<Restaurant>) restaurantList);
        outState.putParcelable(KEY_RESTAURANT, restaurant);
        outState.putBoolean(KEY_SHOULD_SEARCH, shouldntSearch);
        outState.putInt(KEY_BASE_FRAGMENT, baseFragment);
        super.onSaveInstanceState(outState);
    }

    /**
     * Handles changes in fragments back-stack
     */
    private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    updateBackArrowButton();
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!isRootFragment()) {
                        onBackPressed();
                        return true;
                    }
                    if (listCallback != null) {
                        listCallback.filter(newText);
                    }
                    return true;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, AppPreferenceActivity.class));
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Otherwise, it may return to the previous fragment stack
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            if (baseFragment == LIST_FRAGMENT) {
                showRestaurantsList();
            } else {
                showRestaurantsMap();
            }
        } else {
            // Lastly, it will rely on the system behavior for back
            super.onBackPressed();
        }
        updateBackArrowButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true;
                updateDeviceLocation();
                if (isLocationPermissionGranted && Utils.isLocationEnabled(this)) {
                    searchRestaurants();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                isLocationPermissionGranted = false;

                Snackbar.make(coordinatorLayout, getString(R.string.location_permission), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void setListCallback(OnRestaurantsListActionListener listener) {
        this.listCallback = listener;
    }

    public void setMapCallback(OnRestaurantsListActionListener listener) {
        this.mapCallback = listener;
    }

    /**
     * Checks if number of backstack items is 0
     *
     * @return true if backstack is empty, false otherwise
     */
    private boolean isRootFragment() {
        return getFragmentManager().getBackStackEntryCount() == 0;
    }

    /**
     * Updates upper left navigation arrow icon
     */
    protected void updateBackArrowButton() {
        boolean isRoot = isRootFragment();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }

        if (isRoot) { //hide floating button when not in root
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    //////////////////////////////////////////////////
    ///////////// Fragments manipulation /////////////
    //////////////////////////////////////////////////

    /**
     * Navigates to restaurants list fragment and sets floating button and search view state
     */
    public void showRestaurantsList() {

        if (restaurantsListFragment == null) {
            restaurantsListFragment = RestaurantsListFragment.newInstance(restaurantList);
        } else {
            restaurantsListFragment.setList(restaurantList);
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, restaurantsListFragment);
        transaction.commit();

        currentFragment = LIST_FRAGMENT;
        baseFragment = LIST_FRAGMENT;
        fab.setImageResource(R.drawable.ic_maps_icon);
        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Navigates to restaurants map fragment and sets floating button and search view state
     */
    private void showRestaurantsMap() {

        if (restaurantsMapFragment == null) {
            restaurantsMapFragment = RestaurantsMapFragment.newInstance(restaurantList, getCurrentLatLng());
        } else {
            restaurantsMapFragment.setList(restaurantList);
            restaurantsMapFragment.setCurrentLatLng(getCurrentLatLng());
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, restaurantsMapFragment);
        transaction.commit();

        //set some UI elements
        currentFragment = MAP_FRAGMENT;
        baseFragment = MAP_FRAGMENT;
        fab.setImageResource(R.drawable.ic_list_icon);
        if (searchView != null) {
            searchView.setVisibility(View.GONE);
        }
    }

    /**
     * Navigates to restaurant details fragment
     *
     * @param restaurant info to be presented inside fragment
     */
    private void navigateToRestaurantDetailsFragment(Restaurant restaurant) {

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_RESTAURANT_PARCELABLE, restaurant);

        RestaurantDetailsFragment fragment = getRestaurantDetailsFragment();
        if (fragment == null) {
            fragment = RestaurantDetailsFragment.newInstance();
            fragment.setArguments(bundle);
        }

        fragment.getArguments().putAll(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                R.animator.slide_in_from_left, R.animator.slide_out_to_right);
        transaction.replace(R.id.fragment_container, fragment, RESTAURANT_FRAGMENT_TAG);
        // If this is not the top level media (root), we add it to the fragment back stack,
        // so that actionbar toggle and Back will work appropriately:
        if (getFragmentManager().findFragmentByTag(RESTAURANT_FRAGMENT_TAG) == null) {
            transaction.addToBackStack(RESTAURANT_FRAGMENT_TAG);
        }
        transaction.commit();

        currentFragment = DETAILS_FRAGMENT;
        if (searchView != null) {
            searchView.setVisibility(View.GONE);
        }
        this.restaurant = restaurant;
    }

    /**
     * Gets restaurant details fragment if present in back-stack
     *
     * @return fragment if present
     */
    private RestaurantDetailsFragment getRestaurantDetailsFragment() {
        return (RestaurantDetailsFragment) getFragmentManager().findFragmentByTag(RESTAURANT_FRAGMENT_TAG);
    }


    //////////////////////////////////////////////////
    /////////// Google API and maps logic ////////////
    //////////////////////////////////////////////////

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Gets the current location of the device and starts the location update notifications.
     */
    private void updateDeviceLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        // Get the best and most recent location of the device
        if (isLocationPermissionGranted) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    /**
     * Gets the device's current location and builds the map
     * when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        updateDeviceLocation();
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }


    @Override
    public void onLocationChanged(Location location) {
        if (mapCallback != null) {
            //call getCurrentLatLng to decide demo or not
            mapCallback.updateLocation(getCurrentLatLng());
        }
        searchRestaurants();
    }

    //////////////////////////////////////////////////////

    /**
     * Callback from restaurant list and restaurant map fragments
     *
     * @param restaurantId id
     */
    @Override
    public void onRestaurantSelected(String restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        navigateToRestaurantDetailsFragment(restaurant);
    }

    /**
     * Finds restaurant by id
     *
     * @param id to search for
     * @return restaurant if found
     */
    private Restaurant findRestaurantById(String id) {
        for (Restaurant restaurant : restaurantList) {
            if (restaurant.getId().equals(id)) {
                return restaurant;
            }
        }
        return null;
    }

    /**
     * Gets latitude and longitude for current location or
     * Gets lotitude and longitude for predefined location (demo)
     *
     * @return latitude and longitude
     */
    private LatLng getCurrentLatLng() {
        if (isDemo) {
            return new LatLng(currentLatitude, currentLongitude);
        }
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    /**
     * Search for nearby restaurants
     */
    public void searchRestaurants() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseURL())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String latLnd = getCurrentLatLng().latitude + "," + getCurrentLatLng().longitude;

        MapsApi service = retrofit.create(MapsApi.class);

        if (isDemo) {
            service.getDemoRestaurants().enqueue(result);
        } else {
            service.getRestaurants(types, latLnd, radius, Config.API_KEY).enqueue(result);
        }
    }

    /**
     * Retrofit result received for sent GET request
     */
    private Callback<Result> result = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
            progressBar.setVisibility(View.GONE);
            restaurantList.clear();
            restaurantList.addAll(response.body().getResults());

            if (!restaurantList.isEmpty()) {
                if (listCallback != null) {
                    listCallback.updateList(restaurantList);
                }

                if (mapCallback != null) {
                    mapCallback.updateList(restaurantList);
                }

                if (!shouldntSearch) { //initial setup
                    showRestaurantsList();
                    shouldntSearch = true;
                }
            } else {
                Snackbar.make(coordinatorLayout, R.string.no_videos_found, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            Log.e(TAG, "onFailure: ", t);
            progressBar.setVisibility(View.GONE);
        }
    };

    /**
     * Gets base URL
     *
     * @return demo url or real url acquired by location service
     */
    private String getBaseURL() {
        if (isDemo) {
            return DEMO_BASE_URL;
        }
        return BASE_URL;
    }

    private boolean isDemoPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getBoolean("demoMode", true);
    }

    /**
     * Interface for sending updates to list fragment
     */
    public interface OnRestaurantsListActionListener {
        void filter(String filter);

        void updateList(List<Restaurant> restaurantList);

        void updateLocation(LatLng location);
    }

}
