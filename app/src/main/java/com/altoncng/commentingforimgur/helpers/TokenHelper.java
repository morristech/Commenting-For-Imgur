package com.altoncng.commentingforimgur.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.altoncng.commentingforimgur.Constants;
import com.altoncng.commentingforimgur.imgurmodel.ImgurAPI;
import com.altoncng.commentingforimgur.utils.NetworkUtils;
import com.altoncng.commentingforimgur.utils.dbLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit.RestAdapter;

/**
 * Created by Eye on 10/2/2015.
 */
public class TokenHelper {

    public String accessToken;
    public String refreshToken;
    public String accountUsername;
    public int timeSince;

    private SharedPreferences sharedpreferences;
    public TokenHelperInterface signal = null;
    private Context context;

    private int connErrorCode;

    public TokenHelper(SharedPreferences sharedpreferences, TokenHelperInterface signal, Context context){
        this.sharedpreferences = sharedpreferences;
        this.signal = signal;
        this.context = context;

        accessToken = sharedpreferences.getString("access_token", "");
        refreshToken = sharedpreferences.getString("refresh_token", "");
        accountUsername = sharedpreferences.getString("account_username", null);
        timeSince = sharedpreferences.getInt("time", 0);

    }

    public boolean getTokens(String data){
        JSONObject obj = null;
        if(data == null || data.equals("")) {
            dbLog.w("imgurLog", "imgurLog getTokens accessToken data == null");
            return false;
        }try {
            obj = new JSONObject(data);
            accessToken = obj.getString("access_token");
            refreshToken = obj.getString("refresh_token");
            accountUsername = obj.getString("account_username");
            storeTokens();
            dbLog.w("imgurLog", "imgurLog getTokens try after storetokens");
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            dbLog.w("imgurLog", "imgurLog getTokens exception");
            return false;
        }
    }

    public void storeTokens(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (accessToken != null) {
            editor.putString("access_token", accessToken);
            editor.putString("refresh_token", refreshToken);
            editor.putString("account_username", accountUsername);
            int time = (int) System.currentTimeMillis()/1000;
            editor.putInt("time", time);

            editor.commit();
            timeSince = time;
        }
    }

    public class getAuthorization extends AsyncTask<String, Void, String> {

        String errorStr = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String auth = "https://api.imgur.com/oauth2/token";
            String toReturn = "";
            JSONObject jsonParam = new JSONObject();

            if (!NetworkUtils.isConnected(context)) {
                return null;
            }

            try {
                jsonParam.put("client_id", Constants.MY_IMGUR_CLIENT_ID);
                jsonParam.put("client_secret", Constants.MY_IMGUR_CLIENT_SECRET);
                jsonParam.put("grant_type", "pin");
                jsonParam.put("pin", params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(auth);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization:", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                connErrorCode = conn.getResponseCode();
                if (conn.getResponseCode() != 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getErrorStream())));
                    errorStr = readAll(br);
                    return null;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                toReturn = readAll(br);

                br.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if("null".equals(result) || result == null){
                signal.tokensReceived(errorStr, false, connErrorCode);
            }
            if(getTokens(result))
                signal.tokensReceived(accountUsername, true, connErrorCode);
        }
    }

    public class getRefreshToken extends AsyncTask<String, Void, String> {

        private int task;

        public getRefreshToken(int task){
            super();
            this.task = task;
        }

        @Override
        protected String doInBackground(String... params) {
            String toReturn = "";
            JSONObject jsonParam = new JSONObject();

            if (!NetworkUtils.isConnected(context)) {
                return null;
            }

            try {
                jsonParam.put("refresh_token", refreshToken);
                jsonParam.put("client_id", Constants.MY_IMGUR_CLIENT_ID);
                jsonParam.put("client_secret", Constants.MY_IMGUR_CLIENT_SECRET);
                jsonParam.put("grant_type", "refresh_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL("https://api.imgur.com/oauth2/token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization:", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                connErrorCode = conn.getResponseCode();
                if (conn.getResponseCode() != 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getErrorStream())));
                    String errorStr = readAll(br);
                    return null;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                toReturn = readAll(br);

                br.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (getTokens(result)) {
                signal.processFinish(accessToken, task, connErrorCode);
            }
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public boolean isSignedIn(){
        return (accessToken != null && !accessToken.isEmpty());
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurAPI.server)
                .build();
        return imgurAdapter;
    }
}
