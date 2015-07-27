package com.pingjiangli.notistudys.auxiliary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;


import com.pingjiangli.notistudys.MainActivity;
import com.pingjiangli.notistudys.dataApplication;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Pingjiang.Li on 05/07/15.
 */
public class DataThread extends Thread {
    private Context context;
    private BlockingQueue<Data> dataqueue;
    private NotiSensor sensors;

    public DataThread(Context context, BlockingQueue<Data> queue, NotiSensor sens) {
        this.context = context;
        dataqueue = queue;
        sensors = sens;
    }

    public void run() {
        //sleep 1 seconds for data collecting
        //collect sensor data
        Data data = new Data();
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        data.front_screen_on=String.valueOf(pm.isScreenOn());
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dataApplication dataApp = (dataApplication) ((Activity) context).getApplication();


        //通过network获取location
        String networkProvider = LocationManager.NETWORK_PROVIDER;
        //通过gps获取location
        String GpsProvider = LocationManager.GPS_PROVIDER;
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Location location = locationManager.getLastKnownLocation(GpsProvider);
            if(location != null){
                data.latitude = String.valueOf(location.getLatitude());
                data.longitude = String.valueOf(location.getLongitude());
            }else {
                location = locationManager.getLastKnownLocation(networkProvider);
                if(location != null){
                    data.latitude = String.valueOf(location.getLatitude());
                    data.longitude = String.valueOf(location.getLongitude());
                }else {
                    data.latitude = "null";
                    data.longitude = "null";
                }
            }
        }else {
            data.latitude = "null";
            data.longitude = "null";
        }

        data.gravity= String.valueOf(dataApp.getGravity());

        //google activity
        Uri url = Uri.parse("content://com.aware.plugin.google.activity_recognition.provider/plugin_google_activity_recognition");
        Cursor latest_activity = context.getContentResolver().query(url, null, null, null,  "timestamp DESC LIMIT 1");
        if( latest_activity != null && latest_activity.moveToFirst() ) {
            data.activity_name = latest_activity.getString(latest_activity.getColumnIndex("activity_name"));
            data.activity_type = latest_activity.getString(latest_activity.getColumnIndex("activity_type"));
            data.activity_confidence = latest_activity.getString(latest_activity.getColumnIndex("confidence"));

        }
        if( latest_activity != null && ! latest_activity.isClosed() ) latest_activity.close();
        Log.d("NLService",data.activity_name+" , "+data.activity_confidence);

        //save the data to queue
        try {
            dataqueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //send broadcast to start ESM
        Intent i = new Intent(MainActivity.ACTION_CHECK_ESM);
        context.sendBroadcast(i);

        //close sensor
        sensors.disableSensor();
    }
}