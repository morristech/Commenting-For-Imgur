package com.altoncng.commentingforimgur;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.altoncng.commentingforimgur.imgurmodel.Album;
import com.altoncng.commentingforimgur.imgurmodel.Comment;
import com.altoncng.commentingforimgur.imgurmodel.CommentTree;
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
 * Fragment used as a page in the viewpager of GalleryViewerActivity,
 * to display a post and its comments from the imgur gallery.
 * Used with GalleryViewerRecyclerAdapter and GalleryViewerViewHolder
 */
public class GalleryFragment extends Fragment implements GalleryViewerRecyclerAdapter.commentRefreshInterface {

    private OnFragmentInteractionListener mListener;
    private Upload galleryPostUpload;
    private View inflatedView;

    int width;
    int height;
    int position;

    VideoView videoView;

    public boolean pageVisible = false;

    private StaggeredGridLayoutManager staggeredGridManager;
    RecyclerView recyclerView;
    GalleryViewerRecyclerAdapter rcAdapter;

    ArrayList<CommentTree> root;
    ArrayList<CommentTree> rootList;

    int totalCount;

    SharedPreferences sharedpreferences;

    int errCode;
    String postAuthor;
    String acctName;

    long epochTime;

    SwipeRefreshLayout mSwipeRefreshLayout;

    public static GalleryFragment newInstance(Upload upload, int position) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putParcelable("data", upload);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public GalleryFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public GalleryFragment(Upload upload, int position) {
        galleryPostUpload = upload;
        this.position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null)
        {
            galleryPostUpload = getArguments().getParcelable("data");
            position = getArguments().getInt("position");
            dbLog.w("imgurLog", "imgurLog GalleryFragment extras upload " + galleryPostUpload);
        }

        sharedpreferences = getActivity().getSharedPreferences("imgurRestApp", Context.MODE_PRIVATE);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x*19/20;
        height = size.y*19/20;

        postAuthor = galleryPostUpload.account_url;
        //acctName = ;

        if(galleryPostUpload.isAlbum){
            new getGalleryAlbum().execute();
        }else {
            Album album = new Album(4);
            album.imageIds[0] = null;
            album.title[0] = galleryPostUpload.title;
            album.description[0] = "" + galleryPostUpload.description;
            album.animated[0] = galleryPostUpload.animated.equals("true");
            album.hThumbnailLink[0] = galleryPostUpload.hThumbnailLink;
            album.link[0] = galleryPostUpload.albumLink;
            album.hThumbnailLink[1] = galleryPostUpload.hThumbnailLink;
            album.link[1] = galleryPostUpload.albumLink;
            album.layoutType[0] = 0;
            if(album.link[1].substring(album.link[1].length() - 1).equals("4"))
                album.layoutType[1] = Constants.TYPE_TEXT_VIDEO_TEXT;
            else
                album.layoutType[1] = Constants.TYPE_TEXT_IMAGE_TEXT;

            album.layoutType[2] = Constants.TYPE_DESCRIPTION;
            album.layoutType[3] = Constants.TYPE_STATS;

            galleryPostUpload.album = album;
            new getComments().execute();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible())
        {
            if (!isVisibleToUser)   // If we are becoming invisible, then...
            {
                if(videoView != null) {
                    videoView.pause();
                }
            }

            if (isVisibleToUser) // If we are becoming visible, then...
            {
                if(videoView != null) {
                    videoView.start();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.fragment_gallery_recycler, container, false);

        recyclerView = (RecyclerView)myInflatedView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        staggeredGridManager = new StaggeredGridLayoutManager(1, 1);
        //recyclerView.setLayoutManager(staggeredGridManager);

        LinearLayoutManager LLManager = new LinearLayoutManager(getActivity());
        /****************************************************************************************************************************/
        //LLManager.setStackFromEnd(true);  //loads bottom up, so while scrolling down, there would be image loading stutter,
                                            // but none while scrolling back up. Maybe switch whenever scrolling direction changes?
        /****************************************************************************************************************************/
        recyclerView.setLayoutManager(LLManager);
        inflatedView = myInflatedView;
        rcAdapter = new GalleryViewerRecyclerAdapter(getActivity(), galleryPostUpload, sharedpreferences, this);
        recyclerView.setAdapter(rcAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) myInflatedView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                new getComments().execute();
            }
        });

        return myInflatedView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(videoView != null)
            videoView.start();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void refreshComments() {
        new getComments().execute();
    }

    public interface OnFragmentInteractionListener {
        public void onSwipe(int position);
    }



    public class getGalleryAlbum extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            dbLog.w("imgurLog", "imgurLog GalleryFragment getGalleryAlbum");
            return getAlbum();
        }

        @Override
        protected void onPostExecute(String result){
            parsePostJSON(result);
        }
    }

    public String getAlbum(){
        String start = "https://api.imgur.com/3/gallery/album/" + galleryPostUpload.albumId;
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
                dbLog.w("imgurLog", "imgurLog getAlbum : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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

    //imgurLog getAlbum parseJSON : JSON is:
    // {"data":{"id":"u7HSf","title":"lil tumblr fumblr 2","description":null,"datetime":1445427604,"cover":"gdiefoQ","cover_width":484,"cover_height":596,"account_url":"brieflychiefly","account_id":16131828,"privacy":"public","layout":"blog","views":248522,"link":"http:\/\/imgur.com\/a\/u7HSf","ups":9446,"downs":189,"points":9257,"score":9265,"is_album":true,"vote":null,"favorite":false,"nsfw":false,"section":"tumblr",
    // "comment_count":479,"comment_preview":[{"id":495740662,"image_id":"u7HSf",
    // "comment":"Wtf the girls hair is actually in the twisty part of the towel hat? I'm 25 and did not know this for real. But it does make sense","author":"CaptainBrusin","author_id":5094183,"on_album":true,"album_cover":"gdiefoQ","ups":1558,"downs":27,"points":1531,"datetime":1445429568,"parent_id":0,"deleted":false,"vote":null,"platform":"android","children":[]},
    // {"id":495734616,"image_id":"u7HSf","comment":"That hipster with the brownies, he was holding them before they were cool.","author":"diehardlance","author_id":24730748,"on_album":true,"album_cover":"gdiefoQ","ups":470,"downs":8,"points":462,"datetime":1445428150,"parent_id":0,"deleted":false,"vote":null,"platform":"desktop","children":[]},
    // {"id":495733895,"image_id":"u7HSf","comment":"Man, not only have I not seen these, they're pretty much all good.  I wish I had more than one upvote for you OP.","author":"fredgiblet","author_id":13931529,"on_album":true,"album_cover":"gdiefoQ","ups":194,"downs":4,"points":190,"datetime":1445427934,"parent_id":0,"deleted":false,"vote":null,"platform":"desktop","children":[]},
    // {"id":495767750,"image_id":"u7HSf","comment":"The whole baby made me upvote, not just the leg, but the while thing","author":"laughingsohardifelloffmydinosaur","author_id":4067233,"on_album":true,"album_cover":"gdiefoQ","ups":121,"downs":2,"points":119,"datetime":1445434664,"parent_id":0,"deleted":false,"vote":null,"platform":"android","children":[]},{"id":495747971,"image_id":"u7HSf","comment"...

    public void parsePostJSON(String data){
        JSONObject obj = null;

        if(data == null) {
            return;
        }try {
            obj = new JSONObject(data);
            dbLog.w("imgurLog", "imgurLog getAlbum parseJSON : JSON is: " + obj.toString());
            JSONObject ary = obj.getJSONObject("data");
            errCode = Integer.parseInt(obj.getString("status"));

            int imagesCount = Integer.parseInt(ary.getString("images_count"));
            Album album = new Album(imagesCount + 3);//+1 for title header, +1 again for stats

            album.imageIds[0] = null;
            album.title[0] = ary.getString("title");
            album.description[0] = ary.getString("description");
            album.animated[0] = false;
            album.hThumbnailLink[0] = null;
            album.link[0] = null;
            album.layoutType[0] = 0;

            int j=0;
            for(int i=1; i<=imagesCount; i++) {
                album.imageIds[i] = ary.getJSONArray("images").getJSONObject(j).getString("id");
                album.title[i] = ary.getJSONArray("images").getJSONObject(j).getString("title");
                album.description[i] = ary.getJSONArray("images").getJSONObject(j).getString("description");
                album.animated[i] = ary.getJSONArray("images").getJSONObject(j).getString("animated").equals("true");
                String str = ary.getJSONArray("images").getJSONObject(j).getString("link");
                int edit = str.lastIndexOf(".");
                if(!album.animated[i]) {
                    if (Integer.parseInt(ary.getJSONArray("images").getJSONObject(j).getString("width")) * 2.5 < Integer.parseInt(ary.getJSONArray("images").getJSONObject(j).getString("height"))) {
                        album.hThumbnailLink[i] = str;//.substring(0, edit) + "h" + str.substring(edit);
                    }else
                        album.hThumbnailLink[i] = str;
                }else {
                    album.hThumbnailLink[i] = str.substring(0, edit) + "l" + str.substring(edit);
                }album.link[i] = str;

                if(album.link[i].substring(album.link[i].length() - 1).equals("4"))
                    album.layoutType[i] = Constants.TYPE_TEXT_VIDEO_TEXT;
                else
                    album.layoutType[i] = Constants.TYPE_TEXT_IMAGE_TEXT;
                j++;
            }album.layoutType[imagesCount+1] = Constants.TYPE_DESCRIPTION;
            album.layoutType[imagesCount+2] = Constants.TYPE_STATS;
            galleryPostUpload.album = album;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(errCode != 200)
            Toast.makeText(getActivity(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();

        /*GalleryViewerRecyclerAdapter rcAdapter = new GalleryViewerRecyclerAdapter(getActivity(), galleryPostUpload);
        recyclerView.setAdapter(rcAdapter);*/
        if(recyclerView!=null)
            rcAdapter.notifyDataSetChanged();
        //addViewsToGallery(galleryPostUpload.album);
        new getComments().execute();
    }

    public void allowGet(){
        pageVisible = true;
        if(galleryPostUpload.isAlbum)
            //new getGalleryAlbum().execute();
        ;
    }

    /*****************************************GET COMMENTS**************************************/
    //computing the comments is also done here to prevent stutter from having to do so much work while
    //loading everything in the GalleryViewerRecyclerAdapter, which causes major stutter while scrolling

    public class getComments extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            dbLog.w("imgurLog", "imgurLog GalleryFragment getGalleryComments");
            return getCommentJSON();
        }

        @Override
        protected void onPostExecute(String result){
            parseCommentJSON(result);
        }
    }

    public String getCommentJSON(){
        epochTime = System.currentTimeMillis()/1000;

        String start = "";
        if(galleryPostUpload.isAlbum)
            start = "https://api.imgur.com/3/gallery/album/" + galleryPostUpload.albumId + "/comments/best";
        else
            start = "https://api.imgur.com/3/gallery/image/" + galleryPostUpload.id + "/comments/best";

        if (!NetworkUtils.isConnected(getActivity())) {
            return null;
        }

        try {
            URL url = new URL(start);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);

            if (conn.getResponseCode() != 200) {
                String errorString = CommonMethods.getStringFromInputStream(conn.getErrorStream());
                errCode = conn.getResponseCode();
                dbLog.w("imgurLog", "imgurLog getCommentJSON : HTTP error code :  " + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n"
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

    //JSON is: {"data":[{"id":504212684,"image_id":"iSG6u","comment":"Still trying to figure out why the snake was flipping out in #8","author":"motherofpibbles","author_id":23495674,"on_album":true,"album_cover":"hGGa0xo","ups":531,"downs":0,"points":531,"datetime":1446486033,"parent_id":0,"deleted":false,"vote":null,"platform":"android",
    // "children":[{"id":504234772,"image_id":"iSG6u","comment":"it's been hit by a car, killing it and doing weird things to it's nervous system. You see it a lot when they get hit on the head","author":"ferninthewoods","author_id":2767192,"on_album":true,"album_cover":"hGGa0xo","ups":321,"downs":3,"points":318,"datetime":1446488264,"parent_id":504212684,"deleted":false,"vote":null,"platform":"desktop",
    // "children":[{"id":504241428,"image_id":"iSG6u","comment":"So I just saw a worry worm die? Now I'm sad.","author":"SpoonOfDoom","author_id":1732193,"on_album":true,"album_cover":"hGGa0xo","ups":154,"downs":3,"points":151,"datetime":1446488947,"parent_id":504234772,"deleted":false,"vote":null,"platform":"desktop","children":[{"id":504244680,"image_id":"iSG6u","comment":"yeah me too. too often i see...

    public void parseCommentJSON(String data){
        JSONObject obj = null;

        if(data == null) {
            return;
        }try {
            obj = new JSONObject(data);
            dbLog.w("imgurLog", "imgurLog parseCommentJSON parseJSON : JSON is: " + obj.toString());
            JSONArray ary = obj.getJSONArray("data");
            errCode = Integer.parseInt(obj.getString("status"));

            root = new ArrayList<CommentTree>();
            rootList = new ArrayList<CommentTree>();
            int level = -1;

            totalCount = 0;
            for(int i=0; i<ary.length(); i++) {
                root.add(recursiveCommentParse(ary.getJSONObject(i), level));
                root.get(i).data.layoutType = Constants.TYPE_COMMENT;
            }galleryPostUpload.comment_tree = root;
            galleryPostUpload.comment_tree_list = rootList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(recyclerView!=null)
            rcAdapter.notifyDataSetChanged();
        if(errCode != 200)
            Toast.makeText(getActivity(), ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public CommentTree recursiveCommentParse(JSONObject ary, int level){
        Comment mComment = new Comment();
        CommentTree tree;

        try {
            int recordedTime = ary.getInt("datetime");
            mComment.postTime = CommonMethods.getTimeSince(recordedTime);

            mComment.comment_id = ary.getString("id");
            mComment.comment = ary.getString("comment");
            mComment.author = ary.getString("author");
            if(mComment.author.equals(postAuthor)) {
                mComment.colorBG = 0xFF373737;
                mComment.isAuthor = true;
            }else {
                mComment.colorBG = 0xFF262626;
                mComment.isAuthor = false;
            }mComment.points = Integer.parseInt(ary.getString("points"));
            if(ary.getString("vote").equals("null"))
                mComment.status = 0;
            else if(ary.getString("vote").equals("upvote"))
                mComment.status = 1;
            else
                mComment.status = -1;
            level++;
            mComment.comment_level = level;
            mComment.layoutType = Constants.TYPE_HIDDEN_COMMENT;
            tree = new CommentTree(mComment);
            tree.absPosition = totalCount;
            totalCount++;

            rootList.add(tree);
            for(int i=0; i<ary.getJSONArray("children").length(); i++) {
                CommentTree commentTree = recursiveCommentParse(ary.getJSONArray("children").getJSONObject(i), level);
                commentTree.setParent(tree);
                tree.addChild(commentTree);
                tree.addToCount(commentTree.getCount());
                tree.child_positions.add(totalCount);
            }level--;
            return tree;
        }catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
