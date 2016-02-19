package com.altoncng.commentingforimgur.imgurmodel;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public interface ImgurAPI {

    String server = "https://api.imgur.com";
    //change ^ and @POST below to change where it publishes?

    /****************************************
     * Upload
     * Image upload API
     */

    @POST("/3/image")
    void postImage(
            @Header("Authorization") String auth,
            @Query("title") String title,
            @Query("description") String description,
            @Query("album") String albumId,
            @Query("account_url") String username,
            @Body TypedFile file,
            Callback<ImageResponse> cb
    );

    @POST("/3/gallery/{id}")
    void postGallery(
            @Path("id") String id,
            @Header("Authorization") String auth,
            @Query("title") String title,
            @Query("topic") String topic,
            @Query("terms") String terms,
            @Query("mature") String mature,
            Callback<ImageResponse> cb
    );

    @PUT("/3/album/{album}/add")
    void addToAlbum(
            @Path("album") String albumId,
            @Header("Authorization") String auth,
            @Query("ids") String[] ids,
            Callback<ImageResponse> cb
    );

    @POST("/3/album")
    void createAlbum(
            @Header("Authorization") String auth,
            @Query("ids") String[] ids,
            @Query("title") String title,
            @Query("description") String description,
            @Query("privacy") String privacy,   // Values are : public | hidden | secret
            @Query("layout") String layout,     // Values are : blog | grid | horizontal | vertical
            @Query("cover") String cover,        // id of image you want as cover
            Callback<ImageResponse> cb
    );

    @POST("/3/comment/{id}/vote/{vote}")
    void voteComment(
            @Path("id") String id,
            @Path("vote") String vote,          //up or down
            @Header("Authorization") String auth,
            Callback<ImageResponse> cb
    );

    @POST("/3/gallery/{id}/vote/{vote}")
    void votePost(
            @Path("id") String id,
            @Path("vote") String vote,          //up or down
            @Header("Authorization") String auth,
            Callback<ImageResponse> cb
    );

    @POST("/3/comment/{id}")
    void replyComment(
            @Path("id") String id,
            @Header("Authorization") String auth,
            @Query("image_id") String imageId,
            @Query("comment") String comment,
            Callback<ImageResponse> cb
    );

    @POST("/3/comment")
    void replyGallery(
            @Header("Authorization") String auth,
            @Query("image_id") String imageId,
            @Query("comment") String comment,
            Callback<ImageResponse> cb
    );

    @POST("/3/image/{id}/favorite")
    void favoriteImage(
            @Path("id") String id,
            @Header("Authorization") String auth,
            Callback<ImageResponse> cb
    );

    @POST("/3/album/{id}/favorite")
    void favoriteAlbum(
            @Path("id") String id,
            @Header("Authorization") String auth,
            Callback<ImageResponse> cb
    );

    @POST("/oauth2/token")
    void getAcctTokens(
            @Query("client_id") String client_id,
            @Query("client_secret") String client_secret,
            @Query("grant_type") String grant_type,
            @Query("pin") String pin,
            Callback<ImageResponse> cb
    );
}