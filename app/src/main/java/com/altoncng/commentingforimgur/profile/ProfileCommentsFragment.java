package com.altoncng.commentingforimgur.profile;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.altoncng.commentingforimgur.Constants;
import com.altoncng.commentingforimgur.ErrorMessage;
import com.altoncng.commentingforimgur.R;
import com.altoncng.commentingforimgur.imgurmodel.Album;
import com.altoncng.commentingforimgur.imgurmodel.Comment;
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
 * Created by Eye on 1/20/2016.
 */
public class ProfileCommentsFragment extends Fragment {

    ArrayList<Comment> commentList;
    ArrayList<String> postList;
    Context context;

    RecyclerView recyclerView;
    ProfileCommentsRecyclerAdapter pcRecyclerAdapter;

    String username;
    int errCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commentList = new ArrayList<>();
        postList = new ArrayList<>();

        username = null;
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.profile_comments_fragment, container, false);

        recyclerView = (RecyclerView)myInflatedView.findViewById(R.id.profileCommentsRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager LLManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(LLManager);

        commentList = new ArrayList<>();
        postList = new ArrayList<>();

        new getProfileComments().execute();
        return myInflatedView;
    }

    public static ProfileCommentsFragment newInstance(String text) {

        ProfileCommentsFragment f = new ProfileCommentsFragment();
        Bundle b = new Bundle();
        b.putString("username", text);

        f.setArguments(b);

        return f;
    }

    public class getProfileComments extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getComments();
        }

        @Override
        protected void onPostExecute(String result){
            parsePostJSON(result);
        }
    }

    public String getComments(){
        /*sort 	    optional 	    'best', 'worst', 'oldest', or 'newest'. Defaults to 'newest'.
          page 	    optional 	    Page number (50 items per page). Defaults to 0.*/
        String start = "https://api.imgur.com/3/account/" + username + "/comments/newest/0";
        //String start = "https://api.imgur.com/3/account/" + "umarubot" + "/comments/newest/0"; //test
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
                dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment getProfileComments : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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
            dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment IOException");
            e.printStackTrace();
        }return null;
    }

    public void parsePostJSON(String data){
        JSONObject obj = null;
        if(!commentList.isEmpty()) {
            commentList.clear();
            postList.clear();
        }

        if(data == null) {
            dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment getAlbum parseJSON : data is null");
            return;
        }try {
            obj = new JSONObject(data);
            errCode = Integer.parseInt(obj.getString("status"));
            JSONArray ary = obj.getJSONArray("data");

            int size = ary.length();
            for(int i=0; i<size; i++) {
                Comment cmt = new Comment();
                JSONObject item = ary.getJSONObject(i);
                cmt.comment = item.getString("comment");
                cmt.points = item.getInt("points");
                cmt.postTime = CommonMethods.getTimeSince(item.getInt("datetime"));
                if(item.getBoolean("on_album"))
                    postList.add(item.getString("album_cover"));
                else
                    postList.add(item.getString("image_id"));
                commentList.add(cmt);
            }
        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment getProfileComments parseJSON JSONException e");
            e.printStackTrace();
        }
        if(errCode != 200) {
            Toast.makeText(getActivity(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
            dbLog.w("imgurLog", "imgurLog ProfileCommentsFragment getProfileComments toast : " + errCode + " " + ErrorMessage.returnErrorMessage(errCode));
        }
        pcRecyclerAdapter = new ProfileCommentsRecyclerAdapter(context, username, commentList, postList);
        recyclerView.setAdapter(pcRecyclerAdapter);
        if(recyclerView!=null)
            pcRecyclerAdapter.notifyDataSetChanged();

    }
}
