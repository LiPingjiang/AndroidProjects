package com.pli.yotaphone2.auxiliary;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.providers.Gravity_Provider;
import com.aware.providers.Locations_Provider;
import com.pli.yotaphone2.MainActivity;
import com.pli.yotaphone2.auxiliary.Data;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Pingjiang.Li on 05/07/15.
 */
public class DataThread extends Thread {
    private Context context;
    private BlockingQueue<Data> dataqueue;

    public DataThread(Context context, BlockingQueue<Data> queue) {
        this.context = context;
        dataqueue = queue;
    }

    public void run() {
        //sleep 1 seconds for data collecting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //collect sensor data
        Data data = new Data();

        //location
        Cursor latest_location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
        if( latest_location != null && latest_location.moveToFirst() ) {
            data.longitude = latest_location.getString(latest_location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE));
            data.altitude = latest_location.getString(latest_location.getColumnIndex(Locations_Provider.Locations_Data.ALTITUDE));
        }
        if( latest_location != null && ! latest_location.isClosed() ) latest_location.close();
        Log.d("NLService", "l: " + data.longitude + " a: " + data.altitude);

        //gravity - side on
        Cursor latest_gravity = context.getContentResolver().query(Gravity_Provider.Gravity_Data.CONTENT_URI, null, null, null, Gravity_Provider.Gravity_Data.TIMESTAMP + " DESC LIMIT 1");
        if( latest_gravity != null && latest_gravity.moveToFirst() ) {
            data.gravity = latest_gravity.getString(latest_gravity.getColumnIndex(Gravity_Provider.Gravity_Data.VALUES_2));
//            if( Double.parseDouble(data.gravity)>0)
//            {
////                whichSideOn=1;
//                Log.d("NLService","Front Screen. ");
//            }else if(Double.parseDouble(data.gravity)<0){
////                whichSideOn=2;
//                Log.d("NLService","Back Screen. ");
//            }else{
////                whichSideOn=0;
//                Log.d("NLService","Can't decide. ");
//            }
            Log.d("NLService","Gravity: " + data.gravity );

        }
        if( latest_gravity != null && ! latest_gravity.isClosed() ) latest_gravity.close();

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
        Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, false);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, false);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, false);
        context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));



    }
}