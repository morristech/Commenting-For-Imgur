package com.altoncng.commentingforimgur;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SolventViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView gridTextView;
    public TextView gridUpvoteTextView;
    public TextView gridDownvoteTextView;
    public ImageView gridItemImageView;
    public TextView imageNumTextView;
    public String ivURL;

    public RecyclerClickInterface mListener;

    public SolventViewHolders(View itemView, RecyclerClickInterface listener) {
        super(itemView);
        mListener = listener;

        itemView.setOnClickListener(this);

        gridTextView = (TextView) itemView.findViewById(R.id.gridTextView);
        gridItemImageView = (ImageView) itemView.findViewById(R.id.grid_item_image);
        gridUpvoteTextView = (TextView) itemView.findViewById(R.id.gridUpvoteTextView);
        gridDownvoteTextView = (TextView) itemView.findViewById(R.id.gridDownvoteTextView);
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