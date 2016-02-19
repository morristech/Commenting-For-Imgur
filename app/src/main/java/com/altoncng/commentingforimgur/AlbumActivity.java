package com.altoncng.commentingforimgur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.CommonMethods;
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

//activity for displaying and selecting an album to upload to with UploadActivity
public class AlbumActivity extends AppCompatActivity {

    String acctName;
    String token;

    ArrayList<Upload> albumList;

    StaggeredGridLayoutManager staggeredGridManager;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();

        acctName = intent.getStringExtra("account_name");
        token = intent.getStringExtra("token");
        dbLog.w("imgurLog", "imgurLog AlbumActivity token : " + token);

        if(acctName == null)
            showAlertDialog();
        else
            new getAlbumTask().execute();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);
    }

    public class getAlbumTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getAlbumList();
        }

        @Override
        protected void onPostExecute(String result){
            parseJSON(result);
        }
    }

    public String getAlbumList(){
        //String start = "https://api.imgur.com/3/account/" + acctName + "/albums/ids/";
        //String start = "https://api.imgur.com/3/account/me/albums/ids/"; //for ids
        String start = "https://api.imgur.com/3/account/me/albums/";    //for albums themselves
        if(acctName == null) {
            Toast.makeText(getApplicationContext(), "Something went wrong, try logging in again :(", Toast.LENGTH_LONG).show();
            finish();
        }

        try {
            URL url = new URL(start);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            if (conn.getResponseCode() != 200) {
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog getAlbumList : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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

    public void showAlertDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(AlbumActivity.this);

        final EditText edittext= new EditText(AlbumActivity.this);
        alert.setMessage("You need to either enter an account pin or specify a username");
        alert.setTitle("No Username found");

        alert.setView(edittext);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                acctName = edittext.getText().toString();
                dbLog.w("imgurLog", "imgurLog AlbumActivity edittext.getText().toString() : ." + edittext.getText().toString() + ". string");
                new getAlbumTask().execute();
            }
        });
        alert.show();
    }

    //JSON is: {"data":["7M1Sy","hk4SL","mS2yo","5KQAQ","T13OM"],"success":true,"status":200}

    //{"data":[
    // {"id":"7M1Sy","title":null,"description":null,"datetime":1443437723,"cover":"QMeB6ue","cover_width":525,"cover_height":295,"account_url":"umaruBot","account_id":24364907,"privacy":"secret","layout":"blog","views":0,"link":"http:\/\/imgur.com\/a\/7M1Sy","favorite":false,"nsfw":null,"section":null,"deletehash":"HOCWeqZzocONqdG","images_count":6,"order":0},
    // {"id":"hk4SL","title":null,"description":null,"datetime":1443437710,"cover":"ybbiL0f","cover_width":730,"cover_height":1190,"account_url":"umaruBot","account_id":24364907,"privacy":"hidden","layout":"blog","views":0,"link":"http:\/\/imgur.com\/a\/hk4SL","favorite":false,"nsfw":null,"section":null,"deletehash":"QMqg95aBBfKLcim","images_count":1,"order":0},
    // {"id":"mS2yo","title":null,"description":"asdf","datetime":1443437690,"cover":"oBewwpC","cover_width":525,"cover_height":295,"account_url":"umaruBot","account_id":24364907,"privacy":"public","layout":"blog","views":1,"link":"http:\/\/imgur.com\/a\/mS2yo","favorite":false,"nsfw":null,"section":null,"deletehash":"tm1H0HYgpbSChIq","images_count":7,"order":0},
    // {"id":"5KQAQ","title":null,"description":null,"datetime":1443437675,"cover":"cXhOWyf","cover_width":600,"cover_height":450,"account_url":"umaruBot","account_id":24364907,"privacy":"public","layout":"blog","views":1,"link":"http:\/\/imgur.com\/a\/5KQAQ","favorite":false,"nsfw":null,"section":null,"deletehash":"XsUwhsLEFueJHGJ","images_count":2,"order":0},
    // {"id":"T13OM","title":"stuff","description":null,"datetime":1443224866,"cover":"XgUrf52","cover_width":128,"cover_height":128,"account_url":"umaruBot","account_id":24364907,"privacy":"hidden","layout":"blog","views":1,"link":"http:\/\/imgur.com\/a\/T13OM","favorite":false,"nsfw":null,"section":null,"deletehash":"MDqCbfwdsCpXYD8","images_count":2,"order":0}
    // ],"success":true,"status":200}
    //will not include album images unless specific album is specified!

    public void parseJSON(String data){
        JSONObject obj = null;
        albumList = new ArrayList<Upload>();

        if(data == null || data.isEmpty()) {
            dbLog.w("imgurLog", "imgurLog AlbumActivity parseJSON : data is null");
            setResult(100, null);
            finish();
        }try {
            obj = new JSONObject(data);
            JSONArray ary = obj.getJSONArray("data");
            dbLog.w("imgurLog", "imgurLog AlbumActivity parseJSON : JSON is: " + obj.toString());

            if(obj.getJSONArray("data").length() == 0){
                setResult(100, null);
                finish();
            }

            if(obj.getJSONArray("data").length() == 0){
                Toast.makeText(AlbumActivity.this, "No albums found. Are you sure you have any? :(", Toast.LENGTH_LONG).show();
            }

            for(int i=0; i<obj.getJSONArray("data").length(); i++) {
                Upload upload = new Upload();

                upload.coverId = "http://i.imgur.com/" + ary.getJSONObject(i).getString("cover") + "m.png";
                upload.title = ary.getJSONObject(i).getString("title");
                upload.albumLink = ary.getJSONObject(i).getString("link");
                upload.albumId = ary.getJSONObject(i).getString("id");
                upload.imageNum = ary.getJSONObject(i).getString("images_count");
                albumList.add(upload);
                //dbLog.w("imgurLog", "imgurLog AlbumActivity albumList: " + albumList.get(i));
            }
        } catch (JSONException e) {
            dbLog.w("imgurLog", "imgurLog AlbumActivity parseJSON JSONException e");
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        staggeredGridManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridManager);

        AlbumAdapter rcAdapter = new AlbumAdapter(AlbumActivity.this, AlbumActivity.this, albumList);
        recyclerView.setAdapter(rcAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album, menu);
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
}
