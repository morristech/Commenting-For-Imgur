package com.altoncng.commentingforimgur.imgurmodel;

/**
 * Created by Eye on 11/2/2015.
 */
public class Comment {
    public String comment_id;
    public String author;
    public String comment;
    public int status; //1 for upvote, 0, for none, -1 for downvote?
    public int points;
    public int comment_level;
    public boolean hidden = true;
    public int layoutType;
    public int colorBG;
    public boolean isAuthor;
    public String postTime;
}
