package com.demo.photogallery.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yufei0213 on 2017/2/12.
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean hasQuit;
    private Handler requestHandler;
    private Handler responseHandler;
    private ConcurrentHashMap<T, String> requestMap = new ConcurrentHashMap<>(); //线程安全的map

    private ThumbnailDownloaderListener thumbnailDownloaderListener;

    public interface ThumbnailDownloaderListener<T> {

        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloaderListener(ThumbnailDownloaderListener<T> thumbnailDownloaderListener) {

        this.thumbnailDownloaderListener = thumbnailDownloaderListener;
    }

    public ThumbnailDownloader(Handler responseHandler) {

        super(TAG);
        this.responseHandler = responseHandler;
    }

    public void clearQueue() {

        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    public boolean quit() {

        hasQuit = true;
        return super.quit();
    }

    @Override
    protected void onLooperPrepared() {

        requestHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MESSAGE_DOWNLOAD) {

                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + requestMap.get(target));

                }
            }
        };
    }

    public void queueThumbinal(T target, String url) {

        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {

            requestMap.remove(target);
        } else {

            requestMap.put(target, url);
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
            handleRequest(target);
        }
    }

    private void handleRequest(final T target) {

        try {

            final String url = requestMap.get(target);
            if (url == null)
                return;

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            responseHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (requestMap.get(target) == null || hasQuit)
                        return;

                    requestMap.remove(target);
                    thumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {

            e.printStackTrace();
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
