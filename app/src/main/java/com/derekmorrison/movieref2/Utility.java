package com.derekmorrison.movieref2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by Derek on 11/19/2015.
 */
public class Utility {

    public static String GetSortOrder(Context context){
        // TODO track down why this occasionaly shows up as null
        if (context == null) return "1";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        // 1 = Most Popular, 0 = Highest Vote, 2 = Favorite
        return sharedPref.getString(context.getResources().getString(R.string.pref_sort_order), "1");
    }

    public static int safeLongToInt(long l) {
        // limit the value of the long to between MIN_VALUE and MAX_VALUE
        if (l < Integer.MIN_VALUE) { l = Integer.MIN_VALUE; }
        if (l > Integer.MAX_VALUE) { l = Integer.MAX_VALUE; }

        return (int) l;
    }

    /*
 *  This code was lifted from a couple of examples found on StackOverflow
 *  requires     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *  this method tests the flags available in the ConnectivityManager and returns 'true' if any data path is 'CONNECTED'
 *
 * TODO handle depricated methods
 *  getAllNetworkInfo()
 *  This method was deprecated in API level 23. This method does not support multiple connected
 *  networks of the same type. Use getAllNetworks() and getNetworkInfo(android.net.Network) instead.
 */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
