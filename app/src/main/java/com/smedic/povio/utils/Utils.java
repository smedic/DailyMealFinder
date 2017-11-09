package com.smedic.povio.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by smedic on 19.2.17..
 * Helper class
 */

public class Utils {

    /**
     * Checks if location is enabled on a device
     *
     * @param context application context
     * @return true if enabled, false otherwise
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /**
     * Capitalizes word
     *
     * @param word string
     * @return capitalized word
     */
    public static String capitalizeWord(String word) {
        if (word != null) {
            String placeType = word.replace("_", " ");
            return placeType.substring(0, 1).toUpperCase() + placeType.substring(1);
        }
        return "";
    }

}
