package com.demo.photogallery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yufei0213 on 2017/2/14.
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    public static final String PERM_RECEIVER = "com.demo.photogallery.receiver.StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
    }
}
