package com.pingjiangli.notistudy;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

/**
 * Created by Pingjiang.Li on 17/06/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NLService extends NotificationListenerService {

    private String TAG = "NLService";
    public static NLService currentNlservice;

    @Override
    public void onCreate() {
        super.onCreate();

        currentNlservice = this;

        Log.d(TAG, "created!");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationPosted");

        //boardcast to back screen to add a new notification
        Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_POSTNOTIFICATION);
        i.putExtra("StatusBarNotification",sbn);
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG,"onNotificationRemoved");



        Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_REMOVENOTIFICATION);
        i.putExtra("StatusBarNotification",sbn);
        sendBroadcast(i);

    }



}