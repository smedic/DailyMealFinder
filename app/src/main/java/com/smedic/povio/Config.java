package com.smedic.povio;

/**
 * Created by smedic on 17.2.17..
 */

public class Config {

    public static final String API_KEY = "AIzaSyDHsjP0q0r2zMk-AzM6_4KFmn6Qa7nG8f8";
    public static final String BASE_URL = "https://maps.googleapis.com";
    public static final String RELATIVE_URL = "/maps/api/place/nearbysearch/json";
    public static final String types = "food";
    public static final int radius = 500;
    public static final String PHOTOS_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=300&photoreference=%s&sensor=false&key=%s";

    public static final String DEMO_BASE_URL = "https://api.myjson.com";
    public static final String DEMO_RELATIVE_URL_MENU = "/bins/14sxq9";
    public static final String DEMO_RELATIVE_URL = "/bins/f5r5t";
    //public static final boolean DEMO = true;

    // New Belgrade coordinates
    public static final double currentLatitude = 44.8051635;
    public static final double currentLongitude = 20.4072252;

}
