package com.altoncng.commentingforimgur.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.altoncng.commentingforimgur.R;
import com.altoncng.commentingforimgur.imgurmodel.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Eye on 1/21/2016.
 */
public class ProfileCommentsRecyclerAdapter extends RecyclerView.Adapter<ProfileCommentsViewHolder>{

    Context context;
    ArrayList<Comment> commentList;
    ArrayList<String> postList;

    String name;

    ProfileCommentsRecyclerAdapter(Context context, String name, ArrayList<Comment> commentList, ArrayList<String> postList){
        this.context = context;
        this.name = name;
        this.commentList = commentList;
        this.postList = postList;
    }

    @Override
    public ProfileCommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_comments_row, parent, false);
        ProfileCommentsViewHolder pcViewHolder = new ProfileCommentsViewHolder(layoutView, 0);
        return pcViewHolder;
    }

    @Override
    public void onBindViewHolder(ProfileCommentsViewHolder holder, int position) {
        holder.authorName.setText(name);
        holder.comment.setText(commentList.get(position).comment);
        holder.points.setText(Integer.toString(commentList.get(position).points));
        holder.time.setText(commentList.get(position).postTime);

        Picasso.with(context)
                .load("http://i.imgur.com/" + postList.get(position) + "h.jpg")
                .fit()
                .centerCrop()
                .placeholder(R.drawable.defaultimage320)
                .into(holder.commentPostImageView);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
