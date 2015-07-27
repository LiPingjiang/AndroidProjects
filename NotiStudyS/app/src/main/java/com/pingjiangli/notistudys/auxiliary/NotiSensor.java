package com.pingjiangli.notistudys.auxiliary;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.pingjiangli.notistudys.dataApplication;


/**
 * Created by Pingjiang.Li on 13/07/15.
 */
public class NotiSensor implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor sensor;
    private float mLastX, mLastY, mLastZ;
    private Context mContext;
    dataApplication dataApp;
    public NotiSensor(Context context) {
        mContext = context;
        dataApp = (dataApplication) ((Activity) context).getApplication();
    }

    public void enableSensor() {
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mSensorManager == null) {
            Log.v("sensor..", "Sensors not supported");
        }

        mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);


    }

    public void disableSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
            mLastZ = event.values[SensorManager.DATA_Z];
            String sX = String.valueOf(mLastX);
            String sY = String.valueOf(mLastY);
            String sZ = String.valueOf(mLastZ);

            dataApp.setGravity(mLastZ);

            Log.v("mLastX==", sX);
            Log.v("mLastY==", sY);
            Log.v("mLastZ==", sZ);//GRAVITY
        }
    }

}
