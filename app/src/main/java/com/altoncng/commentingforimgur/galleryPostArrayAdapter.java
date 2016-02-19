package com.altoncng.commentingforimgur;/*
package com.altoncng.commentingforimgur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.altoncng.commentingforimgur.imgurmodel.Upload;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

*/
/**
 * Created by Eye on 10/29/2015.
 *//*

public class galleryPostArrayAdapter extends BaseAdapter {

    public static final int TYPE_TEXT_IMAGE_TEXT = 0;
    public static final int TYPE_TEXT_VIDEO_TEXT = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_TEXT = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_VIDEO = 5;
    Upload post;

    private static class ViewHolder {
        TextView titleTextView;
        ImageView imageView;
        VideoView videoView;
        TextView descriptionTextView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        //implement logic to see which one to use
        return post.getType();
    }

    public galleryPostArrayAdapter(Context context, int resource, Upload post) {
        super(context, resource, post);
        this.post = post;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        Upload upload = post.get(position);
        int listViewItemType = getItemViewType(position);

        if (convertView == null) {

            boolean title, description, image, video;
            title = description = image = video = false;

            if (listViewItemType == TYPE_TEXT_IMAGE_TEXT) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_post_row_imageview, null);
                title = true;
                description = true;
                image = true;
            }else if (listViewItemType == TYPE_TEXT_VIDEO_TEXT) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.gallery_post_row_videoview, null);
                title = true;
                description = true;
                video = true;
            }

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if(title)
                viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            if(description)
                viewHolder.descriptionTextView = (TextView) convertView.findViewById(R.id.descriptionTextView);
            if(image)
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imgurImageView);
            else if(video)
                viewHolder.videoView = (VideoView) convertView.findViewById(R.id.imgurImageView);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        viewHolder.titleTextView.setText(upload.title);
        Ion.with(viewHolder.imageView)
                .placeholder(R.drawable.loading)
                .placeholder(R.drawable.defaultimage320)
                .load(upload.albumLink);
        viewHolder.descriptionTextView.setText(upload.description);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount(){
        return post.
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
*/
