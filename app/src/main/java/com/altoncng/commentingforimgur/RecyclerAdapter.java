package com.altoncng.commentingforimgur;

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
 * Created by Eye on 10/13/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<SolventViewHolders> {

    private ArrayList<Upload> itemList;
    private Context context;

    int width;
    int height;

    final int numColumns = 2;

    Drawable placeholderImage;

    public RecyclerAdapter(Context context, ArrayList<Upload> itemList) {
        this.itemList = itemList;
        this.context = context;
        dbLog.w("imgurLog", "imgurLog RecyclerAdapter constructor");

        if(context != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultimage160);
            placeholderImage = resize(icon);
        }
    }

    @Override
    public SolventViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, null);
        SolventViewHolders rcv = new SolventViewHolders(layoutView, new SolventViewHolders.RecyclerClickInterface(){
            public void clicked(View view, int position){
                dbLog.w("imgurLog", "imgurLog RecyclerAdapter onCreateViewHolder on click");
                Intent intent = new Intent(context, GalleryViewerActivity.class);
                intent.putExtra("gallery_data", itemList);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }

            //Some notifier to keep track of what's clicked?
            //Only problem is knowing what was browsed to after.
            //Would probably involve a lot of callbacks or some static class maybe
            public void longClicked(View view){
                //view.findViewById(R.id.grid_item_image).setBackgroundColor(0xFFEE4444);
            }
        });

        return rcv;
    }

    @Override
    public void onBindViewHolder(SolventViewHolders holder, int position) {
        holder.gridTextView.setText(itemList.get(position).title);
        holder.ivURL = itemList.get(position).coverId;
        holder.gridUpvoteTextView.setText(itemList.get(position).getUps());
        holder.gridDownvoteTextView.setText(itemList.get(position).getDowns());
        if(!itemList.get(position).isAlbum)
            holder.imageNumTextView.setText("1");
        else
            holder.imageNumTextView.setText(itemList.get(position).imageNum);

        Picasso.with(context)
                .load(holder.ivURL)
                .placeholder(placeholderImage)
                .noFade().resize(width / numColumns, (height / 6)*numColumns)
                .centerCrop()
                .into(holder.gridItemImageView);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    private Drawable resize(Bitmap b) {
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width / numColumns, (height / 6) * numColumns, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }
}
