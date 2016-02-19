package com.altoncng.commentingforimgur.imgurmodel;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Eye on 9/18/2015.
 */
public class Upload implements Serializable, Parcelable {
    public File image;
    public String title;
    public String description;
    public String albumId;
    public String albumLink;
    public String token;
    public String hThumbnailLink;
    public String account_url;

    public String coverId;
    public String id = null;

    public boolean toGallery;
    public String topic;
    public String terms;
    public String mature;
    public String animated;

    public int task;
    public int ups;
    public int downs;
    public int views;
    public int size;
    public int width;
    public int height;
    public String vote;
    public String favorite;

    public String imageNum;

    public boolean isAlbum;
    public Album album;

    public boolean commentable = true;

    public ArrayList<CommentTree> comment_tree;
    public ArrayList<CommentTree> comment_tree_list;

    public Upload() {
        favorite = "";
        vote = "";
    }

    public void clearUpload(){
        image = null;
        title = null;
        description = null;
        albumId = null;
        token = null;
        isAlbum = false;
    }

    public Upload(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public String toString() {
        return "title: " + title + " id: " + id + " \n\tcover: " + coverId + " albumlink: " + albumLink;
    }

    public String getUps(){
        return formatNum(ups);
    }

    public String getDowns(){
        return formatNum(downs);
    }

    public String getViews(){
        return formatNum(views);
    }

    public String formatNum(int num){
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(num);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String albumStatus;
        if(isAlbum){
            albumStatus = "true";
        }else
            albumStatus = "false";
        String comments;
        if(commentable)
            comments = "true";
        else
            comments = "false";

        String[] ary = {coverId, albumId, title, albumLink, Integer.toString(ups),
                Integer.toString(downs), albumStatus, Integer.toString(size), animated,
                Integer.toString(width), Integer.toString(height), hThumbnailLink, account_url, id,
                vote, favorite, description, Integer.toString(views), comments};
        dest.writeStringArray(ary);
    }

    public void readFromParcel(Parcel in){
        String[] ary = new String[19];
        in.readStringArray(ary);
        coverId = ary[0];
        albumId = ary[1];
        title = ary[2];
        albumLink = ary[3];
        ups = Integer.parseInt(ary[4]);
        downs = Integer.parseInt(ary[5]);
        if(ary[6].equals("true"))
            isAlbum = true;
        else
            isAlbum = false;
        size = Integer.parseInt(ary[7]);
        animated = ary[8];
        width = Integer.parseInt(ary[9]);
        height = Integer.parseInt(ary[10]);
        hThumbnailLink = ary[11];
        account_url = ary[12];
        id = ary[13];
        vote = ary[14];
        favorite = ary[15];
        description = ary[16];
        views = Integer.parseInt(ary[17]);
        commentable = ary[18].equals("true");
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Upload createFromParcel(Parcel in) {
            return new Upload(in);
        }

        public Upload[] newArray(int size) {
            return new Upload[size];
        }
    };

    public int totalCommentsCalc(){
        if(comment_tree == null || comment_tree.size() == 0)
            return 0;
        int size = 0;
        for(int i=0; i<comment_tree.size();i++){
            size+= comment_tree.get(i).getCount();
        }return size + comment_tree.size();
    }

    public CommentTree getComment(int num){
        for(int i=0; i<comment_tree.size(); i++){
            if(comment_tree.get(i).getCount() < num)
                num -= comment_tree.get(i).getCount();
            else{//more children in this commenttree than leftover position num

            }
        }
        return null;//index out of range?
    }
}
