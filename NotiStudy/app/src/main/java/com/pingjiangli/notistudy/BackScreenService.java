package com.pingjiangli.notistudy;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.yotadevices.sdk.BSActivity;

import java.util.ArrayList;

/**
 * Created by Pingjiang.Li on 18/06/15.
 */
public class BackScreenService extends BSActivity {


    private Thread uiUpdateThread;
    private TextView tv;
    private int timer;

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
        setBSContentView(R.layout.back_screen);

        tv = (TextView)findViewById(R.id.tv);



        uiUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.currentThread().sleep(1000);
                        timer ++;
                        Log.d("debug", "timer:" + timer);
                        updateUI("time:\n"+timer);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        uiUpdateThread.start();
    }

    public void updateUI( final String s) {

        BackScreenService.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
            }
        });

    }
}
