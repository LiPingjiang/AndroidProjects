package com.pli.yotaphone2.auxiliary;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pli.yotaphone2.R;
import com.pli.yotaphone2.dataApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Pingjiang.Li on 22/06/15.
 */
public class GridViewAdapter extends BaseAdapter {

    private PackageManager pManager;
    private Context context;                        //运行上下文
    private List<Notification> listNotifications;    //商品信息集合
    private String[] NotificationPackages;
    private StatusBarNotification[] statusBarNotifications;
    private LayoutInflater gridContainer;           //视图容器
    public final class ListItemView{                //自定义控件集合
        public ImageView image;
        public TextView title;
        //public TextView info;
        //public TextView time;
    }



    public GridViewAdapter(Context context, dataApplication dataApp) {
        this.context = context;

        pManager = context.getPackageManager();
        gridContainer = LayoutInflater.from(context);   //创建视图容器并设置上下文

        this.listNotifications=dataApp.getNotification();
        this.NotificationPackages=dataApp.getNotificationPackages();
        this.statusBarNotifications=dataApp.getStatusBarNotifications();

    }

    public int getCount() {
    // TODO Auto-generated method stub
        if(listNotifications != null)
            return listNotifications.size();
        else return 0;
    }

    public Object getItem(int arg0) {
    // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int arg0) {
    // TODO Auto-generated method stub
        return 0;
    }

    /**
     * GridView Item设置
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //Log.e("method", "getView");

        ListItemView  gridItemView = null;
        if (convertView == null) {
            gridItemView = new ListItemView();

            convertView = gridContainer.inflate(R.layout.grid_notification, null);

            //convertView.findViewById(R.id.linerlayout).width

            gridItemView.image = (ImageView)convertView.findViewById(R.id.imagePackage);

            gridItemView.title = (TextView)convertView.findViewById(R.id.notificationTitle);
            //listItemView.info = (TextView)convertView.findViewById(R.id.infoItem);
            //listItemView.time = (TextView)convertView.findViewById(R.id.timeItem);

            convertView.setTag(gridItemView);
        }else {
            gridItemView = (ListItemView)convertView.getTag();
        }

        try {
            gridItemView.image.setBackground(context.createPackageContext(NotificationPackages[position], context.CONTEXT_INCLUDE_CODE
                    | Context.CONTEXT_IGNORE_SECURITY).getDrawable(listNotifications.get(position).icon));
        }catch(Exception ex) {
            Log.d("gridviewadapter", "Resource ID: " + listNotifications.get(position).icon + " ,exception: " + ex.getMessage());
        }

//        gridItemView.title.setText(listNotifications.get(
//                position).extras.get(Notification.EXTRA_TITLE).toString());

        ApplicationInfo ai;
        try {
            ai = pManager.getApplicationInfo( statusBarNotifications[position].getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        gridItemView.title.setText((String) (ai != null ? pManager.getApplicationLabel(ai) : "(unknown)"));



//        listItemView.info.setText(listNotifications.get(
//                position).extras.get(Notification.EXTRA_TEXT).toString());
        //listItemView.time.setText(listNotifications.get(position).extras.get(Notification.EXTRA_SHOW_WHEN).toString());
//        listItemView.time.setText(getDate(listNotifications.get(position).when));

        gridItemView.title.setTextColor(Color.BLACK);
//        listItemView.info.setTextColor(Color.BLACK);
//        listItemView.time.setTextColor(Color.BLACK);

        //gridItemView.title.getLayoutParams().width=340;
        gridItemView.image.getLayoutParams().width=90;
        gridItemView.image.getLayoutParams().height=90;
        gridItemView.title.setTextSize(7);
//        listItemView.time.setTextSize(10);
//        listItemView.info.setTextSize(8);
        gridItemView.title.setTypeface(null, Typeface.BOLD);

        return convertView;
    }
    public String getDate(long timeStamp) {
        if (timeStamp == 0) {
            return "";
        }
        timeStamp = timeStamp * 1000;
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        result = format.format(new Date(timeStamp));
        return result;
    }

}
