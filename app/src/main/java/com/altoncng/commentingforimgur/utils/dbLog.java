package com.altoncng.commentingforimgur.utils;

import android.util.Log;

import com.altoncng.commentingforimgur.Constants;

public class dbLog {
    public static void w (String tag, String message){
        if(Constants.LOGGING) {
            if (tag != null && message != null)
                Log.w(tag, message);
        }
    }

    public static void d (String tag, String message){
        if(Constants.LOGGING) {
            if (tag != null && message != null)
                Log.d(tag, message);
        }
    }
}
