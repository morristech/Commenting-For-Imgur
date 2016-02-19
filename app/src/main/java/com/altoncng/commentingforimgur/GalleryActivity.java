package com.altoncng.commentingforimgur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.CommonMethods;
import com.altoncng.commentingforimgur.utils.NetworkUtils;
import com.altoncng.commentingforimgur.utils.dbLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/*
* Current main screen of app. Displays the imgur front page gallery by default
* with further pages accessed in the menu. Because it uses a recyclerview,
* classes RecyclerAdapter and SolventViewHolders are used in this activity
 */
public class GalleryActivity extends AppCompatActivity {

    GridView gridview;

    String acctName;
    String token;

    Menu menu;

    String gallerySection = "hot";
    String gallerySort = "time";
    int galleryPage = 0;

    MenuItem section;
    MenuItem sort;
    MenuItem pageMenuButton;

    ArrayList<Upload> galleryPostList;

    private StaggeredGridLayoutManager staggeredGridManager;
    RecyclerView recyclerView;

    SwipeRefreshLayout mSwipeRefreshLayout;

    int errCode;

    boolean galleryPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_gallery);
        setContentView(R.layout.gallery_recycler_activity);

        Intent intent = getIntent();

        acctName = intent.getStringExtra("account_name");
        token = intent.getStringExtra("token");
        dbLog.w("imgurLog", "imgurLog GalleryActivity token : " + token);

        //drag down refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                new getGalleryTask().execute();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);

        mSwipeRefreshLayout.setRefreshing(true);
        new getGalleryTask().execute();

        String action = intent.getAction();
        String segments = null;

        //if opened by an imgur link
        if (Intent.ACTION_VIEW.equals(action)) {
            if(intent.getData().getPathSegments().size() > 1) {
                galleryPost = true;
                segments = intent.getData().getPathSegments().get(1);
                dbLog.w("imgurLog", "imgurLog imgur link galleryactivity if :::" + segments + ":::");
                new getSinglePostTask().execute(segments);
            }else if(intent.getData().getPathSegments().size() == 1) {
                if(!intent.getData().getPathSegments().get(0).equals("gallery")) {
                    galleryPost = false;
                    segments = intent.getData().getPathSegments().get(0);
                    dbLog.w("imgurLog", "imgurLog imgur link galleryactivity else :::" + segments + ":::");
                    new getSinglePostTask().execute(segments);
                }
            }
        }
    }

    public class getGalleryTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getGalleryList();
        }

        @Override
        protected void onPostExecute(String result){
            parseJSON(result);
        }
    }

    public String getGalleryList(){
        String start = "https://api.imgur.com/3/gallery/" + gallerySection + "/" + gallerySort + "/" + galleryPage;
        if (!NetworkUtils.isConnected(GalleryActivity.this)) {
            return null;
        }
        try {
            URL url = new URL(start);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

            if (conn.getResponseCode() != 200) {
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog getGalleryList : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }return null;
    }

    public void parseJSON(String data){
        JSONObject obj = null;
        if(galleryPostList == null || galleryPostList.isEmpty())
            galleryPostList = new ArrayList<Upload>();
        else if(galleryPostList.size() > 1){
            galleryPostList.clear();
            galleryPostList = new ArrayList<Upload>();
        }

        if(data == null) {
            dbLog.w("imgurLog", "imgurLog GalleryActivity parseJSON : data is null");
            errCode = 0;
            return;
        }try {
            obj = new JSONObject(data);
            JSONArray ary = obj.getJSONArray("data");
            errCode = Integer.parseInt(obj.getString("status"));
            dbLog.w("imgurLog", "imgurLog GalleryActivity parseJSON : JSON is: " + obj.toString());

            if(obj.getJSONArray("data").length() == 0){
                setResult(100, null);
                showDialog();
            }

            for(int i=0; i<obj.getJSONArray("data").length(); i++) {
                Upload upload = new Upload();
                upload.isAlbum = ary.getJSONObject(i).getString("is_album").equals("true");
                if(upload.isAlbum){
                    upload.coverId = "http://i.imgur.com/" + ary.getJSONObject(i).getString("cover") + "m.png";
                    upload.albumId = ary.getJSONObject(i).getString("id");
                    upload.imageNum = ary.getJSONObject(i).getString("images_count");
                }else {
                    upload.id = ary.getJSONObject(i).getString("id");
                    upload.coverId = "http://i.imgur.com/" + ary.getJSONObject(i).getString("id") + "m.png";
                    upload.size = Integer.parseInt(ary.getJSONObject(i).getString("size"));
                    upload.animated = ary.getJSONObject(i).getString("animated");
                    upload.width = Integer.parseInt(ary.getJSONObject(i).getString("width"));
                    upload.height = Integer.parseInt(ary.getJSONObject(i).getString("height"));
                    if(upload.width*2.5 > upload.height) {
                        upload.hThumbnailLink = "http://i.imgur.com/" + ary.getJSONObject(i).getString("id") + "h.jpg";
                        upload.coverId = "http://i.imgur.com/" + ary.getJSONObject(i).getString("id") + "h.jpg";
                    }else{
                        upload.hThumbnailLink = "http://i.imgur.com/" + ary.getJSONObject(i).getString("id") + ".jpg";
                    }
                }
                upload.account_url = ary.getJSONObject(i).getString("account_url");
                upload.title = ary.getJSONObject(i).getString("title");
                upload.description = ary.getJSONObject(i).getString("description");

                //if its a really big solo iamge
                //if(upload.size > 1000000 && !upload.isAlbum && ary.getJSONObject(i).getString("animated").equals("false")) {
                if(!upload.isAlbum && ary.getJSONObject(i).getString("animated").equals("false")) {
                    String str = ary.getJSONObject(i).getString("link");
                    int index = str.lastIndexOf(".");
                    //upload.albumLink = str.substring(0, index) + "h" + str.substring(index);
                    upload.albumLink = str.substring(0, index) + str.substring(index);
                }else if(upload.animated != null && upload.animated.equals("true"))//solo animated image
                    upload.albumLink = ary.getJSONObject(i).getString("mp4");
                else
                    upload.albumLink = ary.getJSONObject(i).getString("link");  //if its an album
                upload.ups = Integer.parseInt(ary.getJSONObject(i).getString("ups"));
                upload.downs = Integer.parseInt(ary.getJSONObject(i).getString("downs"));
                upload.views = Integer.parseInt(ary.getJSONObject(i).getString("views"));
                upload.vote = ary.getJSONObject(i).getString("vote");
                upload.favorite = ary.getJSONObject(i).getString("favorite");
                galleryPostList.add(upload);
            }

            /*for(int i=0; i<galleryPostList.size(); i++){
                dbLog.w("imgurLog", "imgurLog GalleryActivity full albumList: " + galleryPostList.get(i).toString());
            }*/

        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog GalleryActivity parseJSON JSONException e");
            errCode = 200;
            e.printStackTrace();
            showDialog();
        }

        if(errCode != 200)
            Toast.makeText(getApplicationContext(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);

        RecyclerAdapter rcAdapter = new RecyclerAdapter(GalleryActivity.this, galleryPostList);
        recyclerView.setAdapter(rcAdapter);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public class getSinglePostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getPostFromId(params[0]);
        }

        @Override
        protected void onPostExecute(String result){
            parseSinglePost(result);
        }
    }

    private String getPostFromId(String id){

        String start = "https://api.imgur.com/3/gallery/album/" + id;
        if (!NetworkUtils.isConnected(GalleryActivity.this)) {
            return null;
        }
        try {
            URL url = new URL(start);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

            if (conn.getResponseCode() != 200) {
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog IMAGE OR ALBUM -> IMAGE");
                conn.disconnect();

                url = new URL("https://api.imgur.com/3/gallery/image/" + id);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);
            }

            if(conn.getResponseCode() != 200){
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog IMAGE OR ALBUM -> IMAGE failed");
                conn.disconnect();
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

    public void parseSinglePost(String data){
        JSONObject obj = null;

        if(galleryPostList == null || galleryPostList.isEmpty())
            galleryPostList = new ArrayList<Upload>();

        if(data == null) {
            return;
        }try {
            obj = new JSONObject(data);
            //dbLog.w("imgurLog", "imgurLog getAlbum parseJSON : JSON is: " + obj.toString());
            JSONObject ary = obj.getJSONObject("data");

            Upload upload = new Upload();
            upload.isAlbum = ary.getString("is_album").equals("true");
            upload.commentable = galleryPost;
            if(upload.isAlbum){
                upload.coverId = "http://i.imgur.com/" + ary.getString("cover") + "m.png";
                upload.albumId = ary.getString("id");
                upload.imageNum = ary.getString("images_count");
            }else {
                upload.id = ary.getString("id");
                upload.coverId = "http://i.imgur.com/" + ary.getString("id") + "m.png";
                upload.size = Integer.parseInt(ary.getString("size"));
                upload.animated = ary.getString("animated");
                upload.width = Integer.parseInt(ary.getString("width"));
                upload.height = Integer.parseInt(ary.getString("height"));
                if(upload.width*2.5 > upload.height) {
                    upload.hThumbnailLink = "http://i.imgur.com/" + ary.getString("id") + "h.jpg";
                    upload.coverId = "http://i.imgur.com/" + ary.getString("id") + "h.jpg";
                }else{
                    upload.hThumbnailLink = "http://i.imgur.com/" + ary.getString("id") + ".jpg";
                }
            }
            upload.account_url = ary.getString("account_url");
            upload.title = ary.getString("title");
            upload.description = ary.getString("description");

            //if its a really big solo iamge
            //if(upload.size > 1000000 && !upload.isAlbum && ary.getJSONObject(i).getString("animated").equals("false")) {
            if(!upload.isAlbum && ary.getString("animated").equals("false")) {
                String str = ary.getString("link");
                int index = str.lastIndexOf(".");
                upload.albumLink = str.substring(0, index) + str.substring(index);
            }else if(upload.animated != null && upload.animated.equals("true"))//solo animated image
                upload.albumLink = ary.getString("mp4");
            else
                upload.albumLink = ary.getString("link");  //if its an album
            upload.ups = Integer.parseInt(ary.getString("ups"));
            upload.downs = Integer.parseInt(ary.getString("downs"));
            upload.views = Integer.parseInt(ary.getString("views"));
            upload.vote = ary.getString("vote");
            upload.favorite = ary.getString("favorite");
            galleryPostList.add(0, upload);
        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog GalleryActivity parseSinglePost JSONException e");
            e.printStackTrace();
            return;
        }

        Intent intent = new Intent(GalleryActivity.this, GalleryViewerActivity.class);
        intent.putExtra("gallery_data", galleryPostList);
        intent.putExtra("position", 0);
        startActivity(intent);
    }

    private void showDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(GalleryActivity.this);
        alert.setTitle("Error");
        alert.setMessage("Imgur did not respond :(");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        this.menu = menu;
        section = menu.findItem(R.id.sectionMenuButton);
        section.setTitle("Most Viral");
        sort = menu.findItem(R.id.sortMenuButton);
        sort.setTitle("Newest First");
        pageMenuButton = menu.findItem(R.id.pageMenuButton);
        pageMenuButton.setTitle("Front Page");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuHot:
                galleryPostList.clear();
                gallerySection = "hot";
                section = menu.findItem(R.id.sectionMenuButton);
                section.setTitle("Most Viral");
                new getGalleryTask().execute();
                return true;
            case R.id.menuUser:
                galleryPostList.clear();
                gallerySection = "user";
                section = menu.findItem(R.id.sectionMenuButton);
                section.setTitle("User Sub");
                new getGalleryTask().execute();
                return true;
            case R.id.menuViral:
                galleryPostList.clear();
                gallerySort = "viral";
                sort = menu.findItem(R.id.sortMenuButton);
                sort.setTitle("Popularity");
                new getGalleryTask().execute();
                return true;
            case R.id.menuTopSort:
                galleryPostList.clear();
                gallerySort = "top";
                sort = menu.findItem(R.id.sortMenuButton);
                sort.setTitle("Highest Scoring");
                new getGalleryTask().execute();
                return true;
            case R.id.menuTime:
                galleryPostList.clear();
                gallerySort = "time";
                sort = menu.findItem(R.id.sortMenuButton);
                sort.setTitle("Newest First");
                new getGalleryTask().execute();
                return true;
            case R.id.page0:
                galleryPostList.clear();
                galleryPage = 0;
                pageMenuButton.setTitle("Front Page");
                new getGalleryTask().execute();
                return true;
            case R.id.page1:
                galleryPostList.clear();
                galleryPage = 1;
                pageMenuButton.setTitle("Page 1");
                new getGalleryTask().execute();
                return true;
            case R.id.page2:
                galleryPostList.clear();
                galleryPage = 2;
                pageMenuButton.setTitle("Page 2");
                new getGalleryTask().execute();
                return true;
            case R.id.page3:
                galleryPostList.clear();
                galleryPage = 3;
                pageMenuButton.setTitle("Page 3");
                new getGalleryTask().execute();
                return true;
            case R.id.page4:
                galleryPostList.clear();
                galleryPage = 4;
                pageMenuButton.setTitle("Page 4");
                new getGalleryTask().execute();
                return true;
            case R.id.page5:
                galleryPostList.clear();
                galleryPage = 5;
                pageMenuButton.setTitle("Page 5");
                new getGalleryTask().execute();
                return true;
            case R.id.page6:
                galleryPostList.clear();
                galleryPage = 6;
                pageMenuButton.setTitle("Page 6");
                new getGalleryTask().execute();
                return true;
            case R.id.page7:
                galleryPostList.clear();
                galleryPage = 7;
                pageMenuButton.setTitle("Page 7");
                new getGalleryTask().execute();
                return true;
            case R.id.page8:
                galleryPostList.clear();
                galleryPage = 8;
                pageMenuButton.setTitle("Page 8");
                new getGalleryTask().execute();
                return true;
            case R.id.page9:
                galleryPostList.clear();
                galleryPage = 9;
                pageMenuButton.setTitle("Page 9");
                new getGalleryTask().execute();
                return true;
            case R.id.upload_menu_button:
                Intent intent = new Intent(GalleryActivity.this, UploadActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
