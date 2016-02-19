package com.altoncng.commentingforimgur.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.altoncng.commentingforimgur.Constants;
import com.altoncng.commentingforimgur.ErrorMessage;
import com.altoncng.commentingforimgur.R;
import com.altoncng.commentingforimgur.RecyclerAdapter;
import com.altoncng.commentingforimgur.dynamicviewpager.CustomPager;
import com.altoncng.commentingforimgur.dynamicviewpager.MyPagerAdapter;
import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.messaging.MessagingActivity;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Eye on 1/18/2016.
 */
public class ProfileActivity extends Activity {

    ArrayList<Upload> galleryPostList;
    ViewPager viewpager;

    String username;

    TextView profile_name_textview;
    TextView profile_points_textview;
    TextView profile_time_textview;
    TextView profile_text_textview;

    ScrollView profile_text_scrollview;
    TextView profile_bio_button;

    private int errCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);

        Button button = (Button)findViewById(R.id.profile_messaging_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MessagingActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        galleryPostList = intent.getParcelableArrayListExtra("gallery_data");
        username = intent.getStringExtra("username");

        viewpager = (ViewPager) findViewById(R.id.pager);
        viewpager.setAdapter(new MyPagerAdapter(getFragmentManager()));

        profile_name_textview = (TextView) findViewById(R.id.profile_name_textview);
        profile_points_textview = (TextView) findViewById(R.id.profile_points_textview);
        profile_time_textview = (TextView) findViewById(R.id.profile_time_textview);
        profile_text_textview = (TextView) findViewById(R.id.profile_text_textview);

        profile_name_textview.setText(username);
        profile_bio_button = (TextView) findViewById(R.id.profile_bio_button);
        profile_bio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profile_text_scrollview.getVisibility() == View.GONE) {
                    profile_text_scrollview.setVisibility(View.VISIBLE);
                    profile_bio_button.setText("Hide Bio");
                }else{
                    profile_text_scrollview.setVisibility(View.GONE);
                    profile_bio_button.setText("Show Bio");
                }
            }
        });

        profile_text_scrollview = (ScrollView) findViewById(R.id.profile_text_scrollview);

        new AccessProfile().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_gallery_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private String tabtitles[] = new String[] { "Comments", "Submissions", "Favorites" };
        private int numTabs = 3;
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {
                //pass profile name?

                case 0: return ProfileCommentsFragment.newInstance(username);
                case 1: return ProfileGalleryFragment.newInstance(username);
                case 2: return ProfileFavoriteFragment.newInstance(username);
                default: return ProfileGalleryFragment.newInstance(username);
            }
        }

        @Override
        public int getCount() {
            return numTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabtitles[position];
        }
    }

    public class AccessProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getProfile();
        }

        @Override
        protected void onPostExecute(String result){
            parsePostJSON(result);
        }
    }

    public String getProfile(){
        String start = "https://api.imgur.com/3/account/" + username;

        if (!NetworkUtils.isConnected(ProfileActivity.this)) {
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }return null;
    }

    /*imgurLog ProfileActivity parseJSON : JSON is:
    {"data":{"id":30148101,"url":"blahblahblahblahhhblahhh","bio":null,"reputation":415,"created":1453582584,
    "pro_expiration":false},"success":true,"status":200}
*/
    public void parsePostJSON(String data){
        JSONObject obj = null;

        String date1 = "";
        String points = "";
        String bio = "";

        if(data == null) {
            dbLog.w("imgurLog", "imgurLog getAlbum parseJSON : data is null");
            return;
        }try {
            obj = new JSONObject(data);
            dbLog.w("imgurLog", "imgurLog ProfileActivity parseJSON : JSON is: " + obj.toString());
            errCode = Integer.parseInt(obj.getString("status"));

            JSONObject profileData = obj.getJSONObject("data");

            points = formatNum(profileData.getInt("reputation"));
            Date date = new Date((long)profileData.getInt("created")*1000);
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.US);
            date1 = format.format(date);
            bio = profileData.getString("bio");
            if(bio.equals("null"))
                bio = "";

        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog getProfileGallery parseJSON JSONException e");
            e.printStackTrace();
        }
        if(errCode != 200) {
            Toast.makeText(this, ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
            dbLog.w("imgurLog", "imgurLog getProfileGallery toast : " + errCode + " " + ErrorMessage.returnErrorMessage(errCode));
        }
        profile_text_textview.setText(bio);
        profile_points_textview.setText("Reputation: " + points);
        profile_time_textview.setText("Since " + date1);
    }

    public String formatNum(int num){
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(num);
    }

}
