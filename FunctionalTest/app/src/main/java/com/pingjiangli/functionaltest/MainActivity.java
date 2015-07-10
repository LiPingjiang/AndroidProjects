package com.pingjiangli.functionaltest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    private String TAG = "Functionaltest";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");

        context = this;


        //open sensor
        //active all the needed sensor
        Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, true);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, true);
        Aware.setSetting(context, Aware_Preferences.FREQUENCY_GRAVITY,0 );
        Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);
        Aware.setSetting(context, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0);
        context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

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

        Log.d(TAG,"onDestory");

        //close sensor
        Aware.setSetting(context, Aware_Preferences.STATUS_GRAVITY, false);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_GPS, false);
        Aware.setSetting(context, Aware_Preferences.STATUS_LOCATION_NETWORK, false);
        context.sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }
}
