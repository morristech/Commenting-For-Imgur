package com.altoncng.commentingforimgur.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.altoncng.commentingforimgur.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class CommonMethods {

    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }return sb.toString();
    }

    public static String getTimeSince(int recorded){
        long epochTime = System.currentTimeMillis()/1000;
        int time = (int) epochTime - recorded;
        int num = 1;
        String toReturn = "";
        if(time < 60) {
            num = time;
            toReturn = "" + time + " second";
        }else if(time < 3600) {
            num = time/60;
            toReturn = "" + num + " minute";
        }else if(time < 86400){
            num = time/3600;
            toReturn = "" + num + " hour";
        }else if(time < 2678400){
            num = time/86400;
            toReturn = "" + num + " day";
        }else if(time < 31536000){
            num = time/2678400;
            toReturn = "" + num + " month";
        }else{
            num = time/31536000;
            toReturn = "" + num + " year";
        }if(num > 1)
            toReturn = toReturn + "s";
        toReturn = toReturn + " ago";

        return toReturn;
    }

    public static void getAcctPermission(Context context){
        String oAuthURL = "oauth2/authorize?client_id=" + Constants.MY_IMGUR_CLIENT_ID + "&response_type=" + "pin";
        openLink(context, oAuthURL);
        //get user to enter pin
    }

    public static void openLink(Context context, String link){
        Uri uri = Uri.parse("https://api.imgur.com/" + link); // missing 'http://' will cause crash
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}
