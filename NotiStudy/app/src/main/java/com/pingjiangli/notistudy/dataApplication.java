package com.pingjiangli.notistudy;

import android.app.Application;
import android.app.Notification;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Pingjiang.Li on 23/06/15.
 */
public class dataApplication extends Application {

    private ArrayList<Notification> listNotification;
    private ListView backScreenListView;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

    }
    public ArrayList<Notification> getNotification(){
        return listNotification;
    }
    public void updateNotification(ArrayList<Notification> ln){
        this.listNotification = (ArrayList<Notification>)ln.clone();
    }
    public ListView getBaclScreenListView(){
        return backScreenListView;
    }
    public void updateBaclScreenListView(ListView lv){
        this.backScreenListView = lv;
    }
}
