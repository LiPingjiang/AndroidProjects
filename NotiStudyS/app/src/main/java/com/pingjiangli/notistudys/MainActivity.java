package com.pingjiangli.notistudys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.pingjiangli.notistudys.auxiliary.Data;
import com.pingjiangli.notistudys.auxiliary.ListViewAdapter;
import com.pingjiangli.notistudys.auxiliary.NotiSensor;
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
    public static String ACTION_NOTISTUDY_ESM_COMPLETE  = "com.pingjiangli.esm.complete";
    public static String ACTION_NOTISTUDY_ESM_START     = "com.pingjiangli.esm.start";

    private ListView listView;
    private ListViewAdapter listViewAdapter;
    private ArrayList<Notification> listNotification= new ArrayList<>() ;
    private dataApplication dataApp;
    private NotificationManager nManger;
    private Queue<StatusBarNotification> NotiQueue;
    private Boolean esmIsRunning=false;


    private NotiSensor sensors;
    private String esmAnswer[]=new String[7];

    private TextView title;
    private TextView content;
    public RadioButton c1;
    public RadioButton c2;
    public RadioButton c3;
    public RadioButton c4;
    public RadioButton c5;
    public Button bt_confirm;
    public Button bt_skip;
    public Button bt_nobother;
    public int status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isEnabledNotificationAccess())
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

        Log.d("mainactivitydebug", "create");
        //initial reference

        sensors  = new NotiSensor(this);
        listView = (ListView)findViewById(R.id.list_notifications);
        dataApp  = (dataApplication) getApplication();
        nManger  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotiQueue= new LinkedList<StatusBarNotification>();
        //initial broadcast receiver
        IntentFilter intentFilter = new IntentFilter();


        intentFilter.addAction(ACTION_NOTISTUDY_POSTNOTIFICATION);
        intentFilter.addAction(ACTION_NOTISTUDY_REMOVENOTIFICATION);
        intentFilter.addAction(ACTION_CHECK_ESM);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(ACTION_NOTISTUDY_ESM_START);
        intentFilter.addAction(ACTION_NOTISTUDY_ESM_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);


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
        Log.v("mainactivitydebug", "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
            }

    public void refreshListView(){
        dataApp.updateNotification(getListNotifications());
        listViewAdapter = new ListViewAdapter(this,listNotification); //create adapter
        listView.setAdapter(listViewAdapter);
        Intent i = new Intent(ACTION_REFRESH_NOTIFICATION);
//        i.putExtra("listNotification", (Parcelable) listNotification.clone());
        sendBroadcast(i);
        Log.d("bsdebug", "send from mainactivity");
        /*
        getListNotifications();

        listViewAdapter = new ListViewAdapter(this,dataApp,false); //create adapter
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        Intent i = new Intent(ACTION_REFRESH_NOTIFICATION);
        sendBroadcast(i);*/

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
    /*private boolean getListNotifications() {

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
        */

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
                StatusBarNotification sbn = NotiQueue.poll();//remove first one
                Notification notification = sbn.getNotification();
                Data data = null;
                try {
                    data = dataqueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }




                //Saving data to the ContentProvider
                ContentValues new_data = new ContentValues();
                new_data.put(Provider.NotiStudy_Data.DEVICE_ID, Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID));
                new_data.put(Provider.NotiStudy_Data.TIMESTAMP, System.currentTimeMillis());
                new_data.put(Provider.NotiStudy_Data.GRAVITY,data.gravity);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_NAME,data.activity_name);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_TYPE,data.activity_type);
                new_data.put(Provider.NotiStudy_Data.ACTIVITY_CONFIDENCE,data.activity_confidence);
                new_data.put(Provider.NotiStudy_Data.LOCATION_LONGITUDE,data.longitude);
                new_data.put(Provider.NotiStudy_Data.LOCATION_ALTITUDE,data.latitude);
                //api20 only
                if(Build.VERSION.SDK_INT>=20)
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_CATEGORY,notification.category);
                else
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_CATEGORY,"no category(api<20)");
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_PRIORITY,notification.priority);

                if(Build.VERSION.SDK_INT>=21)
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_VISIBILITY,notification.visibility);
                else
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_VISIBILITY,"no visibility(api<21)");


                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_WHEN,notification.when);
                if(notification.extras.get(Notification.EXTRA_TEMPLATE)!=null)
                    new_data.put(Provider.NotiStudy_Data.NOTIFICATION_TEMPLATE,(String)notification.extras.get(Notification.EXTRA_TEMPLATE));
                new_data.put(Provider.NotiStudy_Data.ESM_LOCATION, esmAnswer[1]);
                new_data.put(Provider.NotiStudy_Data.ESM_IDENTITY,esmAnswer[2]);
                new_data.put(Provider.NotiStudy_Data.ESM_PRESURE,esmAnswer[3]);
                new_data.put(Provider.NotiStudy_Data.ESM_IMPORTANCE,esmAnswer[4]);
                new_data.put(Provider.NotiStudy_Data.ESM_URGENCE,esmAnswer[5]);
                new_data.put(Provider.NotiStudy_Data.NOTIFICATION_PACKAGENAME, sbn.getPackageName());
                new_data.put(Provider.NotiStudy_Data.FRONT_SCREEN_ON,data.front_screen_on );

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
    private void changeUI(String type) {
        switch (type) {
            case "main": {

                setContentView(R.layout.activity_main);
                listView = (ListView) findViewById(R.id.list_notifications);
                refreshListView();
                Log.d("mainactivitydebug", "change to main," + listView);
                break;
            }
            case "esm": {
                Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_START);
                sendBroadcast(i);

                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                Log.d("updateESMmainactivity", "changeUI: esm");

                //start a thread ,after 2 minutes, stop esm
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(240000);
                            if (esmIsRunning) {
                                for (int j = status; j <= 6; j++) {
                                    esmAnswer[j - 1] = "NoAnswer";
                                }
                                status = 6;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateESM();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


                setContentView(R.layout.esmfront);
                title = (TextView) findViewById(R.id.title);
                content = (TextView) findViewById(R.id.content);

                c1 = (RadioButton) findViewById(R.id.choice1);
                c2 = (RadioButton) findViewById(R.id.choice2);
                c3 = (RadioButton) findViewById(R.id.choice3);
                c4 = (RadioButton) findViewById(R.id.choice4);
                c5 = (RadioButton) findViewById(R.id.choice5);
                bt_confirm = (Button) findViewById(R.id.button);
                bt_confirm.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        RadioGroup rg = (RadioGroup) findViewById(R.id.radiogroup);
                        esmAnswer[status - 1] = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                        Log.d("mainactivitydebug", "check text :" + esmAnswer[status - 1]);

                        rg.clearCheck();
                        updateESM();
                    }
                });
                bt_skip = (Button) findViewById(R.id.skip);
                bt_skip.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (esmIsRunning) {
                            for (int j = status; j <= 6; j++) {
                                esmAnswer[j - 1] = "NoAnswer";
                            }
                            status = 6;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateESM();
                                }
                            });
                        }
                    }
                });
                bt_nobother = (Button) findViewById(R.id.nobother);
                bt_nobother.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (esmIsRunning) {
                            for (int j = status; j <= 6; j++) {
                                esmAnswer[j - 1] = "NoAnswer";
                            }
                            status = 6;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateESM();
                                }
                            });
                        }
                        postponeESM=5;

                    }
                });

                status = 1;
                updateESM();
                break;
            }
        }
    }
        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void updateESM()
        {
            Log.d("updateESMmainactivity",String.valueOf(postponeESM));

            if(postponeESM>0){
                esmAnswer[1]="ESM postpone";
                esmAnswer[2]="ESM postpone";
                esmAnswer[3]="ESM postpone";
                esmAnswer[4]="ESM postpone";
                esmAnswer[5]="ESM postpone";

                postponeESM--;

//                if(status!=1)
                    status=6;

            }
            Log.d("updateESMmainactivity","run:"+String.valueOf(status));
            switch (status) {
                case 1: {
                    try{
                        content.setText(NotiQueue.peek().getNotification().extras.get(Notification.EXTRA_TITLE).toString()
                                +"\n"+NotiQueue.peek().getNotification().extras.get(Notification.EXTRA_TEXT).toString());
                    }catch(Exception e){
                        content.setText("");
                    }

                    title.setText("Choose your location type.");
                    c1.setText("Home");
                    c2.setText("Work");
                    c3.setText("University/School");
                    c4.setText("Outdoor");
                    c5.setText("Other");
                    bt_confirm.setText("Next(1/5)");
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
                    bt_confirm.setText("Next(2/5)");
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
                    bt_confirm.setText("Next(3/5)");
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
                    bt_confirm.setText("Next(4/5)");
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
                    bt_confirm.setText("Confirm");
                    status = 6;
                    break;
                }
                case 6: {
                    changeUI("main");
                    moveTaskToBack (true);
                    Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_COMPLETE);
                    sendBroadcast(i);
                    status = 1;
                    break;
                }
            }
        }
}
