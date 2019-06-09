package com.example.myfeed;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

    public static boolean isConnected(Activity activity) {
        boolean isConnected = false;
        ConnectivityManager connectivityMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
        if(networkInfo.getState().equals(NetworkInfo.State.CONNECTED)){
            return true;
        }
        else{
            return false;
        }
    }
}
