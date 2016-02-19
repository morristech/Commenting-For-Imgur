package com.altoncng.commentingforimgur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.altoncng.commentingforimgur.utils.dbLog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Eye on 9/27/2015.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumViewHolder> {

    private ArrayList<Upload> albumList;
    private Context context;

    int width;
    int height;

    final int numColumns = 2;

    Drawable placeholderImage;
    Activity activity;

    public AlbumAdapter(Context context, Activity activity, ArrayList<Upload> albumList) {
        this.albumList = albumList;
        this.context = context;
        this.activity = activity;
        //dbLog.w("imgurLog", "imgurLog RecyclerAdapter constructor");

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultimage160);
        placeholderImage = resize(icon);
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, null);
        AlbumViewHolder rcv = new AlbumViewHolder(layoutView, new AlbumViewHolder.RecyclerClickInterface(){
            public void clicked(View view, int position){
                //dbLog.w("imgurLog", "imgurLog album_Link " + albumList.get(position).albumLink + " album_id " + albumList.get(position).albumId);
                Intent intent = new Intent();
                intent.putExtra("album_Link", albumList.get(position).albumLink);
                intent.putExtra("album_id", albumList.get(position).albumId);
                activity.setResult(100, intent);
                activity.finish();
            }

            public void longClicked(View view){

            }
        });

        return rcv;
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        if(albumList.get(position).title != null)
            holder.gridTextView.setText(albumList.get(position).title);
        else
            holder.gridTextView.setText("");
        holder.ivURL = albumList.get(position).coverId;
        holder.imageNumTextView.setText(albumList.get(position).imageNum);

        Picasso.with(context)
                .load(holder.ivURL)
                .placeholder(placeholderImage)
                .noFade().resize(width / numColumns, (height / 6)*numColumns)
                .centerCrop()
                .into(holder.gridItemImageView);
    }

    @Override
    public int getItemCount() {
        return this.albumList.size();
    }

    private Drawable resize(Bitmap b) {
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width / numColumns, (height / 6) * numColumns, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }
}