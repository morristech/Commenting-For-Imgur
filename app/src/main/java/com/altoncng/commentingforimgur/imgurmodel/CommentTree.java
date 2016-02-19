package com.altoncng.commentingforimgur.imgurmodel;

import com.altoncng.commentingforimgur.Constants;

import java.util.ArrayList;

/**
 * Created by Eye on 11/2/2015.
 */
public class CommentTree {

    public Comment data;
    public ArrayList<CommentTree> children;
    public CommentTree parent;

    public int absPosition;
    public ArrayList<Integer> child_positions;

    int count=0;

    public CommentTree(Comment data) {
        this.data = data;
        children = new ArrayList<CommentTree>();
        child_positions = new ArrayList<Integer>();
    }

    public CommentTree(Comment data, CommentTree parent) {
        this.data = data;
        this.parent = parent;
        children = new ArrayList<CommentTree>();
        child_positions = new ArrayList<Integer>();
    }

    public void setParent(CommentTree parent){
        this.parent = parent;
    }

    public void addToCount(int num){
        count+=num;
    }

    public int getCount(){
        return count;
    }

    public void addChild(CommentTree child){
        children.add(child);
        count++;
    }

    public int getDirectCount(){
        return children.size();
    }

    public void cascadeClose(ArrayList<CommentTree> children){
        int cSize = children.size();
        for(int i=0; i<cSize; i++) {
            if (children.get(i).data.layoutType == Constants.TYPE_HIDDEN_COMMENT && children.get(i).data.hidden == true){
                ;
            }else{
                children.get(i).data.hidden = true;
                children.get(i).data.layoutType = Constants.TYPE_HIDDEN_COMMENT;
                cascadeClose(children.get(i).children);
            }
        }return;
    }

}
