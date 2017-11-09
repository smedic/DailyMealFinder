package com.smedic.povio.interfaces;

import com.smedic.povio.Config;
import com.smedic.povio.model.Result;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by smedic on 16.2.17..
 * Creating GET requests for nearby places list
 */

public interface MapsApi {

    // Real URL for current location (menu is unavailable)
    @GET(Config.RELATIVE_URL)
    Call<Result> getRestaurants(@Query("types") String types, @Query("location") String location,
                                @Query("radius") Integer radius, @Query("key") String key);

    // URL of fake location (used to demonstrate menu)
    @GET(Config.DEMO_RELATIVE_URL_MENU)
    Call<Result> getDemoRestaurants();

}
