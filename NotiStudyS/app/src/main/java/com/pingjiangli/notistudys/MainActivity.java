package com.pingjiangli.notistudys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.Locations;
import com.aware.providers.Applications_Provider;
import com.aware.providers.ESM_Provider;
import com.aware.providers.Locations_Provider;
import com.pingjiangli.notistudys.auxiliary.Data;
import com.pingjiangli.notistudys.auxiliary.DataThread;
import com.pingjiangli.notistudys.auxiliary.ListViewAdapter;
import com.pingjiangli.notistudys.auxiliary.Provider;
import com.pingjiangli.notistudys.service.NLService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class MainActivity extends Activity {

    public static String ACTION_NOTISTUDY_POSTNOTIFICATION = "com.pingjiangli.Notification.postnotification";
    public static String ACTION_NOTISTUDY_REMOVENOTIFICATION = "com.pingjiangli.Notification.removenotification";
    public static String ACTION_REFRESH_NOTIFICATION = "action_refresh_notification";
    public static String ACTION_CHECK_ESM = "action_check_esm";

    private ListView listView;
    private ListViewAdapter listViewAdapter;
    private ArrayList<Notification> listNotification= new ArrayList<>() ;
    private dataApplication dataApp;
    private NotificationManager nManger;
    private int[] NotificationIDs;
    private String[] NotificationPackages;
    private StatusBarNotification[] statusBarNotifications;
    public static MainActivity instance;
    private Queue<Notification> NotiQueue;
    private Boolean esmIsRunning=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isEnabledNotificationAccess())
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

        Log.d("mainactivitydebug", "create");
        //initial reference
        instance = this;
        listView = (ListView)findViewById(R.id.list_notifications);
        dataApp  = (dataApplication) getApplication();
        nManger  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotiQueue= new LinkedList<Notification>();
        //initial broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Locations.ACTION_AWARE_LOCATIONS);
        intentFilter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);
        //intentFilter.addAction(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);
        intentFilter.addAction(ESM.ACTION_AWARE_ESM_QUEUE_STARTED);
        intentFilter.addAction(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE);
        intentFilter.addAction(ACTION_NOTISTUDY_POSTNOTIFICATION);
        intentFilter.addAction(ACTION_NOTISTUDY_REMOVENOTIFICATION);
        intentFilter.addAction(ACTION_CHECK_ESM);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mBroadcastReceiver, intentFilter);

        //initial Aware
        Aware.setSetting(this, Aware_Preferences.STATUS_ESM, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_NOTIFICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_GRAVITY, false);
//        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        Aware.stopPlugin(this, "com.aware.plugin.google.activity_recognition");

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH)); //Ask AWARE to activate sensors

        refreshListView();

        startService(new Intent(MainActivity.this, NLService.class));

        Log.d("mainactivitydebug", "create finish");
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void refreshListView(){
        getListNotifications();

        listViewAdapter = new ListViewAdapter(this,dataApp,false); //create adapter
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                //Log.d("onclickinMAIN","click! " + position);
                Notification n = ((dataApplication) getApplication()).getNotification().get(position);
                NLService.currentNlservice.cancelNotification(((dataApplication) getApplication()).getStatusBarNotifications()[position].getKey());

                if (n.contentIntent != null) {

                    nManger.cancel(((dataApplication) getApplication()).getNotificationIDs()[position]);
                    try {
                        n.contentIntent.send();
                        Log.d("onclickinMAIN", "send!");

                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Untouchable notification, try to delete it.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Intent i = new Intent(ACTION_REFRESH_NOTIFICATION);
//        i.putExtra("listNotification", (Parcelable) listNotification.clone());
        sendBroadcast(i);
        //Log.d("bsdebug", "send from mainactivity");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean getListNotifications() {
        Log.d("getListNotifications", "getlistNotification");

        if(NLService.currentNlservice==null || NLService.currentNlservice.getActiveNotifications() == null) {
            Log.d("getListNotifications", "return");
            return false;
        }
        listNotification.clear();
        NotificationIDs  = new int[NLService.currentNlservice.getActiveNotifications().length+5];// maybe there is a sync problem, so next loop may crash because of out of boundry
        NotificationPackages = new String[NLService.currentNlservice.getActiveNotifications().length+5];
        statusBarNotifications = new StatusBarNotification[NLService.currentNlservice.getActiveNotifications().length];

        int i=0;
        for (StatusBarNotification sbn : NLService.currentNlservice.getActiveNotifications()) {
            if(sbn.getNotification()!=null) {
                listNotification.add(i, sbn.getNotification());
                statusBarNotifications[i]=sbn;
                NotificationIDs[i]=sbn.getId();
                NotificationPackages[i]=sbn.getPackageName();
                Log.d("getListNotifications", i + ": " + sbn.toString());
                i++;
            }
        }
        Log.d("getListNotifications",i+"");
        dataApp.updateNotification(listNotification);
        dataApp.updateNotificationIDs(NotificationIDs);
        dataApp.updateNotificationPackages(NotificationPackages);
        dataApp.updateStatusBarNotifications(statusBarNotifications);

        Log.d("getListNotifications", "return true");
        return true;

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NOTIFYSTUDY", "Received: " + intent.getAction());

            if(intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS)) {

            }
            if( intent.getAction().equals( Locations.ACTION_AWARE_LOCATIONS )) {
                Cursor latest_location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if( latest_location != null && latest_location.moveToFirst() ) {
                    Log.d("NOTIFYSTUDY", "Locations: "+ DatabaseUtils.dumpCursorToString(latest_location));
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

                esmIsRunning=true;

            }
            if( intent.getAction().equals(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE) ) {
                Log.d("NLService","ACTION_AWARE_ESM_QUEUE_COMPLETE");

                esmIsRunning=false;

                //currentNotification
                //Log.d("NLService","Notification: " + currentNotification.toString());
                Notification notification = NotiQueue.poll();//remove first one
                Data data = null;
                try {
                    data = dataqueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //esm
                String location = null;
                String identity = null;
                String presure = null;
                String importance = null;
                String urgence = null;
                String packagename=null;
//                String answer=null;
                Cursor latest_esm = context.getContentResolver().query(ESM_Provider.ESM_Data.CONTENT_URI, null, null, null,  "timestamp DESC LIMIT 5");
                if( latest_esm != null && latest_esm.moveToFirst() ) {
                    location = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));
                    packagename = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.TRIGGER));
//                    activity_type = latest_esm.getString(latest_esm.getColumnIndex("activity_type"));
//                    activity_confidence = latest_esm.getString(latest_esm.getColumnIndex("confidence"));
                    Log.d("esmMainActivity","location:"+ location);
                    if(latest_esm.moveToNext()){
                        identity = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));
                        Log.d("esmMainActivity","identity:"+ identity);
                        if(latest_esm.moveToNext()){
                            presure = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));
                            if(latest_esm.moveToNext()){
                                importance = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));
                                if(latest_esm.moveToNext()){
                                    urgence = latest_esm.getString(latest_esm.getColumnIndex(ESM_Provider.ESM_Data.ANSWER));
                                }
                            }
                        }
                    }
                }
                if( latest_esm != null && ! latest_esm.isClosed() ) latest_esm.close();

                //Saving data to the ContentProvider
                ContentValues new_data = new ContentValues();
                new_data.put(Provider.NotiStudy_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                new_data.put(Provider.NotiStudy_Data.TIMESTAMP, System.currentTimeMillis());
                new_data.put(Provider.NotiStudy_Data.GRAVITY,data.gravity);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_NAME,data.activity_name);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_TYPE,data.activity_type);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_CONFIDENCE,data.activity_confidence);
                new_data.put(Provider.NotiStudy_Data.LOCATION_LONGITUDE,data.longitude);
                new_data.put(Provider.NotiStudy_Data.LOCATION_ALTITUDE,data.altitude);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_CATEGORY,notification.category);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_PRIORITY,notification.priority);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_VISIBILITY,notification.visibility);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_WHEN,notification.when);
                if(notification.extras.get(Notification.EXTRA_TEMPLATE)!=null)
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_TEMPLATE,(String)notification.extras.get(Notification.EXTRA_TEMPLATE));
                new_data.put(Provider.NotiStudy_Data.ESM_LOCATION, location);
                new_data.put(Provider.NotiStudy_Data.ESM_IDENTITY,identity);
                new_data.put(Provider.NotiStudy_Data.ESM_IMPORTANCE,importance);
                new_data.put(Provider.NotiStudy_Data.ESM_PRESURE,presure);
                new_data.put(Provider.NotiStudy_Data.ESM_URGENCE,urgence);
                new_data.put(Provider.NotiStudy_Data.ESM_PACKAGENAME,packagename);

                //Insert the data to the ContentProvider
                getContentResolver().insert(Provider.NotiStudy_Data.CONTENT_URI, new_data);


                //recheck the data queue
                Intent i = new Intent(MainActivity.ACTION_CHECK_ESM);
                sendBroadcast(i);

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_POSTNOTIFICATION)) {
                Log.d("NLService", "_________NEWNOTIFICATION_________");
                refreshListView();

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_REMOVENOTIFICATION)) {
                Log.d("NLService", "_________REMOVENOTIFICATION_________");


                //open sensor
                //active all the needed sensor
                Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, true);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, true);
                Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, true);
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_GRAVITY,0 );
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);
                Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0);
                context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

                //use thread to collect data
                Thread thread = new DataThread(context,dataqueue);
                thread.start();

                NotiQueue.offer(((StatusBarNotification) intent.getExtras().get("StatusBarNotification")).getNotification());
                refreshListView();



            }
            if(intent.getAction().equals(ACTION_CHECK_ESM)) {
                Log.d("NLService", "_________NEW ESM_________");
                //if front screen is not locked
                KeyguardManager mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
                if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                    //keyguard is off
                    PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                    //font screen is on
                    if(pm.isScreenOn()){
                        //if dataqueue is not empty
                        if(dataqueue.size()>0 && !esmIsRunning)
                        {
                            esm(context);
                        }

                    }
                }

            }

            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                Log.d("mainactivityaction","screen is on");
            }
            if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                Log.d("mainactivityaction","user is present");
                //if dataqueue is not empty
                if(dataqueue.size()>0 && !esmIsRunning)
                {
                    esm(context);
                }
            }
        }

    };
    public static void esm(Context context)
    {
        Log.d("mainactivityesm", "call esm");
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
    BlockingQueue<Data> dataqueue = new ArrayBlockingQueue<Data>(30);

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private boolean isEnabledNotificationAccess() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
