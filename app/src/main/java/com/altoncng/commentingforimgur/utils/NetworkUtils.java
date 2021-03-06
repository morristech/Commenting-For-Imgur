package com.altoncng.commentingforimgur.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Eye on 9/18/2015.
*/
public class NetworkUtils {
    public static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean isConnected(Context mContext) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }catch (Exception ex){
            dbLog.w(TAG, ex.getMessage());
        }
        return false;
    }

    /*public static boolean connectionReachable() {
        Socket socket = null;
        boolean reachable = false;
        try {
            socket = new Socket("google.com", 80);
            reachable = socket.isConnected();
        } catch (UnknownHostException e) {
            dbLog.w(TAG, "Error connecting to server");
            reachable = false;
        } catch (IOException e) {
            dbLog.w(TAG, "Error connecting to server");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    aLog.w(TAG, "Error closing connecting socket test");
                }
            }
        }
        dbLog.w(TAG, "Data connectivity change detected, ping test=" + String.valueOf(reachable));
        return reachable;
    }*/

}