package com.altoncng.commentingforimgur.profile;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.altoncng.commentingforimgur.Constants;
import com.altoncng.commentingforimgur.ErrorMessage;
import com.altoncng.commentingforimgur.R;
import com.altoncng.commentingforimgur.RecyclerAdapter;
import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.CommonMethods;
import com.altoncng.commentingforimgur.utils.NetworkUtils;
import com.altoncng.commentingforimgur.utils.dbLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Eye on 1/28/2016.
 */
public class ProfileFavoriteFragment extends Fragment {

    ArrayList<Upload> profileFavoritesList;
    Context context;

    RecyclerView recyclerView;
    RecyclerAdapter rcAdapter;
    StaggeredGridLayoutManager staggeredGridManager;
    SwipeRefreshLayout mSwipeRefreshLayout;

    String username;
    int errCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = null;
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.gallery_recycler_activity, container, false);

        recyclerView = (RecyclerView) myInflatedView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) myInflatedView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                new getProfileFavorites().execute();
            }
        });

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);

        profileFavoritesList = new ArrayList<Upload>();

        new getProfileFavorites().execute();
        return myInflatedView;
    }

    public static ProfileFavoriteFragment newInstance(String text) {

        ProfileFavoriteFragment f = new ProfileFavoriteFragment();
        Bundle b = new Bundle();
        b.putString("username", text);

        f.setArguments(b);

        return f;
    }

    public class getProfileFavorites extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getFavorites();
        }

        @Override
        protected void onPostExecute(String result) {
            parsePostJSON(result);
        }
    }

    public String getFavorites() {
        /*sort 	    optional 	    'best', 'worst', 'oldest', or 'newest'. Defaults to 'newest'.
          page 	    optional 	    Page number (50 items per page). Defaults to 0.*/
        String start = "https://api.imgur.com/3/account/" + username + "/gallery_favorites";

        if (!NetworkUtils.isConnected(getActivity())) {
            return null;
        }

        try {
            URL url = new URL(start);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

            if (conn.getResponseCode() != 200) {
                errCode = conn.getResponseCode();
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment getFavorites : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
                        + errorString);
                return "";
            }
            String toReturn = "";
            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                toReturn = CommonMethods.readAll(br);

                br.close();
            }
            conn.disconnect();
            return toReturn;

        } catch (MalformedURLException e) {
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment IOException");
            e.printStackTrace();
        }
        return null;
    }

    public void parsePostJSON(String data) {
        JSONObject obj = null;

        if (profileFavoritesList == null || profileFavoritesList.isEmpty())
            profileFavoritesList = new ArrayList<Upload>();
        else if (profileFavoritesList.size() > 1) {
            profileFavoritesList.clear();
            profileFavoritesList = new ArrayList<Upload>();
        }

        if (data == null) {
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment parseJSON : data is null");
            return;
        }
        try {
            obj = new JSONObject(data);
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment ProfileFavorites parseJSON : JSON is: " + obj.toString());
            JSONArray ary = obj.getJSONArray("data");
            errCode = Integer.parseInt(obj.getString("status"));

            int size = ary.length();
            for (int i = 0; i < size; i++) {
                Upload upload = new Upload();
                JSONObject item = ary.getJSONObject(i);
                upload.isAlbum = item.getString("is_album").equals("true");
                if (upload.isAlbum) {
                    upload.coverId = "http://i.imgur.com/" + item.getString("cover") + "m.png";
                    upload.albumId = item.getString("id");
                    upload.imageNum = item.getString("images_count");
                } else {
                    upload.id = item.getString("id");
                    upload.coverId = "http://i.imgur.com/" + item.getString("id") + "m.png";
                    upload.size = Integer.parseInt(item.getString("size"));
                    upload.animated = item.getString("animated");
                    upload.width = Integer.parseInt(item.getString("width"));
                    upload.height = Integer.parseInt(item.getString("height"));
                    if (upload.width * 2.5 > upload.height) {
                        upload.hThumbnailLink = "http://i.imgur.com/" + item.getString("id") + "h.jpg";
                        upload.coverId = "http://i.imgur.com/" + item.getString("id") + "h.jpg";
                    } else {
                        upload.hThumbnailLink = "http://i.imgur.com/" + item.getString("id") + ".jpg";
                    }
                }
                upload.account_url = item.getString("account_url");
                upload.title = item.getString("title");
                upload.description = item.getString("description");

                //if its a really big solo iamge
                //if(upload.size > 1000000 && !upload.isAlbum && ary.getJSONObject(i).getString("animated").equals("false")) {
                if (!upload.isAlbum && item.getString("animated").equals("false")) {
                    String str = item.getString("link");
                    int index = str.lastIndexOf(".");
                    //upload.albumLink = str.substring(0, index) + "h" + str.substring(index);
                    upload.albumLink = str.substring(0, index) + str.substring(index);
                } else if (upload.animated != null && upload.animated.equals("true"))//solo animated image
                    upload.albumLink = item.getString("mp4");
                else
                    upload.albumLink = item.getString("link");  //if its an album
                upload.ups = Integer.parseInt(item.getString("ups"));
                upload.downs = Integer.parseInt(item.getString("downs"));
                upload.views = Integer.parseInt(item.getString("views"));
                upload.vote = item.getString("vote");
                upload.favorite = item.getString("favorite");
                profileFavoritesList.add(upload);
            }
        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment getProfileGallery parseJSON JSONException e");
            e.printStackTrace();
        }
        if (errCode != 200) {
            Toast.makeText(getActivity(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
            dbLog.w("imgurLog", "imgurLog ProfileFavoriteFragment getProfileGallery toast : " + errCode + " " + ErrorMessage.returnErrorMessage(errCode));
        }

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);

        rcAdapter = new RecyclerAdapter(context, profileFavoritesList);
        recyclerView.setAdapter(rcAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
