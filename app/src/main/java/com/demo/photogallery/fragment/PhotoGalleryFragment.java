package com.demo.photogallery.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.demo.photogallery.R;
import com.demo.photogallery.model.GalleryItem;
import com.demo.photogallery.network.FlickrFetchr;
import com.demo.photogallery.network.ThumbnailDownloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufei0213 on 2017/2/5.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String Tag = "PhotoGalleryFragment";

    private RecyclerView recyclerView;

    private List<GalleryItem> galleryItems = new ArrayList<>();

    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {

        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        thumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        thumbnailDownloader.setThumbnailDownloaderListener(new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {

                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                photoHolder.bindDrawable(drawable);
            }
        });
        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(Tag, "Background thread start");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_grallery, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycle_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        thumbnailDownloader.quit();
        Log.i(Tag, "Background thread destory");
    }

    private void setupAdapter() {

        if (isAdded()) {

            recyclerView.setAdapter(new PhotoAdapter(galleryItems));
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> items = new ArrayList<>();

        public PhotoAdapter(List<GalleryItem> items) {

            this.items = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {

            GalleryItem item = items.get(position);
            Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(drawable);
            thumbnailDownloader.queueThumbinal(holder, item.getUrl());
        }

        @Override
        public int getItemCount() {

            return items.size();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public PhotoHolder(View itemView) {

            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable) {

            imageView.setImageDrawable(drawable);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {

            String query = "robot";

            if (query == null) {

                return new FlickrFetchr().fetchRecentPhotos();
            }else {

                return new FlickrFetchr().searchPhotos(query);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {

            galleryItems = items;
            setupAdapter();
        }
    }
}
