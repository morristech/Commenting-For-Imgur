package com.altoncng.commentingforimgur;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.altoncng.commentingforimgur.dynamicviewpager.CustomPager;
import com.altoncng.commentingforimgur.dynamicviewpager.MyPagerAdapter;
import com.altoncng.commentingforimgur.imgurmodel.Upload;

import java.util.ArrayList;

/*
 * Class for viewing singular posts from the gallery, holding a custom viewpager with GalleryFragment
 */
public class GalleryViewerActivity  extends Activity implements GalleryFragment.OnFragmentInteractionListener{

    View fragmentFrameLayout;
    ArrayList<Upload> galleryPostList;
    int position;

    CustomPager viewpager;
    private MyPagerAdapter mPagerAdapter;

    ScrollView galleryPageScrollView;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_gallery_viewer);
        setContentView(R.layout.activity_gallery_post_viewpager);

        Intent intent = getIntent();
        galleryPostList = intent.getParcelableArrayListExtra("gallery_data");
        position = intent.getIntExtra("position", 0);

        viewpager = (CustomPager) findViewById(R.id.viewpager);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager(), position, galleryPostList, galleryPostList.size());
        viewpager.setAdapter(mPagerAdapter);
        viewpager.setCurrentItem(position);
        viewpager.setOffscreenPageLimit(1);

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //do nothing here
            }

            @Override
            public void onPageSelected(int position) {
                //galleryPageScrollView.fullScroll(View.FOCUS_UP);
                try{
                    mPagerAdapter.getFragment(position).allowGet();
                }catch(NullPointerException e){
                    finish();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /*GalleryFragment galleryFragment = GalleryFragment.newInstance(galleryPostList.get(position), position);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(fragmentFrameLayout.getId(), galleryFragment, GalleryFragment.class.getName());
        fragmentTransaction.commit();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSwipe(int newPosition) {
        position = newPosition;
    }

}
