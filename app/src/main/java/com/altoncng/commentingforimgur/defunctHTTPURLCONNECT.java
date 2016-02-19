package com.altoncng.commentingforimgur;/*
package com.altoncng.commentingforimgur;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import com.altoncng.commentingforimgur.helpers.DocumentHelper;

public class defunctHTTPURLCONNECT extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;

    EditText imgurLinkEditText;
    EditText pinEditText;
    ImageView imgurImageView;
    Button submitButton;
    Button pinButton;
    Button exchangeButton;
    Button postButton;

    String pin;

    Bitmap bitmap;
    Bitmap chosenImage;
    boolean album;

    String accessToken;
    String refreshToken;

    File finalFile;
    File oldfinalFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgurImageView = (ImageView) findViewById(R.id.imgurImageView);
        //imgurLinkEditText = (EditText)findViewById(R.id.imgurLinkEditText);
        imgurLinkEditText.setText("qPrJm2x");// 4swX4N1 doesn't work 403 permission denied
        pinEditText = (EditText)findViewById(R.id.pinEditText);
        //submitButton = (Button) findViewById(R.id.submitButton);

        */
/*submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DownloadText().execute((String[]) null);
            }
        });*//*


        pinButton = (Button) findViewById(R.id.pinButton);

        pinButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                getAcctPermission();
            }
        });

        exchangeButton = (Button) findViewById(R.id.exchangeButton);

        exchangeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                pin = pinEditText.getText().toString();
                new getAuthorization().execute();
            }
        });

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
                new createPost().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.w("imgurLog", "imgurLog onActivityResult");
        Uri returnUri;

        if (requestCode != RESULT_LOAD_IMAGE) {
            Log.w("imgurLog", "imgurLog onActivityResult requestCode : " + requestCode);
            return;
        }

        if (resultCode != RESULT_OK) {
            Log.w("imgurLog", "imgurLog onActivityResult resultCode : " + resultCode);
            return;
        }

        returnUri = data.getData();

        String filePath = DocumentHelper.getPath(this, returnUri);
        //Safety check to prevent null pointer exception
        if (filePath == null || filePath.isEmpty()) return;
        finalFile = new File(filePath);

        imgurImageView.setImageURI(returnUri);
        chosenImage = ((BitmapDrawable)imgurImageView.getDrawable()).getBitmap();

        if(chosenImage == null)
            Log.w("imgurLog", "imgurLog onActivityResult chosenImage is null! " + "\n" + finalFile + "\n" + oldfinalFile);

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
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
        album = false;
        try {
            if(isAlbum(imgurLinkEditText.getText().toString())) {
                start += "album/";
                album = true;
            }else
                start += "image/";

            URL url = new URL(start + imgurLinkEditText.getText().toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization:", Constants.MY_IMGUR_CLIENT_ID);

            Log.w("imgurLog", "imgurLog getImage : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage());
            if (conn.getResponseCode() != 200) {
                //throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                //Toast.makeText(getApplicationContext(), "Failed : HTTP error code :  " + conn.getResponseCode(), Toast.LENGTH_LONG).show();
                return "";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String toReturn = readAll(br);

            br.close();
            conn.disconnect();

            return toReturn;

            */
/*StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            conn.disconnect();
            return sb.toString(); *//*
//JSON string?

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
            Log.w("imgurLog", "imgurLog isAlbum : " + myStr);
            return (myStr.equals("false")) ? false : true;
        } catch (JSONException e) {
            e.printStackTrace();
        }return true;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
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
        if(data.equals(""))
            return;
        try {
            obj = new JSONObject(data);
            myStr = obj.getJSONObject("data").getString("title");
            Toast.makeText(getApplicationContext(), "title is: " + myStr, Toast.LENGTH_LONG).show();

            if(!album)
                myImage = obj.getJSONObject("data").getString("link");
            else
                myImage = obj.getJSONObject("data").getJSONArray("images").getJSONObject(0).getString("link");
            new loadimage().execute(myImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class loadimage extends AsyncTask<String, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(defunctHTTPURLCONNECT.this);

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

    public void getAcctPermission(){
        String oAuthURL = "oauth2/authorize?client_id=" + Constants.MY_IMGUR_CLIENT_ID + "&response_type=" + "pin";
        openLink(oAuthURL);
        //get user to enter pin
    }

    public void openLink(String link){
        Uri uri = Uri.parse("https://api.imgur.com/" + link); // missing 'http://' will cause crash
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public class getAuthorization extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String auth = "https://api.imgur.com/oauth2/token";
            String toReturn = "";

            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("client_id", Constants.MY_IMGUR_CLIENT_ID);
                jsonParam.put("client_secret", Constants.MY_IMGUR_CLIENT_SECRET);
                jsonParam.put("grant_type", "pin");
                jsonParam.put("pin", pin);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(auth);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization:", "Client-ID 5488e69f8bc66b3");

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                Log.w("imgurLog", "imgurLog exchangePinForTokens : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage());
                if (conn.getResponseCode() != 200) {
                    //throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    //Toast.makeText(getApplicationContext(), "Failed : HTTP error code :  " + conn.getResponseCode(), Toast.LENGTH_LONG).show();
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
            }Log.w("imgurLog", "imgurLog exchange pin response is: \n" + toReturn.toString());
            return toReturn;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            getTokens(result);
        }
    }

    public void getTokens(String data){
        JSONObject obj = null;
        if(data.equals("") || data == null)
            return;
        try {
            obj = new JSONObject(data);
            accessToken = obj.getString("access_token");
            refreshToken = obj.getString("refresh_token");
            Toast.makeText(getApplicationContext(), "tokens are: " + accessToken + " " + refreshToken, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    */
/*
    Parameters for posting an image
    Key 	        necessity   Description

    image 	        required 	A binary file, base64 data, or a URL for an image. (up to 10MB)
    album 	        optional 	The id of the album you want to add the image to. For anonymous albums, {album} should be the deletehash that is returned at creation.
    type 	        optional 	The type of the file that's being sent; file, base64 or URL
    name 	        optional 	The name of the file, this is automatically detected if uploading a file with a POST and multipart / form-data
    title 	        optional 	The title of the image.
    description 	optional 	The description of the image.
    *//*

    public class createPost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String uploadURL = "https://api.imgur.com/3/upload";
            String toReturn = "";

            try {
                URL url = new URL(uploadURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                accessToken = "822b060f66db99a2010d6253ec5a7f481979fefb";//12:05
                conn.setRequestProperty("Authorization:", "Bearer " + accessToken);
                conn.setDoOutput(true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                chosenImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                String data = Base64.encode(baos.toByteArray(), Base64.DEFAULT).toString();
                Log.w("imgurLog", "imgurLog createPost encode base64" + data);

                String sdata = URLEncoder.encode("image", "UTF-8") + "="
                        + URLEncoder.encode("http://i.4cdn.org/a/1442552923277.png", "UTF-8");
                */
/*JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("image", "http://i.4cdn.org/a/1442552923277.png");
                    jsonParam.put("type", "url");
                } catch (JSONException e) {
                    Log.w("imgurLog", "imgurLog createPost jsonparam failed");
                    e.printStackTrace();
                }*//*

                Log.w("imgurLog", "imgurLog createPost : 1");
                OutputStream os = conn.getOutputStream();
                //os.write(jsonParam.toString().getBytes("UTF-8"));
                //os.write("Content-Disposition: form-data; name=\"image/png\"\nhttps://i.4cdn.org/a/1442552923277.png".getBytes("UTF-8"));
                //os.flush();
                //os.close();

                DataOutputStream wr = new DataOutputStream(os);
                wr.writeBytes("image=http://i.4cdn.org/a/1442552923277.png&type=url");
                wr.flush();
                wr.close();

                Log.w("imgurLog", "imgurLog createPost : 2");
                Log.w("imgurLog", "imgurLog createPost : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + " \n" + " " + conn.getErrorStream());
                Log.w("imgurLog", "imgurLog createPost : 3");

                String errorString = getStringFromInputStream(conn.getErrorStream());
                Log.w("imgurLog", "imgurLog createPost : errorString : " + errorString);

                if (conn.getResponseCode() != 200) {
                    Log.w("imgurLog", "imgurLog createPost : if (conn.getResponseCode() != 200)");
                    return null;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                toReturn = readAll(br);

                br.close();
                conn.disconnect();
                Log.w("imgurLog", "imgurLog createPost : end of try");
                */
/*.altoncng.commentingforimgur W/imgurLog﹕ imgurLog exchange pin response is:
                {"access_token":"16140f122a795d1cf7404f803024a7d64b3965ca","expires_in":3600,"token_type":"bearer","scope":null,"refresh_token":"4ec180ed5035839dc8f2e0125ef9e93c522e4a91","account_id":6982808,"account_username":"TheSunIsTooHot"}
*//*

            } catch (MalformedURLException e) {
                Log.w("imgurLog", "imgurLog createPost : MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.w("imgurLog", "imgurLog createPost : IOException");
                e.printStackTrace();
            }Log.w("imgurLog", "imgurLog createPost URL response is: \n" + toReturn.toString());
            return toReturn;
        }
    }

    private static String getStringFromInputStream(InputStream is) {

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
        }

        return sb.toString();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getFilePath(Context context, Uri uri)
    {
        int currentApiVersion;
        try
        {
            currentApiVersion = android.os.Build.VERSION.SDK_INT;
        }
        catch(NumberFormatException e)
        {
            //API 3 will crash if SDK_INT is called
            currentApiVersion = 3;
        }
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst())
            {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }
        else if (currentApiVersion <= Build.VERSION_CODES.HONEYCOMB_MR2 && currentApiVersion >= Build.VERSION_CODES.HONEYCOMB)

        {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();

            if (cursor != null)
            {
                int column_index =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
            }
            return result;
        }
        else
        {

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index
                    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }
}
*/
