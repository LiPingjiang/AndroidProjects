package com.pli.yotaphone2;

import android.app.Application;
import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Pingjiang.Li on 23/06/15.
 */
public class dataApplication extends Application {

    private ArrayList<Notification> listNotification;
    private int[] NotificationIDs;
    private String[] NotificationPackages;
    private StatusBarNotification[] statusBarNotifications;
    private double realtime_Gravity;

    @Override
    public void onCreate() {
        super.onCreate();

    }
    public ArrayList<Notification> getNotification(){
        return listNotification;
    }
    public void updateNotification(ArrayList<Notification> ln){
        this.listNotification = (ArrayList<Notification>)ln.clone();
    }
    public int[] getNotificationIDs(){
        return NotificationIDs;
    }
    public void updateNotificationIDs(int[] IDs){
        this.NotificationIDs = IDs;
    }
    public String[] getNotificationPackages(){
        return NotificationPackages;
    }
    public void updateNotificationPackages(String[] Packages){
        this.NotificationPackages = Packages;
    }
    public StatusBarNotification[] getStatusBarNotifications(){
        return statusBarNotifications;
    }
    public void updateStatusBarNotifications(StatusBarNotification[] sbns){
        this.statusBarNotifications = sbns;
    }
    public double getGravity(){
        return realtime_Gravity;
    }
    public void setGravity(double g){
        realtime_Gravity=g;
    }
}
