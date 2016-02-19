package com.altoncng.commentingforimgur;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Eye on 12/31/2015.
 */
public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView gridTextView;
    public ImageView gridItemImageView;
    public TextView imageNumTextView;
    public String ivURL;

    public RecyclerClickInterface mListener;

    public AlbumViewHolder(View itemView, RecyclerClickInterface listener) {
        super(itemView);
        mListener = listener;

        itemView.setOnClickListener(this);

        gridTextView = (TextView) itemView.findViewById(R.id.gridTextView);
        gridItemImageView = (ImageView) itemView.findViewById(R.id.grid_item_image);
        imageNumTextView = (TextView) itemView.findViewById(R.id.imageNumTextView);
    }

    @Override
    public void onClick(View view) {
        mListener.clicked(view, this.getLayoutPosition());
    }

    @Override
    public boolean onLongClick(View v) {
        mListener.longClicked(v);
        return false;
    }

    public static interface RecyclerClickInterface {
        public void clicked(View caller, int position);
        public void longClicked(View caller);
    }
}