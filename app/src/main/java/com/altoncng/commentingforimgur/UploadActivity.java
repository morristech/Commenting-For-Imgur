package com.altoncng.commentingforimgur;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.altoncng.commentingforimgur.helpers.DocumentHelper;
import com.altoncng.commentingforimgur.helpers.TokenHelper;
import com.altoncng.commentingforimgur.helpers.TokenHelperInterface;
import com.altoncng.commentingforimgur.imgurmodel.Album;
import com.altoncng.commentingforimgur.imgurmodel.ImageResponse;
import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.CommonMethods;
import com.altoncng.commentingforimgur.utils.dbLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*
Originally MainActivity, now renamed UploadActivity
Activity doing the uploading of images to imgur, either anonymously or logged in
*/
public class UploadActivity extends AppCompatActivity implements TokenHelperInterface {

    private static int RESULT_LOAD_IMAGE = 1;

    TextView loginTextView;

    EditText imgurLinkEditText;
    ImageView imgurImageView;
    Button postButton;
    Button galleryPostButton;

    EditText titleEditText;
    EditText descriptionEditText;
    boolean albumCheck;

    String pin;

    Bitmap bitmap;
    boolean galleryPost;

    File finalFile = null;

    Upload upload;
    Album album;

    String titleString;

    SharedPreferences sharedpreferences;

    Button albumButton;
    Button editAlbumButton;

    String chosenAlbum;

    TokenHelper tokenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences("imgurRestApp", Context.MODE_PRIVATE);
        tokenHelper = new TokenHelper(sharedpreferences, this, this);

        loginTextView = (TextView) findViewById(R.id.loginTextView);
        if(tokenHelper.accountUsername != null){
            loginTextView.setText("Logged in as " + tokenHelper.accountUsername);
            loginTextView.setTextColor(Color.parseColor("#85BF25"));
        }

        imgurImageView = (ImageView) findViewById(R.id.imgurImageView);

        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);

        imgurImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        postButton = (Button) findViewById(R.id.postButton);

        postButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //new createPost().execute();
                //addTokenAndUpload(finalFile, 1); //not anonymous
                //new UploadService(UploadActivity.this).Execute(upload, new UiCallback());
                if(finalFile == null){
                    Toast.makeText(getApplicationContext(), "Tap the image above to choose something to upload first", Toast.LENGTH_LONG).show();
                }else
                    anonymousUpload(finalFile, 1);
            }
        });

        albumButton  = (Button) findViewById(R.id.albumButton);
        albumButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(finalFile == null){
                    Toast.makeText(getApplicationContext(), "Tap the image above to choose something to upload first", Toast.LENGTH_LONG).show();
                }else
                    addTokenAndUpload(finalFile, 4);
            }
        });

        editAlbumButton  = (Button) findViewById(R.id.editAlbumButton);
        editAlbumButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(tokenHelper.isSignedIn()) {
                    Intent intent = new Intent(UploadActivity.this, AlbumActivity.class);
                    intent.putExtra("token", tokenHelper.accessToken);
                    intent.putExtra("account_name", tokenHelper.accountUsername);
                    startActivityForResult(intent, 100);
                }else
                    Toast.makeText(getApplicationContext(), "Please log in first", Toast.LENGTH_LONG).show();
            }
        });

        galleryPostButton = (Button) findViewById(R.id.galleryPostButton);

        galleryPostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                galleryPost = true;
                titleString = titleEditText.getText().toString();
                dbLog.w("imgurLog", "imgurLog UploadActivity galleryPostButton");
                if((titleEditText.getText().toString()).equals("") || !(titleEditText.getText().toString()).matches(".*\\w.*") ) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(UploadActivity.this);

                    final EditText edittextTitle= new EditText(UploadActivity.this);
                    alert.setMessage("To publish to gallery, you need a title. Leave blank for default blank title");
                    alert.setTitle("Enter Your Title");
                    alert.setView(edittextTitle);

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            titleString = edittextTitle.getText().toString();
                            if (!titleString.matches(".*\\w.*"))
                                titleString = "untest";//gives blank title
                            addTokenAndUpload(finalFile, 2);
                        }
                    });
                    alert.show();
                }else
                    addTokenAndUpload(finalFile, 2);
            }
        });

        upload = new Upload();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(data == null){
                Toast.makeText(getApplicationContext(), "No album chosen. Did you not have any?", Toast.LENGTH_SHORT).show();
                return;
            }
            chosenAlbum = data.getStringExtra("album_id");
            dbLog.w("imgurLog", "imgurLog UploadActivity chosenAlbum album_id: ." + chosenAlbum);
            if(addToken(finalFile, 3) == false) {//only reach here if you chose an album to add images to
                //immediately do, otherwise if its true, its in processFinish
                addTokenAndUpload(finalFile, 3);
            }
        }else {
            Uri returnUri;
            if (requestCode != RESULT_LOAD_IMAGE) {
                dbLog.w("imgurLog", "imgurLog UploadActivity onActivityResult requestCode : " + requestCode);
                return;
            }

            if (resultCode != RESULT_OK) {
                dbLog.w("imgurLog", "imgurLog UploadActivity onActivityResult resultCode : " + resultCode);
                return;
            }

            returnUri = data.getData();
            String filePath = DocumentHelper.getPath(this, returnUri);
            //Safety check to prevent null pointer exception
            if (filePath == null || filePath.isEmpty()) return;
            finalFile = new File(filePath);

            imgurImageView.setImageURI(returnUri);
            scaleImage();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.loginMenuButton:
                AlertDialog.Builder alert = new AlertDialog.Builder(UploadActivity.this);

                final EditText loginET= new EditText(UploadActivity.this);
                alert.setMessage("Enter or copy and paste your pin below to log in with this app");
                alert.setTitle("Login");
                alert.setView(loginET);
                loginET.setTextIsSelectable(true);
                loginET.setGravity(Gravity.CENTER_HORIZONTAL);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        pin = loginET.getText().toString();
                        tokenHelper.new getAuthorization().execute(pin);
                    }
                });

                final AlertDialog dialog = alert.create();

                loginET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });

                dialog.show();

                CommonMethods.getAcctPermission(UploadActivity.this);

                return true;
            case R.id.galleryMenuButton:
                Intent intent = new Intent(UploadActivity.this, GalleryActivity.class);
                intent.putExtra("token", tokenHelper.accessToken);
                intent.putExtra("account_name", tokenHelper.accountUsername);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //if a new token had to be generated...
    @Override
    public void processFinish(String output, int task, int errCode) {
        upload.token = output;
        new UploadService(UploadActivity.this).Execute(upload, new UiCallback());
    }

    @Override
    public void tokensReceived(String username, boolean success, int errCode) {
        if(errCode == 200){
            loginTextView.setText("Logged in as " + username);
            loginTextView.setTextColor(Color.parseColor("#85BF25"));
        }else
            Toast.makeText(getApplicationContext(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
    }

    public class DownloadText extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getImage();
        }

        @Override
        protected void onPostExecute(String result){
            parseJSON(result);
        }
    }

    public String getImage(){
        String start = "https://api.imgur.com/3/gallery/";
        albumCheck = false;
        try {
            if(isAlbum(imgurLinkEditText.getText().toString())) {
                start += "album/";
                albumCheck = true;
            }else
                start += "image/";

            URL url = new URL(start + imgurLinkEditText.getText().toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

            if (conn.getResponseCode() != 200) {
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                dbLog.w("imgurLog", "imgurLog UploadActivity getImage : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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
        } catch (JSONException e) {
            e.printStackTrace();
        }return null;
    }

    public boolean isAlbum(String url) throws IOException, JSONException {
        String toCheck = "https://imgur.com/gallery/" + url + ".json";
        JSONObject json = readJsonFromUrl(toCheck);
        try {
            String myStr = json.getJSONObject("data").getJSONObject("image").getString("is_album");
            dbLog.w("imgurLog", "imgurLog UploadActivity isAlbum : " + myStr);
            return (myStr.equals("false")) ? false : true;
        } catch (JSONException e) {
            e.printStackTrace();
        }return true;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = CommonMethods.readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public void parseJSON(String data){
        JSONObject obj = null;
        String myStr;
        String myImage;
        if(data == null) {
            return;
        }try {
            obj = new JSONObject(data);
            myStr = obj.getJSONObject("data").getString("title");
            Toast.makeText(getApplicationContext(), "title is: " + myStr, Toast.LENGTH_LONG).show();

            if(!albumCheck)
                myImage = obj.getJSONObject("data").getString("link");
            else
                myImage = obj.getJSONObject("data").getJSONArray("images").getJSONObject(0).getString("link");
            new loadimage().execute(myImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class loadimage extends AsyncTask<String, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(UploadActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("Loading Image...");
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
            imgurImageView.setImageBitmap(bitmap);
            return;
        }
    }

/*
Parameters for posting an image
Key 	        necessity   Description

image 	        required 	A binary file, base64 data, or a URL for an image. (up to 10MB)
album 	        optional 	The id of the album you want to add the image to. For anonymous albums, {album} should be the deletehash that is returned at creation.
type 	        optional 	The type of the file that's being sent; file, base64 or URL
name 	        optional 	The name of the file, this is automatically detected if uploading a file with a POST and multipart / form-data
title 	        optional 	The title of the image.
description 	optional 	The description of the image.
*/

    private void anonymousUpload(File image, int task) {
        createUpload(image, task);
        new UploadService(UploadActivity.this).ExecuteAnonymousPost(upload, new UiCallback());
    }

    private void addTokenAndUpload(File image, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        createUpload(image, task);

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            dbLog.w("imgurLog", "imgurLog UploadActivity createUpload : getting new token");
            tokenHelper.new getRefreshToken(0).execute();
        }else {
            upload.token = tokenHelper.accessToken;
            dbLog.w("imgurLog", "imgurLog UploadActivity createUpload : accessToken : " + upload.token + " " + tokenHelper.timeSince + " " + currTime);
            new UploadService(UploadActivity.this).Execute(upload, new UiCallback());
        }
    }

    private boolean addToken(File image, int task){
        int currTime = (int) System.currentTimeMillis()/1000;

        createUpload(image, task);

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            dbLog.w("imgurLog", "imgurLog UploadActivity createUpload : getting new token");
            tokenHelper.new getRefreshToken(task).execute();
            return true;
        }else {
            upload.token = tokenHelper.accessToken;
            dbLog.w("imgurLog", "imgurLog UploadActivity addToken : accessToken : " + upload.token + " " + tokenHelper.timeSince + " " + currTime);
            return false;
        }
    }

    private void createUpload(File image, int task){
        upload = new Upload();
        upload.image = image;
        upload.task = task;

        if (galleryPost){
            upload.toGallery = true;
            galleryPost = false;
        }else
            upload.toGallery = false;

        upload.title = titleEditText.getText().toString();
        if(upload.toGallery)
            upload.title = titleString;
        upload.description = descriptionEditText.getText().toString();

    }

    private void createAlbum(int task){
        album = new Album();
        album.task = task;

        album.token = tokenHelper.accessToken;
        String title = titleEditText.getText().toString();
        if (!title.matches(".*\\w.*"))
            title = "untest";
        album.title[0] = title;
        if(album.toGallery)
            album.title[0] = titleString;
        album.description[0] = descriptionEditText.getText().toString();
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            if(upload.task == 1)
                return;
            if(upload.task == 2 && upload.id == null) {
                upload.topic = "";
                upload.mature = "0";
                upload.terms = "1";

                upload.id = imageResponse.data.id;
                dbLog.w("imgurLog", "imgurLog UploadActivity UiCallback upload.id : " + upload.id);
                new UploadService(UploadActivity.this).Execute(upload, new UiCallback());
            }if(upload.task == 3 && upload.id == null){
                //if uploading first, then create album object? execute
                //run add to album execute
                upload.id = imageResponse.data.id;

                createAlbum(3);
                album.createArray(upload.id);
                album.id = chosenAlbum;
                new UploadService(UploadActivity.this).ExecutePut(album, new UiCallback());
            }if(upload.task == 4 && upload.id == null){
                //create album object, add values, execute
                //1. upload images
                //2. create album
                //3. add images to album
                upload.id = imageResponse.data.id;
                createAlbum(4);
                album.createArray(upload.id);
                new UploadService(UploadActivity.this).ExecuteNewAlbum(album, new UiCallback());
            }

        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {

            }
        }
    }

    private void scaleImage()
    {
        // Get the ImageView and its bitmap
        ImageView view = (ImageView) findViewById(R.id.imgurImageView);
        Drawable drawing = view.getDrawable();
        if (drawing == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(250);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    private int dpToPx(int dp)
    {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
