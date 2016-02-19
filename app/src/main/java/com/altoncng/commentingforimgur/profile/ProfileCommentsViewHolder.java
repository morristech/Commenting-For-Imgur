package com.altoncng.commentingforimgur.profile;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.altoncng.commentingforimgur.R;

/**
 * Created by Eye on 1/21/2016.
 */
public class ProfileCommentsViewHolder extends RecyclerView.ViewHolder{

    ImageView commentPostImageView;
    TextView comment;
    TextView authorName;
    TextView points;
    TextView time;

    public ProfileCommentsViewHolder(View itemView, int viewType) {
        super(itemView);

        commentPostImageView = (ImageView) itemView.findViewById(R.id.commentPostImageView);
        comment = (TextView) itemView.findViewById(R.id.descriptionTextView);
        authorName = (TextView) itemView.findViewById(R.id.authorTextView);
        points = (TextView) itemView.findViewById(R.id.pointsTextView);
        time = (TextView) itemView.findViewById(R.id.timeTextView);
    }
}
