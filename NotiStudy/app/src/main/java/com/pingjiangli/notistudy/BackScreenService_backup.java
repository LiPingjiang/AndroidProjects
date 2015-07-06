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

import com.yotadevices.sdk.BSActivity;

import java.util.ArrayList;

/**
 * Created by Pingjiang.Li on 18/06/15.
 */
public class BackScreenService_backup extends BSActivity {


    static String ACTION_REFRESH_NOTIFICATION = "action_refresh_notification";

    private ListView listView;
    private ListViewAdapter listViewAdapter;
    //private ArrayList<Notification> listNotification= new ArrayList<>() ;
    public static BackScreenService_backup instance;

    @Override
    protected void onBSCreate() {
        Log.d("BackScreenNotification", "BS started!");
        super.onBSCreate();

        setBSContentView(R.layout.back_screen);

        listView = (ListView)findViewById(R.id.list_notifications_bs);

        instance = this;
        //((dataApplication) getApplication()).updateBaclScreenListView(listView);
        Log.d("bsdebug", "C listview:" + listView);

        //Initial();


    }
    /*@TargetApi(Build.VERSION_CODES.KITKAT)
    private void getListNotifications() {


        if(NLService.currentNlservice==null)
            return;
        if(listNotification==null){
            Log.d("bsdebug","listNotification is null");
            return;
        }
        listNotification.clear();
        int i=0;
        for (StatusBarNotification sbn : NLService.currentNlservice.getActiveNotifications()) {
            if(sbn.getNotification()!=null) {
                listNotification.add(i, sbn.getNotification());
                i++;
            }
        }
        Log.d("bsdebug",listNotification.toString());

    }*/
    private void Initial(){
        Log.d("bsdebug", "BS initial. ");

        instance = this;
        //LayoutInflater inflater = LayoutInflater.from(this);
        //View layout = inflater.inflate(R.layout.back_screen, null);

        Log.d("bsdebug", "listview:"+listView);

        mBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d("bsdebug", "BS receive action: "+ intent.getAction());
                if(intent.getAction().equals(MainActivity.ACTION_REFRESH_NOTIFICATION)){
                    Log.d("bsdebug", "refreshListView");
                    //Log.d("bsdebug", "Context:"+instance);

                    Log.d("bsdebug", "listview:"+listView);

                    listViewAdapter = new ListViewAdapter(instance,((dataApplication) getApplication()).getNotification());
                    //listView.setAdapter(listViewAdapter);
                    //refreshListView();
                }

                if(intent.getAction().equals(MainActivity.ACTION_NOTISTUDY_POSTNOTIFICATION)) {
                    //refreshListView();
                }
                if(intent.getAction().equals(MainActivity.ACTION_NOTISTUDY_REMOVENOTIFICATION)) {
                    //refreshListView();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_NOTISTUDY_POSTNOTIFICATION);
        intentFilter.addAction(MainActivity.ACTION_NOTISTUDY_REMOVENOTIFICATION);
        intentFilter.addAction(MainActivity.ACTION_REFRESH_NOTIFICATION);
        registerReceiver(mBroadcastReceiver, intentFilter);
        Log.d("bsdebug", "Finish register!"+mBroadcastReceiver.toString());
    }
    private void refreshListView(){
        Log.d("bsdebug", "refreshListView_start"+listView.toString());
        //getListNotifications();
        //listViewAdapter = new ListViewAdapter(this,listNotification); //create adapter
        listViewAdapter = new ListViewAdapter(this,((dataApplication) getApplication()).getNotification());
        listView.setAdapter(listViewAdapter);
        Log.d("bsdebug", "refreshListView_end");
    }

    private BroadcastReceiver mBroadcastReceiver ;

    @Override
    public void onBSSaveInstanceState(Bundle savedInstanceState){
        super.onBSSaveInstanceState(savedInstanceState);

    }
    @Override
    public void onBSRestoreInstanceState(Bundle savedInstanceState){
        super.onBSRestoreInstanceState(savedInstanceState);

    }
    @Override
    public void onBSResume(){
        super.onBSResume();
        Log.d("bsdebug", "BS onBSResume");

        Initial();

    }
    @Override
    public void onBSDestroy() {
        super.onBSDestroy();
        if(mBroadcastReceiver!=null)
            unregisterReceiver(mBroadcastReceiver);
    }

    public void updateList( final String s) {

        BackScreenService_backup.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

    }
}
