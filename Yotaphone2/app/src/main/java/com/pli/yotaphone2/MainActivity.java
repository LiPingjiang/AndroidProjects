package com.pli.yotaphone2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.Locations;
import com.aware.providers.Applications_Provider;
import com.aware.providers.ESM_Provider;
import com.aware.providers.Locations_Provider;
import com.pli.yotaphone2.auxiliary.Data;
import com.pli.yotaphone2.auxiliary.DataThread;
import com.pli.yotaphone2.auxiliary.ListViewAdapter;
import com.pli.yotaphone2.auxiliary.NotiSensor;
import com.pli.yotaphone2.auxiliary.Provider;
import com.pli.yotaphone2.service.NLService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainActivity extends Activity {

    public static String ACTION_NOTISTUDY_POSTNOTIFICATION = "com.pingjiangli.Notification.postnotification";
    public static String ACTION_NOTISTUDY_REMOVENOTIFICATION = "com.pingjiangli.Notification.removenotification";
    public static String ACTION_REFRESH_NOTIFICATION = "action_refresh_notification";
    public static String ACTION_CHECK_ESM = "action_check_esm";
    public static String ACTION_NOTISTUDY_ESM_COMPLETE = "com.pingjiangli.esm.complete";
    public static String ACTION_NOTISTUDY_ESM_START = "com.pingjiangli.esm.start";


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
    private NotiSensor sensors;
    private String esmAnswer[]=new String[7];

    private TextView title;

    public RadioButton c1;
    public RadioButton c2;
    public RadioButton c3;
    public RadioButton c4;
    public RadioButton c5;
    public Button bt;
    public int status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("mainactivitydebug", "create");

        instance = this;


        sensors  = new NotiSensor(this);
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
        intentFilter.addAction(ACTION_NOTISTUDY_ESM_START);
        intentFilter.addAction(ACTION_NOTISTUDY_ESM_COMPLETE);
        intentFilter.addAction(ACTION_CHECK_ESM);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mBroadcastReceiver, intentFilter);

        //initial Aware
//        Aware.setSetting(this, Aware_Preferences.STATUS_ESM, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_NOTIFICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_GRAVITY, false);
        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        //Aware.stopPlugin(this, "com.aware.plugin.google.activity_recognition");

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH)); //Ask AWARE to activate sensors

        changeUI("main");


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
    protected void onStart() {
        super.onStart();
        //Start Back Screen service
        Intent bsIntent = new Intent(this, MyBSActivity.class);
        this.startService(bsIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Aware.stopPlugin(this, "com.aware.plugin.google.activity_recognition");
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        unregisterReceiver(mBroadcastReceiver);
        Intent bsIntent = new Intent(this, MyBSActivity.class);
        this.stopService(bsIntent);

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
        Log.d("getListNotifications", i + "");
        dataApp.updateNotification(listNotification);
        dataApp.updateNotificationIDs(NotificationIDs);
        dataApp.updateNotificationPackages(NotificationPackages);
        dataApp.updateStatusBarNotifications(statusBarNotifications);

        Log.d("getListNotifications", "return true");
        return true;

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NOTIFYSTUDY", "Received: " + intent.getAction());


            if( intent.getAction().equals(ACTION_NOTISTUDY_ESM_START) ) {
                Log.d("NLService", "ACTION_ESM_QUEUE_STARTED");
                esmIsRunning=true;
            }
            if( intent.getAction().equals(ACTION_NOTISTUDY_ESM_COMPLETE) ) {
                Log.d("NLService","ACTION_ESM_QUEUE_COMPLETE");
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



                //Saving data to the ContentProvider
                ContentValues new_data = new ContentValues();
                new_data.put(Provider.NotiStudy_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                new_data.put(Provider.NotiStudy_Data.TIMESTAMP, System.currentTimeMillis());
                new_data.put(Provider.NotiStudy_Data.GRAVITY,data.gravity);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_NAME,data.activity_name);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_TYPE,data.activity_type);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_CONFIDENCE,data.activity_confidence);
                new_data.put(Provider.NotiStudy_Data.LOCATION_LONGITUDE,data.longitude);
                new_data.put(Provider.NotiStudy_Data.LOCATION_ALTITUDE,data.latitude);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_CATEGORY,notification.category);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_PRIORITY,notification.priority);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_VISIBILITY,notification.visibility);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_WHEN,notification.when);
                if(notification.extras.get(Notification.EXTRA_TEMPLATE)!=null)
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_TEMPLATE,(String)notification.extras.get(Notification.EXTRA_TEMPLATE));
                new_data.put(Provider.NotiStudy_Data.ESM_LOCATION, esmAnswer[1]);
                new_data.put(Provider.NotiStudy_Data.ESM_IDENTITY,esmAnswer[2]);
                new_data.put(Provider.NotiStudy_Data.ESM_IMPORTANCE,esmAnswer[3]);
                new_data.put(Provider.NotiStudy_Data.ESM_PRESURE,esmAnswer[4]);
                new_data.put(Provider.NotiStudy_Data.ESM_URGENCE,esmAnswer[5]);
                //new_data.put(Provider.NotiStudy_Data.ESM_PACKAGENAME,packagename);

                //Insert the data to the ContentProvider
                getContentResolver().insert(Provider.NotiStudy_Data.CONTENT_URI, new_data);


                //recheck the data queue
                Intent i = new Intent(MainActivity.ACTION_CHECK_ESM);
                sendBroadcast(i);

                //hide it
                if(runningTaskInfos != null){

                    i = new Intent(MainActivity.this,
                            runningTaskInfos.get(0).topActivity.getClass());
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                }

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_POSTNOTIFICATION)) {
                Log.d("NLService", "_________NEWNOTIFICATION_________");
                refreshListView();

            }
            if(intent.getAction().equals(ACTION_NOTISTUDY_REMOVENOTIFICATION)) {
                Log.d("NLService", "_________REMOVENOTIFICATION_________");


                //open sensor
                sensors.enableSensor();

                //use thread to collect data
                Thread thread = new DataThread(context,dataqueue,sensors);
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
    public void esm(Context context){
        changeUI("esm");
        Log.d("NOTIFYSTUDY","esm running");
    }
    BlockingQueue<Data> dataqueue = new ArrayBlockingQueue<Data>(30);
    ActivityManager activityManager;
    List<ActivityManager.RunningTaskInfo> runningTaskInfos;
    private void changeUI(String type){
        switch (type){
            case "main":{

                setContentView(R.layout.activity_main);
                listView = (ListView)findViewById(R.id.list_notifications);
                refreshListView();
                Log.d("mainactivitydebug","change to main,"+listView);
                break;
            }
            case "esm":{
                Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_START);
                sendBroadcast(i);

                //record the top activity
                ActivityManager activityManager =
                        (ActivityManager)(getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
                runningTaskInfos = activityManager.getRunningTasks(1) ;



                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                //start a thread ,after 2 minutes, stop esm

                setContentView(R.layout.esmfront);
                title= (TextView)    findViewById(R.id.title);

                c1= (RadioButton) findViewById(R.id.choice1);
                c2= (RadioButton) findViewById(R.id.choice2);
                c3= (RadioButton) findViewById(R.id.choice3);
                c4= (RadioButton) findViewById(R.id.choice4);
                c5= (RadioButton) findViewById(R.id.choice5);
                bt= (Button)      findViewById(R.id.button);
                bt .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        RadioGroup rg= (RadioGroup)  findViewById(R.id.radiogroup);
                            esmAnswer[status-1]=((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                        Log.d("mainactivitydebug","check text :"+ esmAnswer[status-1]);

                        rg.clearCheck();
                        updateESM();
                    }
                });

                status=1;
                updateESM();
                break;
            }
        }
    }

    private void updateESM()
    {
        switch (status) {
            case 1: {
                title.setText("Choose your location type.");
                c1.setText("Home");
                c2.setText("Work");
                c3.setText("University/School");
                c4.setText("Outdoor");
                c5.setText("Other");
                bt.setText("Next(1/5)");
                status = 2;
                break;
            }
            case 2: {
                title.setText("Are you alone or with someone?");
                c1.setText("Alone");
                c2.setText("With friends");
                c3.setText("With colleague");
                c4.setText("With strangers");
                c5.setText("Other");
                bt.setText("Next(2/5)");
                status = 3;
                break;
            }
            case 3: {
                title.setText("Will you feel uncomfortable when others see this notification?");
                c1.setText("Very uncomfortable");
                c2.setText("uncomfortable");
                c3.setText("Neither comfortable nor uncomfortable");
                c4.setText("Comfortable");
                c5.setText("Very comfortable");
                bt.setText("Next(3/5)");
                status = 4;
                break;
            }
            case 4: {
                title.setText("Is this notification important?");
                c1.setText("Very important");
                c2.setText("Important");
                c3.setText("Neither important nor unimportant");
                c4.setText("Unimportant");
                c5.setText("Very unimportant");
                bt.setText("Next(4/5)");
                status = 5;
                break;
            }
            case 5: {
                title.setText("Is this notification urgent?");
                c1.setText("Very urgent");
                c2.setText("Urgent");
                c3.setText("Neither urgent nor unurgent");
                c4.setText("Unurgent");
                c5.setText("Very unurgent");
                bt.setText("Confirm");
                status = 6;
                break;
            }
            case 6: {
                changeUI("main");
                Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_COMPLETE);
                sendBroadcast(i);
                status = 1;
                break;
            }
        }
    }

}
