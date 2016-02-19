package com.altoncng.commentingforimgur;

import android.content.Context;

import com.altoncng.commentingforimgur.helpers.TokenHelper;
import com.altoncng.commentingforimgur.imgurmodel.ImageResponse;
import com.altoncng.commentingforimgur.imgurmodel.Upload;

import java.util.ArrayList;

import retrofit.Callback;

/**
 * Created by Eye on 1/4/2016.
 * Class for storing comment data for the comment queue
 */
public class CommentItems {

    Context context;

    GalleryViewerRecyclerAdapter.commentRefreshInterface cListener;
    TokenHelper helper;
    ArrayList<String> commentAry;
    Upload itemList;
    String id;
    String message;
    String token;
    Callback<ImageResponse> callback;

    String comment_id;

    boolean galleryReply;

    public CommentItems(Context context, GalleryViewerRecyclerAdapter.commentRefreshInterface cListener, TokenHelper helper, ArrayList<String> commentAry, Upload itemList,
                        String id, String message, String token, Callback<ImageResponse> callback){
        this.context = context;

        this.cListener = cListener;
        this.helper = helper;
        this.commentAry = commentAry;
        this.itemList = itemList;
        this.id = id;
        this.message = message;
        this.token = token;
        this.callback = callback;

        comment_id = null;
        galleryReply = true;
    }

    public CommentItems(Context context, GalleryViewerRecyclerAdapter.commentRefreshInterface cListener, TokenHelper helper,
                        ArrayList<String> commentAry, Upload itemList, String comment_id, String id,
                        String message, String token, Callback<ImageResponse> callback){
        this.context = context;

        this.cListener = cListener;
        this.helper = helper;
        this.commentAry = commentAry;
        this.itemList = itemList;

        this.comment_id = comment_id;

        this.id = id;
        this.message = message;
        this.token = token;
        this.callback = callback;

        galleryReply = false;
    }
}