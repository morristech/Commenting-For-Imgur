package com.altoncng.commentingforimgur.imgurmodel;

/**
 * Created by Eye on 9/26/2015.
 */
public class Album {

    public String id;
    public String token;
    public String[] title;
    public String[] description;
    public int datetime;
    public String cover;
    public String[] link;
    public String[] hThumbnailLink;
    public String[] imageIds;
    public boolean[] animated;
    public boolean toGallery;
    public String privacy;
    public String layout;
    public int imageCount;

    public int task;

    public int[] layoutType;

    public Album(){}
    public Album(int num){
        imageCount = num;
        title = new String[num];
        description = new String[num];
        link = new String[num];
        imageIds = new String[num];
        animated = new boolean[num];
        hThumbnailLink = new String[num];
        layoutType = new int[num];
    }

    public Album(String id){
        this.id = id;
    }

    public void createArray(String param){
        imageIds = new String[]{param};
        layout = "vertical";
        privacy = "hidden";
        cover = null;
    }

    public String toString(int num) {
        return "title: " + title[num] + " link: " + link[num] + " description: " + description[num] + "animated: " + animated;
    }
}
