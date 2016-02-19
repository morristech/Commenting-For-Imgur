package com.altoncng.commentingforimgur;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Eye on 1/4/2016.
 * For posting comments in 30 second intervals
 */
public class CommentQueue {

    private static Queue<CommentItems> comments_to_post = new LinkedList<>();
    private static boolean posting = false;

    private CommentQueue(){
    }

    public static int queued(){
        return comments_to_post.size();
    }

    public static boolean queueFull(){
        if(queued()>6)
            return true;
        return false;
    }

    public static void add(CommentItems cmt){
        comments_to_post.add(cmt);
        if(posting)
            return;
        else
            postComment();
    }

    public static void postComment(){
        if(!comments_to_post.isEmpty()){
            posting = true;
            CommentItems temp = comments_to_post.remove();
            if(temp.galleryReply)
                new UploadService(temp.context).ExecuteReplyGallery(
                        temp.cListener, temp.helper, temp.commentAry, temp.itemList,
                        temp.id, temp.commentAry.get(0), temp.token, temp.callback);
            else
                new UploadService(temp.context).ExecuteReplyComment(
                        temp.cListener, temp.helper, temp.commentAry, temp.itemList, temp.comment_id,
                        temp.id, temp.commentAry.get(0), temp.token, temp.callback);
        }
    }

    public static void continuePosting(){
        if(!comments_to_post.isEmpty()){
            postComment();
        }else
            posting = false;
    }

    public static boolean isPosting(){
        return comments_to_post.isEmpty();
    }
}
