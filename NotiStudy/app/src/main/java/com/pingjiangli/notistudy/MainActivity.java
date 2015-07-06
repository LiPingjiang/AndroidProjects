package com.pingjiangli.notistudy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.Locations;
import com.aware.providers.Applications_Provider;
import com.aware.providers.Gravity_Provider;
import com.aware.providers.Locations_Provider;

import java.io.Serializable;
import java.util.ArrayList;


public class MainActivity extends Activity{

    static String ACTION_NOTISTUDY_POSTNOTIFICATION = "com.pingjiangli.Notification.postnotification";
    static String ACTION_NOTISTUDY_REMOVENOTIFICATION = "com.pingjiangli.Notification.removenotification";
    static String ACTION_REFRESH_NOTIFICATION = "action_refresh_notification";

    private ListView listView;
    private dataApplication dataApp;

    private ListViewAdapter listViewAdapter;


    /***
     * whichSideOn Value:
     * -1;  Not decided
     * 0:   Not enough data in list to
     * 1:   Front is on
     * 2:   Back is on
     * 3:   Moving
     * */
    int whichSideOn;
    Notification currentNotification;
    private ArrayList<Notification> listNotification= new ArrayList<>() ;
    BackScreenService bss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataApp = (dataApplication) getApplication();

        listView = (ListView)findViewById(R.id.list_notifications);

        Aware.setSetting(this, Aware_Preferences.STATUS_ESM, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_NOTIFICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_GRAVITY, false);
//        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        Aware.stopPlugin(this, "com.aware.plugin.google.activity_recognition");
        //Aware.setSetting(this, , true);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH)); //Ask AWARE to activate sensors

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Locations.ACTION_AWARE_LOCATIONS);
        intentFilter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);
        intentFilter.addAction(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);
        intentFilter.addAction(ESM.ACTION_AWARE_ESM_QUEUE_STARTED);
        intentFilter.addAction(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE);
        intentFilter.addAction(ACTION_NOTISTUDY_POSTNOTIFICATION);
        intentFilter.addAction(ACTION_NOTISTUDY_REMOVENOTIFICATION);

        registerReceiver(mBroadcastReceiver, intentFilter);

        startService(new Intent(MainActivity.this, NLService.class));
        //startService(new Intent(MainActivity.this, BackScreenService.class));
        //bindService(new Intent(MainActivity.this, BackScreenService.class),conn, Context.BIND_AUTO_CREATE);


        //refresh list view
        refreshListView();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //Start Back Screen service
        Intent bsIntent = new Intent(this, BackScreenService.class);
        this.startService(bsIntent);
    }

    public void refreshListView(){
        dataApp.updateNotification(getListNotifications());
        listViewAdapter = new ListViewAdapter(this,listNotification); //create adapter
        listView.setAdapter(listViewAdapter);
        Intent i = new Intent(ACTION_REFRESH_NOTIFICATION);
//        i.putExtra("listNotification", (Parcelable) listNotification.clone());
        sendBroadcast(i);
        Log.d("bsdebug", "send from mainactivity");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private ArrayList<Notification> getListNotifications() {

        if(NLService.currentNlservice==null || NLService.currentNlservice.getActiveNotifications() == null)
            return listNotification;
        listNotification.clear();

        int i=0;
        for (StatusBarNotification sbn : NLService.currentNlservice.getActiveNotifications()) {
            if(sbn.getNotification()!=null) {
                listNotification.add(i, sbn.getNotification());
                i++;
            }
        }
        return listNotification;

    }

    public static void esm(Context context)
    {
        Intent i = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);

        String esm_location =
                "{'esm': {" +
                "'esm_type': " + ESM.TYPE_ESM_RADIO + ", " +
                "'esm_title': 'Location', " +
                "'esm_instructions': 'Choose your location type.', " +
                "'esm_radios':['Home','Work','University/School','Outdoor','Other'],"+
                "'esm_submit': 'Next(1/5)', " +
                "'esm_expiration_threashold': 120, " +
                "'esm_trigger': '"+ context.getPackageName() +"' }}";
        String esm_identity =
                "{'esm': {" +
                "'esm_type': " + ESM.TYPE_ESM_RADIO + ", " +
                "'esm_title': 'Identity', " +
                "'esm_instructions': 'Are you alone or with someone?', " +
                "'esm_radios':['Alone','With friends','With strangers','Other'],"+
                "'esm_submit': 'Next(2/5)', " +
                "'esm_expiration_threashold': 120, " +
                "'esm_trigger': '"+ context.getPackageName() +"' }}";
        String esm_presure =
                "{'esm': {" +
                "'esm_type': " + ESM.TYPE_ESM_LIKERT + ", " +
                "'esm_title': 'Presure', " +
                "'esm_instructions': 'Will you feel uncomfortable when others see this notification?', " +
                "'esm_likert_max': 5,"+
                "'esm_likert_max_label': 'Uncomfortable'," +
                "'esm_likert_min_label': 'Comfortable',"+
                "'esm_submit': 'Next(3/5)', " +
                "'esm_expiration_threashold': 120, " +
                "'esm_trigger': '"+ context.getPackageName() +"' }}";
        String esm_importance =
                "{'esm': {" +
                "'esm_type': " + ESM.TYPE_ESM_LIKERT + ", " +
                "'esm_title': 'Inportance', " +
                "'esm_instructions': 'Is this notification important?', " +
                "'esm_likert_max': 5,"+
                "'esm_likert_max_label': 'Very important'," +
                "'esm_likert_min_label': 'Not important',"+
                "'esm_submit': 'Next(4/5)', " +
                "'esm_expiration_threashold': 120, " +
                "'esm_trigger': '"+ context.getPackageName() +"' }}";
        String esm_urgence =
                "{'esm': {" +
                "'esm_type': " + ESM.TYPE_ESM_LIKERT + ", " +
                "'esm_title': 'Urgence', " +
                "'esm_instructions': 'Is this notification urgent?', " +
                "'esm_likert_max': 5,"+
                "'esm_likert_max_label': 'Very urgent'," +
                "'esm_likert_min_label': 'Not urgent',"+
                "'esm_submit': 'Done(5/5)', " +
                "'esm_expiration_threashold': 120, " +
                "'esm_trigger': '"+ context.getPackageName() +"' }}";
        String esm_str ="["+esm_location+","+esm_identity+","+esm_presure+","+esm_importance+","+esm_urgence+"]";
        //Ask AWARE to show question
        i.putExtra(ESM.EXTRA_ESM, esm_str);
        context.sendBroadcast(i);

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NOTIFYSTUDY", "Received: " + intent.getAction());

            if(intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS)) {

            }
            if( intent.getAction().equals( Locations.ACTION_AWARE_LOCATIONS )) {
                Cursor latest_location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if( latest_location != null && latest_location.moveToFirst() ) {
                    Log.d("NOTIFYSTUDY", "Locations: "+DatabaseUtils.dumpCursorToString(latest_location));
                }
                if( latest_location != null && ! latest_location.isClosed() ) latest_location.close();
            }
            if( intent.getAction().equals( Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND )){
                Cursor latest_app = context.getContentResolver().query(Applications_Provider.Applications_Foreground.CONTENT_URI, null, null, null, Applications_Provider.Applications_Foreground.TIMESTAMP + " DESC LIMIT 1");
                if( latest_app != null && latest_app.moveToFirst() ) {
                    Log.d("NOTIFYSTUDY", "Applications: "+DatabaseUtils.dumpCursorToString(latest_app));
                }
                if( latest_app != null && ! latest_app.isClosed() ) latest_app.close();
            }
            if( intent.getAction().equals(ESM.ACTION_AWARE_ESM_QUEUE_STARTED) ) {
                Log.d("NLService", "ACTION_AWARE_ESM_QUEUE_STARTED");

                //active all the needed sensor
                Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, true);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, true);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, true);
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_GRAVITY,100000 );
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0);
                context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
            }
            if( intent.getAction().equals(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE) ) {
                Log.d("NLService","ACTION_AWARE_ESM_QUEUE_COMPLETE");

                //get data
                String longitude = new String();
                String altitude = new String();  //location
                int onSide;                 //which side is on
                Notification noti;          //notification
                String activity_name = new String();
                String activity_type = new String();
                String activity_confidence = new String();;
                //application usage

                //location
                Cursor latest_location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if( latest_location != null && latest_location.moveToFirst() ) {
                    longitude = latest_location.getString(latest_location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE));
                    altitude = latest_location.getString(latest_location.getColumnIndex(Locations_Provider.Locations_Data.ALTITUDE));
                }
                if( latest_location != null && ! latest_location.isClosed() ) latest_location.close();
                Log.d("NLService","l: "+longitude+" a: "+altitude);

                //gravity - side on
                Cursor latest_gravity = context.getContentResolver().query(Gravity_Provider.Gravity_Data.CONTENT_URI, null, null, null, Gravity_Provider.Gravity_Data.TIMESTAMP + " DESC LIMIT 1");
                if( latest_gravity != null && latest_gravity.moveToFirst() ) {
                    String g = latest_gravity.getString(latest_gravity.getColumnIndex(Gravity_Provider.Gravity_Data.VALUES_2));
                    if( Double.parseDouble(g)>0)
                    {
                        whichSideOn=1;
                        Log.d("NLService","Front Screen. ");
                    }else if(Double.parseDouble(g)<0){
                        whichSideOn=2;
                        Log.d("NLService","Back Screen. ");
                    }else{
                        whichSideOn=0;
                        Log.d("NLService","Can't decide. ");
                    }

                }
                if( latest_gravity != null && ! latest_gravity.isClosed() ) latest_gravity.close();

                //google activity
                Uri url = Uri.parse("content://com.aware.plugin.google.activity_recognition.provider/plugin_google_activity_recognition");
                Cursor latest_activity = context.getContentResolver().query(url, null, null, null,  "timestamp DESC LIMIT 1");
                if( latest_activity != null && latest_activity.moveToFirst() ) {
                    activity_name = latest_activity.getString(latest_activity.getColumnIndex("activity_name"));
                    activity_type = latest_activity.getString(latest_activity.getColumnIndex("activity_type"));
                    activity_confidence = latest_activity.getString(latest_activity.getColumnIndex("confidence"));

                }
                if( latest_activity != null && ! latest_activity.isClosed() ) latest_activity.close();
                Log.d("NLService",activity_name+" , "+activity_confidence);


                //currentNotification
                Log.d("NLService","Notification: " + currentNotification.toString());


                //close sensor
                Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, false);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, false);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, false);
                context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_POSTNOTIFICATION)) {
                Log.d("NLService", "_________NEWNOTIFICATION_________");
                refreshListView();

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_REMOVENOTIFICATION)) {
                Log.d("NLService", "_________REMOVENOTIFICATION_________ will call esm ");

                currentNotification= ((StatusBarNotification)intent.getExtras().get("StatusBarNotification")).getNotification();
                refreshListView();
                //start esm
                //esm(context);
            }
        }

    };

    /*private ServiceConnection conn = new ServiceConnection() {
        // 获取服务对象时的操作
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            bss = ((BackScreenService.ServiceBinder) service).getService();

        }

        // 无法获取到服务对象时的操作
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            bss = null;
        }

    };*/



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(conn);
        unregisterReceiver(mBroadcastReceiver);
    }
}
