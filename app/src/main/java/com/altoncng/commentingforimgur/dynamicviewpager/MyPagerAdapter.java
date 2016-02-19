package com.altoncng.commentingforimgur.dynamicviewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.altoncng.commentingforimgur.GalleryFragment;
import com.altoncng.commentingforimgur.imgurmodel.Upload;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Eye on 10/24/2015.
 */
public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private HashMap<Integer, GalleryFragment> fragments;
    private int mCurrentPosition = -1;

    private int maxSize;
    private ArrayList<Upload> galleryPostList;

    public MyPagerAdapter(FragmentManager fm, int position, ArrayList<Upload> galleryPostList, int size) {
        super(fm);
        this.fragments = new HashMap();
        this.galleryPostList = galleryPostList;
        maxSize = galleryPostList.size();
        maxSize = size;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            CustomPager pager = (CustomPager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        fragments.put(position, GalleryFragment.newInstance(galleryPostList.get(position), 0));
        return fragments.get(position);
    }

    public void destroyItem (ViewGroup container, int position, Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return maxSize;
    }

    public GalleryFragment getFragment(int key) {
        return fragments.get(key);
    }


}