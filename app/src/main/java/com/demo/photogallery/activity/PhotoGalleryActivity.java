package com.demo.photogallery.activity;

import android.support.v4.app.Fragment;

import com.demo.photogallery.fragment.PhotoGalleryFragment;

/**
 * Created by yufei0213 on 2017/2/5.
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {

        return PhotoGalleryFragment.newInstance();
    }
}
