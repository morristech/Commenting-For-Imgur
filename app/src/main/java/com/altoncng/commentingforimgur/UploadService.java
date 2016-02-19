package com.altoncng.commentingforimgur;

import android.content.Context;
import android.os.Handler;

import com.altoncng.commentingforimgur.helpers.NotificationHelper;
import com.altoncng.commentingforimgur.helpers.TokenHelper;
import com.altoncng.commentingforimgur.imgurmodel.Album;
import com.altoncng.commentingforimgur.imgurmodel.ImageResponse;
import com.altoncng.commentingforimgur.imgurmodel.ImgurAPI;
import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.NetworkUtils;
import com.altoncng.commentingforimgur.utils.dbLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

// Uses retrofit, makes the post calls to imgur,
// also holds the callback for commentqueue replying to its own comments
public class UploadService {
    public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;

    public UploadService(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    private int commentPostedCount = 0;
    private Upload itemList;
    private ArrayList<String> commentAry;
    private GalleryViewerRecyclerAdapter.commentRefreshInterface cListener;
    TokenHelper tokenHelper;

    private int postingFail = 0;
    String postedCommentId;

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            dbLog.w("imgurLog", "imgurLog UploadService : networkUtils not connected");
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        if(upload.id == null){
            restAdapter.create(ImgurAPI.class).postImage(
                    //"Authorization: " + upload.token,
                    //Constants.getClientAuth(),
                    //"Bearer 8ae876e5a822eb4e773f2b7469b49d8e9889c8e9",
                    "Bearer " + upload.token,
                    upload.title,
                    upload.description,
                    upload.albumId,
                    null,
                    new TypedFile("image*//**//*", upload.image),
                    new Callback<ImageResponse>() {
                        @Override
                        public void success(ImageResponse imageResponse, Response response) {
                            dbLog.w("imgurLog", "imgurLog UploadService : success");
                            if (cb != null) cb.success(imageResponse, response);
                            if (response == null) {//successful communication, but bad response

                            // Notify image was NOT uploaded successfully

                                notificationHelper.createFailedUploadNotification();
                                dbLog.w("imgurLog", "imgurLog UploadService : success response == null");
                                return;
                            }

                            //Notify image was uploaded successfully

                            if (imageResponse.success) {
                                notificationHelper.createUploadedNotification(imageResponse);
                                dbLog.w("imgurLog", "imgurLog UploadService : success imageResponse.success");
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (cb != null) cb.failure(error);
                            dbLog.w("imgurLog", "imgurLog UploadService : failure " + error.getMessage() + " " +  error.getResponse());
                            notificationHelper.createFailedUploadNotification();
                        }
                    });
        }else{
            dbLog.w("imgurLog", "imgurLog UploadService : else gallery");
            restAdapter.create(ImgurAPI.class).postGallery(
                    upload.id,
                    "Bearer " + upload.token,
                    upload.title,
                    upload.topic,
                    upload.terms,
                    upload.mature,
                    new Callback<ImageResponse>() {
                        @Override
                        public void success(ImageResponse imageResponse, Response response) {
                            if (cb != null)
                                cb.success(imageResponse, response);
                            if (response == null) {
                                notificationHelper.createFailedUploadNotification();
                                dbLog.w("imgurLog", "imgurLog toGallery : success response == null");
                                return;
                            }
                            if (imageResponse.success) {
                                notificationHelper.createUploadedNotification(imageResponse);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (cb != null) cb.failure(error);
                            dbLog.w("imgurLog", "imgurLog toGallery : failure " + error.getMessage() + " getBody " + error.getBody());
                            String msg = null;

                            if(error.getResponse().getBody() != null) {
                                String json = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                                try {
                                    JSONObject obj = new JSONObject(json);
                                    msg = obj.getJSONObject("data").getString("error");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            notificationHelper.createFailedUploadNotification(msg);
                        }
                    });
        }
    }

    public void ExecuteAnonymousPost(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).postImage(
                Constants.getClientAuth(),
                upload.title,
                upload.description,
                upload.albumId,
                null,
                new TypedFile("image/*", upload.image),
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            /*
                             Notify image was NOT uploaded successfully
                            */
                            notificationHelper.createFailedUploadNotification();
                            return;
                        }
                        /*
                        Notify image was uploaded successfully
                        */
                        if (imageResponse.success) {
                            notificationHelper.createUploadedNotification(imageResponse);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                        notificationHelper.createFailedUploadNotification();
                    }
                });
    }

    //put
    public void ExecutePut(Album album, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            dbLog.w("imgurLog", "imgurLog UploadService : networkUtils not connected");
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).addToAlbum(
                album.id,
                "Bearer " + album.token,
                album.imageIds,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService put album : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            notificationHelper.createFailedUploadNotification();
                            dbLog.w("imgurLog", "imgurLog UploadService put album : success response == null");
                            return;
                        }
                    /*
                    Notify image was uploaded successfully
                    */
                        if (imageResponse.success) {
                            notificationHelper.createUploadedNotification(imageResponse);
                            dbLog.w("imgurLog", "imgurLog UploadService put album : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService put album : failure " + error.getMessage() + " " + error.getResponse());
                    }
                });

    }

    public void ExecuteNewAlbum(Album album, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            cb.failure(null);
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).createAlbum(
                "Bearer " + album.token,
                album.imageIds,
                album.title[0],
                album.description[0],
                album.privacy,
                album.layout,
                album.cover,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService new album : success");
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                        /*
                         Notify image was NOT uploaded successfully
                        */
                            notificationHelper.createFailedUploadNotification();
                            dbLog.w("imgurLog", "imgurLog UploadService new album : success response == null");
                            return;
                        }
                    /*
                    Notify image was uploaded successfully
                    */
                        if (imageResponse.success) {
                            notificationHelper.createUploadedNotification(imageResponse);
                            dbLog.w("imgurLog", "imgurLog UploadService new album : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                    }
                });

    }

    public void ExecuteVoteComment(String comment_id, String vote, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            cb.failure(null);
            return;
        }
        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).voteComment(
                comment_id,
                vote,
                "Bearer " + token,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            return;
                        }
                        if (imageResponse.success) {
                            //notificationHelper.createUploadedNotification(imageResponse);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                    }
                });

    }

    public void ExecuteVotePost(String comment_id, String vote, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }
        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).votePost(
                comment_id,
                vote,
                "Bearer " + token,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            return;
                        }

                        if (imageResponse.success) {
                            ;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteVotePost : failure " + error.getMessage() + " " + error.getResponse());
                    }
                });

    }

    public void ExecuteFavoriteImage(String postId, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            cb.failure(null);
            dbLog.w("imgurLog", "imgurLog UploadService : networkUtils not connected");
            return;
        }
        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).favoriteImage(
                postId,
                "Bearer " + token,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteImage : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            return;
                        }

                        if (imageResponse.success) {
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteImage : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteImage : failure " + error.getMessage() + " " + error.getResponse());
                    }
                });

    }

    public void ExecuteFavoriteAlbum(String postId, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).favoriteAlbum(
                postId,
                "Bearer " + token,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteAlbum : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteAlbum : success response == null");
                            return;
                        }
                        if (imageResponse.success) {
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteAlbum : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteFavoriteAlbum : failure " + error.getMessage() + " " + error.getResponse());
                    }
                });

    }

    //generally called one time, as the commentqueue will subsequently use replycomment in any chain
    public void ExecuteReplyGallery(GalleryViewerRecyclerAdapter.commentRefreshInterface cListener, TokenHelper helper, ArrayList<String> commentAry, Upload itemList,
                                    String id, String message, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = new UiCallback();
        this.cListener = cListener;
        tokenHelper = helper;
        this.commentAry = commentAry;
        this.itemList = itemList;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        /*final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();*/

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).replyGallery(
                "Bearer " + token,
                id,
                message,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyGallery : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            //notificationHelper.createFailedUploadNotification();
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyGallery : success response == null");
                            return;
                        }
                        if (imageResponse.success) {
                            //notificationHelper.createUploadedNotification(imageResponse);
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyGallery : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyGallery : failure " + error.getMessage() + " " + error.getResponse());
                        //notificationHelper.createFailedUploadNotification();
                    }
                });

    }

    public void ExecuteReplyComment(GalleryViewerRecyclerAdapter.commentRefreshInterface cListener, String comment_id, String id, String message, String token, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = new UiCallback();
        this.cListener = cListener;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }
        /*final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();*/

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).replyComment(
                comment_id,
                "Bearer " + token,
                id,
                message,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            //notificationHelper.createFailedUploadNotification();
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment : success response == null");
                            return;
                        }
                        if (imageResponse.success) {
                            //notificationHelper.createUploadedNotification(imageResponse);
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment : failure " + error.getMessage() + " " + error.getResponse());
                        //notificationHelper.createFailedUploadNotification();
                    }
                });

    }

    public void ExecuteReplyComment(GalleryViewerRecyclerAdapter.commentRefreshInterface cListener, TokenHelper helper, ArrayList<String> commentAry, Upload itemList,
                                    String comment_id, String id, String message, String token, Callback<ImageResponse> callback) {
        this.cListener = cListener;
        tokenHelper = helper;
        this.commentAry = commentAry;
        this.itemList = itemList;

        final Callback<ImageResponse> cb = new UiCallback();

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        /*final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();*/

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).replyComment(
                comment_id,
                "Bearer " + token,
                id,
                message,
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment2 : success");
                        if (cb != null)
                            cb.success(imageResponse, response);
                        if (response == null) {
                            //notificationHelper.createFailedUploadNotification();
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment2 : success response == null");
                            return;
                        }
                        if (imageResponse.success) {
                            //notificationHelper.createUploadedNotification(imageResponse);
                            dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment2 : success imageResponse.success");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null)
                            cb.failure(error);
                        dbLog.w("imgurLog", "imgurLog UploadService ExecuteReplyComment2 : failure " + error.getMessage() + " " + error.getResponse());
                        //notificationHelper.createFailedUploadNotification();
                    }
                });

    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurAPI.server)
                .build();

        if (Constants.LOGGING)
            imgurAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return imgurAdapter;
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            postingFail = 0;

            postedCommentId = imageResponse.data.id;
            commentPostedCount++;
            if(commentPostedCount < commentAry.size()){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ExecuteReplyComment(cListener, postedCommentId, itemList.id, commentAry.get(commentPostedCount), tokenHelper.accessToken, new UiCallback());
                    }
                }, 30000);
            }else {
                CommentQueue.continuePosting();
                cListener.refreshComments();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            postingFail++;
            if(error != null && postingFail < 3) {
                dbLog.w("imgurLog", "imgurLog UiCallback failure retrying : " + postingFail);
                //Toast.makeText(context, "Posting comment \"" + commentAry.get(commentPostedCount) + "\" failed, retrying!", Toast.LENGTH_LONG).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ExecuteReplyComment(cListener, postedCommentId, itemList.id, commentAry.get(commentPostedCount), tokenHelper.accessToken, new UiCallback());
                    }
                }, 30000);

            }else{
                CommentQueue.continuePosting();
                dbLog.w("imgurLog", "imgurLog UiCallback failure 3 times, terminating");
                //Toast.makeText(context, commentPostedCount + "/" + commentAry.size() + " with " + commentPostedCount + " comment overloading imgur rates", Toast.LENGTH_LONG).show();
            }
        }
    }
}
