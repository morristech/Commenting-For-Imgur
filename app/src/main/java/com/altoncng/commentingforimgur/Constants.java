package com.altoncng.commentingforimgur;

/**
 * Created by Eye on 9/14/2015.
 */
public class Constants {

    public static final boolean LOGGING = false;
    public static final String MY_IMGUR_CLIENT_ID = "YOUR IMGUR CLIENT KEY";
    public static final String MY_IMGUR_CLIENT_SECRET = "YOUR IMGUR SECRET KEY";
    //public static final int TOKEN_REFRESH_TIME = 2332800;   //27 days in seconds
    public static final int TOKEN_REFRESH_TIME = 600000;    //~7 days in seconds

    public static final int TYPE_HIDDEN_COMMENT = -1;
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_TEXT_IMAGE_TEXT = 1;
    public static final int TYPE_TEXT_VIDEO_TEXT = 2;
    public static final int TYPE_DESCRIPTION = 3;
    public static final int TYPE_COMMENT = 4;
    public static final int TYPE_FOCUSED_COMMENT = 5;
    public static final int TYPE_STATS = 6;

    public static String getClientAuth(){
        return "Client-ID " + MY_IMGUR_CLIENT_ID;
    }
}
