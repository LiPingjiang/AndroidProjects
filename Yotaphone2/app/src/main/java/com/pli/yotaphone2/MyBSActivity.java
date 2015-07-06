package com.pli.yotaphone2;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.pli.yotaphone2.auxiliary.GridViewAdapter;
import com.pli.yotaphone2.service.NLService;
import com.yotadevices.sdk.BSActivity;

public class MyBSActivity extends BSActivity {


    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    public static MyBSActivity instance;
    private NotificationManager nManger;


    private TextView tv;
    private TextView stateBar;

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
//        setBSContentView(MainActivity.instance.findViewById(android.R.id.content).);
        setBSContentView(R.layout.back_screen_grid );

        tv = (TextView)findViewById(R.id.tv_grid);
        stateBar = (TextView)findViewById(R.id.statebar_grid);

        gridView = (GridView)findViewById(R.id.grid_notifications_bs);
        nManger  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        instance = this;

        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_NOTISTUDY_POSTNOTIFICATION);
        intentFilter.addAction(MainActivity.ACTION_NOTISTUDY_REMOVENOTIFICATION);
        intentFilter.addAction(MainActivity.ACTION_REFRESH_NOTIFICATION);
        registerReceiver(mBroadcastReceiver, intentFilter);

        updateUI();
    }


    public void updateUI( ) {


        MyBSActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                gridViewAdapter = new GridViewAdapter(MyBSActivity.instance, ((dataApplication) getApplication()));
                gridView.setAdapter(gridViewAdapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                        Log.d("bsdebug","Position_bs: "+position);

                        Notification n = ((dataApplication) getApplication()).getNotification().get(position);
                        NLService.currentNlservice.cancelNotification(((dataApplication) getApplication()).getStatusBarNotifications()[position].getKey());

                        if (n.contentIntent != null) {

                            try {
                                //Log.d("bsdebug","Send: "+n.contentIntent);
                                stateBar.setText("Application is running on front Screen.");
                                //nManger.cancel(((dataApplication) getApplication()).getNotificationIDs()[position]);
                                n.contentIntent.send();
                                //

                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        } else {
                            stateBar.setText("Untouchable notification, try to delete it.");
                            //nManger.cancel(((dataApplication) getApplication()).getNotificationIDs()[position]);

                        }

                    }
                });
            }
        });

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("bsdebug", "BS receive action: "+ intent.getAction());
            if(intent.getAction().equals(MainActivity.ACTION_REFRESH_NOTIFICATION)){
                //Log.d("bsdebug", "receive refresh command");
                updateUI();
            }

            /*if(intent.getAction().equals(MainActivity.ACTION_NOTISTUDY_POSTNOTIFICATION)) {
                //refreshListView();
            }
            if(intent.getAction().equals(MainActivity.ACTION_NOTISTUDY_REMOVENOTIFICATION)) {
                //refreshListView();
            }*/
        }
    };
    @Override
    public void onBSDestroy() {
        super.onBSDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }


}


