package com.altoncng.commentingforimgur;

/**
 * Created by Eye on 12/10/2015.
 */
public class ErrorMessage {

    private ErrorMessage(){}

    public static String returnErrorMessage(int val){
        String errMsg = "";
        switch(val){
            case 400:   errMsg = "Error 400: There appears to be a missing or invalid parameter, or an uploaded image is invalid or corrupt";
                        break;
            case 401:   errMsg = "Error 401: Invalid OAuth credentials. Try to re-login and enter pin again";
                        break;
            case 403:   errMsg = "Error 403: Imgur may have updated its API or API credits may have run out";
                        break;
            case 404:   errMsg = "Error 404: This is not the post you are looking for. This post does not exist";
                        break;
            case 429:   errMsg = "Error 429: You have hit either the rate limiting on the application or on the user's IP address";
                        break;
            case 500:   errMsg = "Error 500: Unexpected internal error with imgur. Imgur is having problems :(";
                        break;
            case 0:     errMsg = "Could not contact imgur";
                        break;
            case -1:    errMsg = "JSON error occurred";
                        break;
            case -2:    errMsg = "Access Token failure. Try logging in again?";
                        break;
            default:    errMsg = "Unknown error occurred with imgur";
                        break;
        }return errMsg;
    }
}
