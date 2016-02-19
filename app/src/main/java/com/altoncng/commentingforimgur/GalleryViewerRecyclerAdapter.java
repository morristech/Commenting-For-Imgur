package com.altoncng.commentingforimgur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.altoncng.commentingforimgur.helpers.TokenHelper;
import com.altoncng.commentingforimgur.helpers.TokenHelperInterface;
import com.altoncng.commentingforimgur.imgurmodel.Comment;
import com.altoncng.commentingforimgur.imgurmodel.ImageResponse;
import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.profile.ProfileActivity;
import com.altoncng.commentingforimgur.utils.CommonMethods;
import com.altoncng.commentingforimgur.utils.dbLog;
import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Eye on 10/30/2015.
 *
 * The recyclerView adapter for displaying an imgur post. Generally made of a title, the image(s),
 * description, a row for displaying post statistics and holding buttons, and all the comments and
 * nested comments
 */
public class GalleryViewerRecyclerAdapter extends RecyclerView.Adapter<GalleryViewerViewHolder> implements TokenHelperInterface {

    private Upload itemList;
    private Context context;

    private commentRefreshInterface cListener;

    int width;
    int height;

    final int numColumns = 1;

    Drawable placeholderImage;

    float margin;

    FrameLayout.LayoutParams LLparamsHide;
    FrameLayout.LayoutParams LLparamsShow;

    private int last_focused_comment = -1;
    private int currPosition = 0;

    SharedPreferences sharedpreferences;
    TokenHelper tokenHelper;

    private Comment toVoteOn;
    private Comment postVote;

    private String favoritePostId;

    private ArrayList<String> commentAry;
    private String comment_id;
    private String id;
    private int commentPostedCount = 0;

    private FrameLayout videoFrameLayout;
    private ImageView imgurThumbnailVideoView;

    private TextView statCommentCharCounter;
    private TextView focusedCommentCharCounter;

    ProfileClickListener profileClickListener;

    public GalleryViewerRecyclerAdapter(Context context, Upload itemList, SharedPreferences sharedpreferences, commentRefreshInterface cListener) {
        this.itemList = itemList;
        this.context = context;
        this.cListener = cListener;
        dbLog.w("imgurLog", "imgurLog GalleryViewerRecyclerAdapter constructor");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultimage160);
        placeholderImage = resize(icon);

        margin = convertDpToPixel(4, context);

        LLparamsHide = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0);
        LLparamsShow = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        this.sharedpreferences = sharedpreferences;
        tokenHelper = new TokenHelper(this.sharedpreferences, this, context);

    }

    @Override
    public GalleryViewerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView;

        if(viewType == Constants.TYPE_TITLE)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_title, null);
        else if(viewType == Constants.TYPE_TEXT_IMAGE_TEXT)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_imageview, null);
        else if(viewType == Constants.TYPE_TEXT_VIDEO_TEXT)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_videoview, null);
        else if(viewType == Constants.TYPE_DESCRIPTION)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_description, null);
        else if(viewType == Constants.TYPE_STATS)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_stats, null);
        else if(viewType == -1)
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_hidden, null);
        else {
            layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_comment, null);
            if(viewType == Constants.TYPE_FOCUSED_COMMENT){
                layoutView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_focused, null);
                //and onclick listeners?
            }
            GalleryViewerViewHolder rcv = new GalleryViewerViewHolder(layoutView, viewType, new GalleryViewerViewHolder.RecyclerClickInterface() {
                public void clicked(View view, int position) {
                    //focused
                    if (last_focused_comment > -1 && last_focused_comment != position) {//focus new click, unfocus old
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).data.layoutType = Constants.TYPE_FOCUSED_COMMENT;
                        itemList.comment_tree_list.get(last_focused_comment - itemList.album.imageCount).data.layoutType = Constants.TYPE_COMMENT;
                        notifyItemChanged(last_focused_comment);
                        notifyItemChanged(position);
                        last_focused_comment = position;
                    }else if(last_focused_comment == position) {//unfocus if reclicked
                        last_focused_comment = -1;
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).data.layoutType = Constants.TYPE_COMMENT;
                        notifyItemChanged(position);
                    }else{//if no previous click
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).data.layoutType = Constants.TYPE_FOCUSED_COMMENT;
                        notifyItemChanged(position);
                        last_focused_comment = position;
                    }

                    //if closed, then open
                    if (itemList.comment_tree_list.get(position - itemList.album.imageCount).data.hidden) {
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).data.hidden = false;

                        int children = itemList.comment_tree_list.get(position - itemList.album.imageCount).child_positions.size();
                        for (int i = 0; i < children; i++) {
                            itemList.comment_tree_list.get(position - itemList.album.imageCount).children.get(i).data.layoutType = Constants.TYPE_COMMENT;
                            notifyItemChanged(itemList.comment_tree_list.get(position - itemList.album.imageCount).child_positions.get(i) + itemList.album.imageCount);
                        }
                    } else {//else close all children
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).data.hidden = true;
                        itemList.comment_tree_list.get(position - itemList.album.imageCount).cascadeClose(itemList.comment_tree_list.get(position - itemList.album.imageCount).children);
                        notifyDataSetChanged();

                        //if last_focused_position == position, set to -1, else do as above and change
                    }
                }
            });

            return rcv;
        }
        GalleryViewerViewHolder rcv = new GalleryViewerViewHolder(layoutView, viewType);
        return rcv;
    }

    @Override
    public void onBindViewHolder(GalleryViewerViewHolder holder, int position) {
        //setting card type as per position
        if(position < itemList.album.imageCount) {//+1 to count for header
        //Check if position is a comment or not first, since the majority of views will be comments

/******************************************************************************************************************************/
            if(getItemViewType(position) == Constants.TYPE_STATS) {
                if(itemList.vote.equals("up")) {
                    holder.upvoteStatsRowButton.setImageResource(R.drawable.upvoteactivetransparent32p);
                    holder.downvoteStatsRowButton.setImageResource(R.drawable.downvoteinactivetransparent32p);
                }else if(itemList.vote.equals("down")) {
                    holder.upvoteStatsRowButton.setImageResource(R.drawable.upvoteinactivetransparent32p);
                    holder.downvoteStatsRowButton.setImageResource(R.drawable.downvoteactivetransparent32p);
                }else{
                    holder.upvoteStatsRowButton.setImageResource(R.drawable.upvoteinactivetransparent32p);
                    holder.downvoteStatsRowButton.setImageResource(R.drawable.downvoteinactivetransparent32p);
                }

                statCommentCharCounter = holder.charCountTextView;
                holder.commentEditText.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    public void afterTextChanged(Editable s) {
                        if (s.length() > 1200)
                            statCommentCharCounter.setTextColor(0xFFEE4444);
                        else
                            statCommentCharCounter.setTextColor(0xFFFFFFEE);
                        statCommentCharCounter.setText(s.length() + "/1200");
                    }
                });

                if(!itemList.commentable){
                    holder.upvoteStatsRowButton.setVisibility(View.GONE);
                    holder.downvoteStatsRowButton.setVisibility(View.GONE);
                    holder.statsUpvoteTextView.setVisibility(View.INVISIBLE);
                    holder.statsDownvoteTextView.setVisibility(View.INVISIBLE);
                    holder.replyToPostButton.setVisibility(View.GONE);
                    holder.commentEditText.setVisibility(View.GONE);
                }

                holder.statsUpvoteTextView.setText(itemList.getUps() + " up");
                holder.statsDownvoteTextView.setText(itemList.getDowns() + " down");
                holder.statsViewsTextView.setText(itemList.getViews() + " views");

                if(itemList.favorite.equals("true"))
                    holder.favoriteStatsRowButton.setImageResource(R.drawable.activefavoritetransparent32p);
                else
                    holder.favoriteStatsRowButton.setImageResource(R.drawable.inactivefavoritetransparent32p);

                holder.upvoteStatsRowButton.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //send upvote post, toggle image, toggle opposite image
                        if(!tokenHelper.isSignedIn()){
                            displayLoginDialog(v);
                        }else {
                            String vote = "up";
                            if (itemList.vote.equals("up")) {
                                itemList.vote = "";
                                itemList.ups -= 1;
                            }else {
                                itemList.vote = "up";
                                itemList.ups += 1;
                            }notifyItemChanged(itemList.album.imageCount - 1);
                            if(itemList.isAlbum)
                                GalleryViewerRecyclerAdapter.this.postVote(vote, itemList.albumId, 0);
                            else
                                GalleryViewerRecyclerAdapter.this.postVote(vote, itemList.id, 0);
                        }
                    }
                });

                holder.downvoteStatsRowButton.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //send downvote post, toggle image, toggle opposite image
                        if(!tokenHelper.isSignedIn()){
                            displayLoginDialog(v);
                        }else {
                            String vote = "up";
                            if (itemList.vote.equals("down")) {
                                itemList.vote = "";
                                itemList.downs -= 1;
                            } else {
                                itemList.vote = "down";
                                itemList.downs += 1;
                            }notifyItemChanged(itemList.album.imageCount - 1);
                            if(itemList.isAlbum)
                                GalleryViewerRecyclerAdapter.this.postVote(vote, itemList.albumId, 0);
                            else
                                GalleryViewerRecyclerAdapter.this.postVote(vote, itemList.id, 0);
                        }
                    }
                });
                holder.favoriteStatsRowButton.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //check if upvoted, if not, upvote. send post favorite
                        if(!tokenHelper.isSignedIn()){
                            displayLoginDialog(v);
                        }else {
                            if (itemList.favorite.equals("true"))
                                itemList.favorite = "false";
                            else {
                                itemList.favorite = "true";
                                if (itemList.vote.equals("") || itemList.vote.equals("null"))
                                    itemList.vote = "true";
                            }
                            notifyItemChanged(itemList.album.imageCount - 1);
                            if (itemList.isAlbum)
                                GalleryViewerRecyclerAdapter.this.favoriteAlbum(itemList.albumId, 0);
                            else
                                GalleryViewerRecyclerAdapter.this.favoriteImage(itemList.id, 0);
                        }
                    }
                });
                final EditText replyEditText = holder.commentEditText;
                holder.replyToPostButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (replyEditText.getText().toString().length() == 0 || replyEditText.getText().toString().trim().length() == 0)
                            return;
                        else {
                            GalleryViewerRecyclerAdapter.this.reply(null, itemList.id, replyEditText.getText().toString(), 5);
                            //replyEditText.setText("");
                        }
                    }
                });
                return;
            }

/******************************************************************************************************************************/
            else if(getItemViewType(position) == Constants.TYPE_DESCRIPTION){
                dbLog.w("imgurLog", "imgurLog itemList.album.description[0] ." + itemList.album.description[0] + ". " + "itemList.album.description[0].equals(\"null\") ." + ".");
                if(itemList.album.description[0] == null || itemList.album.description[0].equals("null"))
                    holder.commentMargin.setVisibility(View.GONE);
                else {
                    holder.descriptionTextView.setText(Html.fromHtml(itemList.album.description[0]));
                    holder.descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
                }return;
            }

            holder.titleTextView.setText(itemList.album.title[position]);
            if(itemList.album.description[position] != null) {
                holder.descriptionTextView.setText(Html.fromHtml(itemList.album.description[position]));
                holder.descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }

/******************************************************************************************************************************/
            if(getItemViewType(position) == Constants.TYPE_TITLE){
                if(!itemList.account_url.equals("null") && itemList.account_url != null) {
                    holder.descriptionTextView.setText(itemList.account_url);
                    holder.descriptionTextView.setOnClickListener(new ProfileClickListener(itemList.account_url));
                }else
                    holder.descriptionTextView.setText("Posted from Reddit");
                return;
            }

            if(itemList.album.title[position] == null || itemList.album.title[position].equals("null"))
                holder.titleTextView.setVisibility(View.GONE);
            if(itemList.album.description[position] == null || itemList.album.description[position].equals("null"))
                holder.descriptionTextView.setVisibility(View.GONE);

            if(getItemViewType(position) == 0){
                holder.titleTextView.setText(itemList.album.title[0]);
                holder.descriptionTextView.setText(itemList.account_url);
            }else if(getItemViewType(position) == 1){
                if(itemList.album.link[position].substring(itemList.album.link[position].length() - 1).equals("v"))
                    itemList.album.link[position] = itemList.album.link[position].substring(0, itemList.album.link[position].length() - 1);
                Ion.with(holder.imageView)
                        .placeholder(R.drawable.defaultimage320)
                        .load(itemList.album.link[position]);
                //dbLog.w("imgurLog", "imgurLog onBindViewHolder ion " + itemList.album.link[position]);
            }else{
                if(itemList.album.link[position].substring(itemList.album.link[position].length() - 1).equals("4"))
                    itemList.album.link[position] = itemList.album.link[position].substring(0, itemList.album.link[position].length() - 3) + "webm";
                Uri uri = Uri.parse(itemList.album.link[position]);
                holder.videoView.setVideoURI(uri);

                videoFrameLayout = holder.video_frame;
                imgurThumbnailVideoView = holder.imgurThumbnailVideoView;

                Ion.with(holder.imgurThumbnailVideoView)
                        .load(itemList.album.hThumbnailLink[position]);

                //Glide is faster, but loaded image is much lower quality
                /*Glide.with(context)
                        .load( itemList.album.hThumbnailLink[position] )
                        //.placeholder(R.drawable.defaultimage320)
                        //.error(R.drawable.full_cake)
                        .into(holder.imgurThumbnailVideoView);*/

                holder.videoView.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //Log.w("imgurLog", "imgurLog GalleryFragment onTouch videoview " + ((VideoView) v).isPlaying());
                        //Log.w("imgurLog", "imgurLog videoview " + ((VideoView) v).getBufferPercentage() + " " + ((VideoView) v).getCurrentPosition());
                        if (((VideoView) v).isPlaying()) {
                            videoFrameLayout.setForeground(ContextCompat.getDrawable(context, R.drawable.playbuttonoverlay256p));
                            ((VideoView) v).pause();
                        } else {
                            videoFrameLayout.setForeground(null);
                            imgurThumbnailVideoView.setVisibility(View.GONE);
                            ((VideoView) v).start();
                        }
                        return false;
                    }
                });
                holder.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        //Log.w("imgurLog", "imgurLog onError videoview what=" + what + " extra=" + extra);
                        return false;
                    }
                });
                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //mp.seekTo(0);
                        mp.setLooping(true);
                    }
                });

                //Log.w("imgurLog", "imgurLog onBindViewHolder videoView " + itemList.album.link[position]);
            }
        }

/******************************************************************************************************************************/
        //else its a comment...
        else{
            //dbLog.w("imgurLog", "imgurLog clicked else position " + position + " imageCount " + itemList.album.imageCount + " getItemCount " + getItemCount() + " replies " + itemList.comment_tree_list.get(position - itemList.album.imageCount).getDirectCount());
            if(getItemViewType(position) == Constants.TYPE_HIDDEN_COMMENT)
                return;

            //set margin according to comment node depth
            int marginsLevel = (int)3*itemList.comment_tree_list.get(position-itemList.album.imageCount).data.comment_level;
            marginsLevel%=24;
            marginsLevel*=margin;
            if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.comment_level > 7){
                marginsLevel+=3*margin;
                holder.commentMargin.setBackgroundColor(0xFF232358);
            }else
                holder.commentMargin.setBackgroundColor(0xFF262626);
            LLparamsShow.setMargins(marginsLevel, 0, 0, (int) margin / 2);
            holder.commentMargin.setLayoutParams(LLparamsShow);

            //OP symbol
            if(itemList.comment_tree_list.get(position - itemList.album.imageCount).data.isAuthor)
                holder.opTextView.setVisibility(View.VISIBLE);
            else
                holder.opTextView.setVisibility(View.GONE);

            //set comment author
            holder.authorTextView.setText(itemList.comment_tree_list.get(position - itemList.album.imageCount).data.author);
            holder.authorTextView.setOnClickListener(new ProfileClickListener(itemList.comment_tree_list.get(position - itemList.album.imageCount).data.author));

            //set comment points
            if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.points == 1)
                holder.pointsTextView.setText(itemList.comment_tree_list.get(position - itemList.album.imageCount).data.points + " point");
            else
                holder.pointsTextView.setText(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.points + " points");

            if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.points > 0)
                holder.pointsTextView.setTextColor(0xFF85BF25);
            else if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.points < 0)
                holder.pointsTextView.setTextColor(0xFFEE4444);

            //comment votes
            if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.status == 1) {
                holder.commentVoteIndicator.setBackgroundResource(R.drawable.commentupvotebg);
            }else if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.status == -1) {
                holder.commentVoteIndicator.setBackgroundResource(R.drawable.commentdownvotebg);
            }else{
                holder.commentVoteIndicator.setBackgroundColor(0xFF85BF25);
                holder.commentVoteIndicator.setBackgroundResource(0);
            }

            //comment time
            holder.timeTextView.setText(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.postTime);

            if(itemList.comment_tree_list.get(position-itemList.album.imageCount).getDirectCount() > 0){
                String reply = "";
                if(itemList.comment_tree_list.get(position-itemList.album.imageCount).getDirectCount() == 1)
                    reply = " reply";
                else
                    reply = " replies";
                holder.repliesTextView.setText(itemList.comment_tree_list.get(position - itemList.album.imageCount).getDirectCount() + reply);
                holder.repliesTextView.setVisibility(View.VISIBLE);
            }else{
                holder.repliesTextView.setText("");
                holder.repliesTextView.setVisibility(View.GONE);
            }
            holder.descriptionTextView.setText(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.comment);

/******************************************************************************************************************************/
            //focused comment for extra options
            if(position == last_focused_comment && getItemViewType(position) == Constants.TYPE_FOCUSED_COMMENT){

                focusedCommentCharCounter = holder.charCountTextView;
                holder.replyCommentEditText.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    public void afterTextChanged(Editable s) {
                        if(s.length()>1200)
                            focusedCommentCharCounter.setTextColor(0xFFEE4444);
                        else
                            focusedCommentCharCounter.setTextColor(0xFFFFFFEE);
                        focusedCommentCharCounter.setText(s.length() + "/1200");
                    }
                });

                //set up or downvote
                if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.status == 1) {
                    holder.upvoteCommentButton.setBackgroundColor(0xFF85BF25);
                    holder.downvoteCommentButton.setBackgroundColor(0xFF262626);
                }else if(itemList.comment_tree_list.get(position-itemList.album.imageCount).data.status == -1) {
                    holder.upvoteCommentButton.setBackgroundColor(0xFF262626);
                    holder.downvoteCommentButton.setBackgroundColor(0xFFEE4444);
                }else{
                    holder.upvoteCommentButton.setBackgroundColor(0xFF262626);
                    holder.downvoteCommentButton.setBackgroundColor(0xFF262626);
                }

                currPosition = position;

                holder.upvoteCommentButton.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        //dbLog.w("imgurLog", "imgurLog UPVOTE COMMENT CLICKED");
                        if(!tokenHelper.isSignedIn()){
                            displayLoginDialog(v);
                        }else {
                            String vote = "up";
                            if (itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status != 1)
                                itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status = 1;
                            else
                                itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status = 0;
                            notifyItemChanged(currPosition - itemList.album.imageCount);
                            GalleryViewerRecyclerAdapter.this.commentVote(vote, itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.comment_id, 0);
                        }
                    }
                });
                holder.downvoteCommentButton.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        //dbLog.w("imgurLog", "imgurLog DOWNVOTE COMMENT CLICKED");
                        if(!tokenHelper.isSignedIn()){
                            displayLoginDialog(v);
                        }else {
                            String vote = "down";
                            if (itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status != -1)
                                itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status = -1;
                            else
                                itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.status = 0;
                            notifyItemChanged(currPosition - itemList.album.imageCount);
                            GalleryViewerRecyclerAdapter.this.commentVote(vote, itemList.comment_tree_list.get(currPosition - itemList.album.imageCount).data.comment_id, 0);
                        }
                    }
                });
                final EditText replyCommentEditText = holder.replyCommentEditText;
                holder.replyToPostButton.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        //dbLog.w("imgurLog", "imgurLog REPLY TO COMMENT CLICKED");
                        if(replyCommentEditText.getText().toString().length() == 0 || replyCommentEditText.getText().toString().trim().length() == 0)
                            return;
                        else{
                            GalleryViewerRecyclerAdapter.this.reply(itemList.comment_tree_list.get(
                                            currPosition - itemList.album.imageCount).data.comment_id,
                                            itemList.id, replyCommentEditText.getText().toString(), 4);
                            replyCommentEditText.setText("");
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if(itemList.album == null) {
            return 0;
        }if(itemList.isAlbum) {
            return itemList.album.imageCount + itemList.totalCommentsCalc();
        }else {
            return 4 + itemList.totalCommentsCalc();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(itemList.album.imageCount > position)
            return itemList.album.layoutType[position];
        else
            return itemList.comment_tree_list.get(position - itemList.album.imageCount).data.layoutType;
    }

    private Drawable resize(Bitmap b) {
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width / numColumns, (height / 6) * numColumns, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    @Override
    public void processFinish(String output, int task, int errCode) {
        if (task == 0) {// refresh token, vote comment
            new UploadService(context).ExecuteVoteComment(toVoteOn.comment_id, toVoteOn.status == 1 ? "up" : "down", tokenHelper.accessToken, new UiCallback());
        } else if (task == 1) {
            new UploadService(context).ExecuteVotePost(postVote.comment_id, postVote.status == 1 ? "up" : "down", tokenHelper.accessToken, new UiCallback());
        } else if (task == 2) {
            new UploadService(context).ExecuteFavoriteImage(favoritePostId, tokenHelper.accessToken, new UiCallback());
        } else if (task == 3) {
            new UploadService(context).ExecuteFavoriteAlbum(favoritePostId, tokenHelper.accessToken, new UiCallback());
        } else if (task == 4) {//replyComment
            ;
        } else if (task == 5) {//replyPost
            ;
        }
    }

    @Override
    public void tokensReceived(String username, boolean success, int errCode) {
        if(errCode == 200)//successfully got account info/info is correct
            ;
        else{
            Toast.makeText(context, ErrorMessage.returnErrorMessage(errCode), Toast.LENGTH_LONG).show();
        }
    }

    private void commentVote(String vote, String id, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        toVoteOn = new Comment();
        toVoteOn.status = "up".equals(vote) ? 1 : -1;
        toVoteOn.comment_id = id;

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            tokenHelper.new getRefreshToken(0).execute();
        }else {
            new UploadService(context).ExecuteVoteComment(id, vote, tokenHelper.accessToken, new UiCallback());
        }
    }

    private void postVote(String vote, String id, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        postVote = new Comment();
        postVote.status = "up".equals(vote) ? 1 : -1;
        postVote.comment_id = id;

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            tokenHelper.new getRefreshToken(1).execute();
        }else {
            new UploadService(context).ExecuteVotePost(id, vote, tokenHelper.accessToken, new UiCallback());
        }
    }

    private void favoriteImage(String id, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        favoritePostId = id;

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            tokenHelper.new getRefreshToken(2).execute();
        } else {
            new UploadService(context).ExecuteFavoriteImage(favoritePostId, tokenHelper.accessToken, new UiCallback());
        }
    }

    private void favoriteAlbum(String id, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        favoritePostId = id;

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            tokenHelper.new getRefreshToken(3).execute();
        } else {
            new UploadService(context).ExecuteFavoriteAlbum(favoritePostId, tokenHelper.accessToken, new UiCallback());
        }
    }

    //combined previous replycomment and replypost
    private void reply(String comment_id, String id, String message, int task) {
        int currTime = (int) System.currentTimeMillis()/1000;

        this.id = id;
        this.comment_id = comment_id;
        commentPostedCount = 0;

        commentMaker(message);

        if(currTime - tokenHelper.timeSince > Constants.TOKEN_REFRESH_TIME){
            tokenHelper.new getRefreshToken(task).execute();
        }else {
            commentPreview(commentAry);
        }
    }

    //separate out longer comments into postable chunks (140 char limit)
    private void commentMaker(String message){
        commentAry = new ArrayList<>();
        int count = 1;
        while(message.length() > 140){
            String str = message.substring(0, 135);
            int index = str.lastIndexOf(" ");

            if (index == -1) {
                str = str + "-" + count + "/";
                message = message.substring(135);
            } else {
                str = str.substring(0, index) + " " + count + "/";
                message = message.substring(index);
            }
            count++;
            commentAry.add(str);
        }
        if(message.length() > 0) {
            String str = message.substring(0, message.length());
            if(commentAry.size() > 1 && message.length() <= 136){
                str = str + " " + count + "/";
                commentAry.add(str);
            }else
                commentAry.add(str);
        }
        int size = commentAry.size();

        if(size > 1){
            for(int i=0; i<size; i++){
                if(commentAry.get(i).length() <= 139) {
                    String temp = commentAry.get(i) + "" + size;
                    commentAry.set(i, temp);
                }
            }
        }
    }

    //creates a preview of the comment to be posted, checking if comment queue is full first
    public void commentPreview(ArrayList<String> comments){
        if(CommentQueue.queueFull()){
            Toast.makeText(context, "Comment queue currently full. Comments are currently being posted, please slow down!", Toast.LENGTH_LONG).show();
            return;
        }
        final Dialog previewDialog = new Dialog(context);
        previewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        previewDialog.setContentView(R.layout.preview_main);

        LinearLayout ll = (LinearLayout) previewDialog.findViewById(R.id.preview_body);

        //create the comment preview reply layout with the separated out comments
        for(int i=0; i<comments.size(); i++){
            View myView = LayoutInflater.from(context).inflate(R.layout.gallery_post_row_comment, null);

            FrameLayout container = new FrameLayout(context);
            FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int)margin * 3 * i;
            params.topMargin = 5;
            myView.setLayoutParams(params);
            container.addView(myView);

            TextView authorTV = (TextView) myView.findViewById(R.id.authorTextView);
            TextView pointsTV = (TextView) myView.findViewById(R.id.pointsTextView);
            TextView repliesTV = (TextView) myView.findViewById(R.id.repliesTextView);
            TextView descriptionTV = (TextView) myView.findViewById(R.id.descriptionTextView);

            authorTV.setText("Anonymous");
            pointsTV.setText("1");
            pointsTV.setTextColor(0xFF85BF25);
            if(i+1 != comments.size())
                repliesTV.setText("1 reply");
            descriptionTV.setText(comments.get(i));

            ll.addView(container);
        }

        Button cancelButton = (Button) previewDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewDialog.dismiss();
            }
        });
        Button postButton = (Button) previewDialog.findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment_id == null) {
                    CommentItems cmtItems = new CommentItems(context, cListener, tokenHelper, commentAry, itemList, id, commentAry.get(0), tokenHelper.accessToken, new UiCallback());
                    CommentQueue.add(cmtItems);
                }else {
                    CommentItems cmtItems = new CommentItems(context, cListener, tokenHelper, commentAry, itemList, comment_id, id, commentAry.get(0), tokenHelper.accessToken, new UiCallback());
                    CommentQueue.add(cmtItems);
                }previewDialog.dismiss();
            }
        });

        previewDialog.show();
    }

    public void displayLoginDialog(View v){
        dbLog.w("imgurLog", "imgurLog displayLoginDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

        final EditText loginET = new EditText(v.getContext());
        alert.setMessage("Enter or copy and paste your pin below to log in with this app");
        alert.setTitle("Login");
        alert.setView(loginET);
        loginET.setTextIsSelectable(true);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                tokenHelper.new getAuthorization().execute(loginET.getText().toString());
            }
        });

        final AlertDialog dialog = alert.create();

        loginET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        dialog.show();
        CommonMethods.getAcctPermission(context);
    }

    public interface commentRefreshInterface{
        public void refreshComments();
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            final String postedCommentId = imageResponse.data.id; // this is true for reply to post at least
            commentPostedCount++;
            if(commentPostedCount < commentAry.size()){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new UploadService(context).ExecuteReplyComment(cListener, postedCommentId, itemList.id, commentAry.get(commentPostedCount), tokenHelper.accessToken, new UiCallback());
                    }
                }, 30000);
            }else
                cListener.refreshComments();
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Toast.makeText(context, commentPostedCount + "/" + commentAry.size() + " with " + commentPostedCount + " comment overloading imgur rates", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ProfileClickListener implements View.OnClickListener
    {
        String name;
        public ProfileClickListener(String name) {
            this.name = name;
        }

        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("username", name);
            context.startActivity(intent);
        }
    };
}
