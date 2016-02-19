package com.altoncng.commentingforimgur;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Created by Eye on 10/30/2015.
 */
public class GalleryViewerViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView titleTextView;
    public TextView descriptionTextView;
    public ImageView imageView;
    public VideoView videoView;
    public ImageView imgurThumbnailVideoView;

    public TextView opTextView;
    public TextView authorTextView;
    public TextView pointsTextView;
    public TextView repliesTextView;
    public TextView timeTextView;

    public LinearLayout commentMargin;
    public LinearLayout commentVoteIndicator;
    public Button upvoteCommentButton;
    public Button downvoteCommentButton;
    public EditText replyCommentEditText;

    public ImageButton upvoteStatsRowButton;
    public ImageButton downvoteStatsRowButton;
    public ImageButton favoriteStatsRowButton;
    public TextView statsUpvoteTextView;
    public TextView statsDownvoteTextView;
    public TextView statsViewsTextView;
    public Button replyToPostButton;
    public EditText commentEditText;

    public TextView charCountTextView;

    public RecyclerClickInterface mListener;

    public FrameLayout video_frame;
    LinearLayout.LayoutParams LLparamsShow;
    FrameLayout.LayoutParams FLparamsShow;

    public GalleryViewerViewHolder(View itemView, int viewType) {
        super(itemView);

        LLparamsShow = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LLparamsShow.setMargins(0, 0, 0, 0);

        FLparamsShow = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FLparamsShow.gravity = Gravity.CENTER;
        FLparamsShow.setMargins(0, 0, 0, 0);

        doEverything(itemView, viewType);
    }

    public GalleryViewerViewHolder(View itemView, int viewType, RecyclerClickInterface listener) {
        this(itemView, viewType);
        itemView.setOnClickListener(this);
    }

    private void doEverything(View itemView, int viewType){
        if (viewType == Constants.TYPE_TEXT_IMAGE_TEXT) {
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            imageView = (ImageView) itemView.findViewById(R.id.imgurImageView);

            commentMargin = (LinearLayout) itemView.findViewById(R.id.card_view);
            commentMargin.setLayoutParams(LLparamsShow);
            imageView.setLayoutParams(LLparamsShow);
            /*titleTextView.setLayoutParams(LLparamsShow);
            descriptionTextView.setLayoutParams(LLparamsShow);
            imageView.setLayoutParams(LLparamsShow);*/
        } else if (viewType == Constants.TYPE_TEXT_VIDEO_TEXT) {
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            videoView = (VideoView) itemView.findViewById(R.id.imgurVideoView);
            video_frame = (FrameLayout) itemView.findViewById(R.id.video_frame);

            commentMargin = (LinearLayout) itemView.findViewById(R.id.card_view);
            LLparamsShow.gravity = Gravity.CENTER;
            commentMargin.setLayoutParams(LLparamsShow);
            videoView.setLayoutParams(FLparamsShow);

            imgurThumbnailVideoView = (ImageView) itemView.findViewById(R.id.imgurThumbnailVideoView);
            imgurThumbnailVideoView.setLayoutParams(FLparamsShow);
            /*titleTextView.setLayoutParams(LLparamsShow);
            descriptionTextView.setLayoutParams(LLparamsShow);
            videoView.setLayoutParams(LLparamsShow);
            video_frame.setLayoutParams(LLparamsShow);*/
        } else if (viewType == Constants.TYPE_TITLE) {
            commentMargin = (LinearLayout) itemView.findViewById(R.id.card_view);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);

            commentMargin.setLayoutParams(LLparamsShow);
            /*titleTextView.setLayoutParams(LLparamsShow);
            descriptionTextView.setLayoutParams(LLparamsShow);*/
        } else if(viewType == Constants.TYPE_STATS){
            commentMargin = (LinearLayout) itemView.findViewById(R.id.card_view);
            upvoteStatsRowButton = (ImageButton) itemView.findViewById(R.id.upvoteStatsRowButton);
            downvoteStatsRowButton = (ImageButton) itemView.findViewById(R.id.downvoteStatsRowButton);
            favoriteStatsRowButton = (ImageButton) itemView.findViewById(R.id.favoriteStatsRowButton);
            replyToPostButton = (Button) itemView.findViewById(R.id.replyToPostButton);
            statsUpvoteTextView = (TextView) itemView.findViewById(R.id.statsUpvoteTextView);
            statsDownvoteTextView = (TextView) itemView.findViewById(R.id.statsDownvoteTextView);
            statsViewsTextView = (TextView) itemView.findViewById(R.id.statsViewsTextView);
            commentEditText = (EditText) itemView.findViewById(R.id.commentEditText);
            charCountTextView = (TextView) itemView.findViewById(R.id.charCountTextView);

            commentMargin.setLayoutParams(LLparamsShow);
            /*upvoteStatsRowButton.setLayoutParams(LLparamsShow);
            downvoteStatsRowButton.setLayoutParams(LLparamsShow);
            favoriteStatsRowButton.setLayoutParams(LLparamsShow);
            replyToPostButton.setLayoutParams(LLparamsShow);
            commentEditText.setLayoutParams(LLparamsShow);*/
        }else if(viewType == Constants.TYPE_COMMENT){
            opTextView = (TextView) itemView.findViewById(R.id.OPTextView);
            authorTextView = (TextView) itemView.findViewById(R.id.authorTextView);
            pointsTextView = (TextView) itemView.findViewById(R.id.pointsTextView);
            repliesTextView = (TextView) itemView.findViewById(R.id.repliesTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            commentMargin = (LinearLayout) itemView.findViewById(R.id.commentMargin);
            commentVoteIndicator  = (LinearLayout) itemView.findViewById(R.id.commentVoteIndicator);
        }else if(viewType == Constants.TYPE_FOCUSED_COMMENT) {
            opTextView = (TextView) itemView.findViewById(R.id.OPTextView);
            authorTextView = (TextView) itemView.findViewById(R.id.authorTextView);
            pointsTextView = (TextView) itemView.findViewById(R.id.pointsTextView);
            repliesTextView = (TextView) itemView.findViewById(R.id.repliesTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            commentMargin = (LinearLayout) itemView.findViewById(R.id.commentMargin);
            upvoteCommentButton = (Button) itemView.findViewById(R.id.upvoteCommentButton);
            downvoteCommentButton = (Button) itemView.findViewById(R.id.downvoteCommentButton);
            replyToPostButton = (Button) itemView.findViewById(R.id.replyToPostButton);
            replyCommentEditText = (EditText) itemView.findViewById(R.id.commentEditText);
            commentVoteIndicator = (LinearLayout) itemView.findViewById(R.id.commentVoteIndicator);
            charCountTextView = (TextView) itemView.findViewById(R.id.charCountTextView);
        }else if(viewType == Constants.TYPE_DESCRIPTION){
            commentMargin = (LinearLayout) itemView.findViewById(R.id.card_view);
            commentMargin.setLayoutParams(LLparamsShow);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            descriptionTextView.setLayoutParams(LLparamsShow);
        }else if(viewType == -1)
            ;
    }

    @Override
    public void onClick(View view) {
        mListener.clicked(view, this.getLayoutPosition());
    }

    public static interface RecyclerClickInterface {
        public void clicked(View caller, int position);
    }
}