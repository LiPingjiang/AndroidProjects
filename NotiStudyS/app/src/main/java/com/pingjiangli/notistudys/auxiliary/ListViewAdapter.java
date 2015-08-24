package com.pingjiangli.notistudys.auxiliary;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pingjiangli.notistudys.R;
import com.pingjiangli.notistudys.dataApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Pingjiang.Li on 22/06/15.
 */
public class ListViewAdapter extends BaseAdapter {
    private Boolean isBackScreen;
    private Context context;                        //运行上下文
    //private List<Map<String, Object>> listItems;    //商品信息集合
    private List<Notification> listNotifications;    //商品信息集合
    private StatusBarNotification[] statusBarNotifications;
    private String[] NotificationPackages;
    private PackageManager pManager;
    private LayoutInflater listContainer;           //视图容器
    private boolean[] hasChecked;                   //记录商品选中状态
    public final class ListItemView{                //自定义控件集合
        public ImageView image;
        public TextView title;
        public TextView info;
        public TextView time;

    }



    public ListViewAdapter(Context context, dataApplication dataApp, Boolean isBackScreen) {
        this.context = context;
        this.isBackScreen = isBackScreen;

        pManager = context.getPackageManager();

        listContainer = LayoutInflater.from(context);   //创建视图容器并设置上下文

        /*for(int i=0;i<listNotifications.size();i++){
            this.listNotifications.add(i, (Notification)listNotifications.get(i));
        }*/
        this.listNotifications=dataApp.getNotification();
        this.NotificationPackages=dataApp.getNotificationPackages();
        this.statusBarNotifications=dataApp.getStatusBarNotifications();

        hasChecked = new boolean[getCount()];

    }

    public int getCount() {
    // TODO Auto-generated method stub
        if( listNotifications != null )
            return listNotifications.size();
        else
            return 0;
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
     * 判断物品是否选择
     * @param checkedID 物品序号
     * @return 返回是否选中状态
     */
    public boolean hasChecked(int checkedID) {
        return hasChecked[checkedID];
    }

    /**
     * 显示物品详情
     * @param clickID
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showDetailInfo(int clickID) {
        new AlertDialog.Builder(context)
        .setTitle("ID:" + clickID)
        //.setMessage(listNotifications.get(clickID).get("detail").toString())
        .setMessage(listNotifications.get(clickID).extras.get(Notification.EXTRA_INFO_TEXT).toString())
        .setPositiveButton("Confirm", null)
        .show();
    }


    /**
     * ListView Item设置
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.e("method", "getView");
        final int selectID = position;

        ListItemView  listItemView = null;
        if (convertView == null) {
            listItemView = new ListItemView();

            convertView = listContainer.inflate(R.layout.list_notification, null);

            listItemView.image = (ImageView)convertView.findViewById(R.id.imageItem);

            listItemView.title = (TextView)convertView.findViewById(R.id.titleItem);
            listItemView.info = (TextView)convertView.findViewById(R.id.infoItem);
            listItemView.time = (TextView)convertView.findViewById(R.id.timeItem);

            convertView.setTag(listItemView);
        }else {
            listItemView = (ListItemView)convertView.getTag();
        }

        try {
            listItemView.image.setBackgroundDrawable(context.createPackageContext(NotificationPackages[position], context.CONTEXT_INCLUDE_CODE
                    | Context.CONTEXT_IGNORE_SECURITY).getResources().getDrawable(listNotifications.get(position).icon));
//            listItemView.image.setImageBitmap(listNotifications.get(position).largeIcon);

        }catch(Exception ex) {
            Log.d("listviewadapter", "Resource ID: " + listNotifications.get(position).icon + " ,exception: " + ex.getMessage());
        }

        Log.d("listviewadapter", position + ": " + listNotifications.get(
                position).toString());

        try{
            listItemView.title.setText(listNotifications.get(
            position).extras.get(Notification.EXTRA_TITLE).toString());
        } catch ( Exception e)
        {
            listItemView.title.setText("Notification");
        }

        listItemView.title.setTextSize(20);
        try{
            listItemView.info.setText(listNotifications.get(
                    position).extras.get(Notification.EXTRA_TEXT).toString());
        } catch ( Exception e)
        {
            listItemView.info.setText("Nothing.");
        }

        listItemView.time.setText(getDate(listNotifications.get(position).when));

        listItemView.title.setTextColor(Color.BLACK);
        listItemView.info.setTextColor(Color.BLACK);
        listItemView.time.setTextColor(Color.BLACK);
        //if is back screen
        if(isBackScreen){

            listItemView.title.getLayoutParams().width=340;
            listItemView.image.getLayoutParams().width=80;
            listItemView.image.getLayoutParams().height=80;
            listItemView.title.setTextSize(13);
            listItemView.time.setTextSize(10);
            listItemView.info.setTextSize(8);
            listItemView.title.setTypeface(null, Typeface.BOLD);

        }

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
