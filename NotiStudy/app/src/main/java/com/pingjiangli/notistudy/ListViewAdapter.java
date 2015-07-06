package com.pingjiangli.notistudy;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pingjiang.Li on 22/06/15.
 */
public class ListViewAdapter extends BaseAdapter {
    private Context context;                        //运行上下文
    //private List<Map<String, Object>> listItems;    //商品信息集合
    private List<Notification> listNotifications;    //商品信息集合
    private LayoutInflater listContainer;           //视图容器
    private boolean[] hasChecked;                   //记录商品选中状态
    public final class ListItemView{                //自定义控件集合
        public ImageView image;
        public TextView title;
        public TextView info;

    }



    public ListViewAdapter(Context context, List<Notification> listNotifications ) {
        this.context = context;
        listContainer = LayoutInflater.from(context);   //创建视图容器并设置上下文

        /*for(int i=0;i<listNotifications.size();i++){
            this.listNotifications.add(i, (Notification)listNotifications.get(i));
        }*/
        this.listNotifications=listNotifications;

        hasChecked = new boolean[getCount()];

    }

    public int getCount() {
    // TODO Auto-generated method stub
        return listNotifications.size();
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
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Log.e("method", "getView");
        final int selectID = position;
        //自定义视图
        ListItemView  listItemView = null;
        if (convertView == null) {
        listItemView = new ListItemView();
        //获取list_item布局文件的视图
        convertView = listContainer.inflate(R.layout.list_notification, null);
        //获取控件对象
        listItemView.image = (ImageView)convertView.findViewById(R.id.imageItem);
        listItemView.title = (TextView)convertView.findViewById(R.id.titleItem);
        listItemView.info = (TextView)convertView.findViewById(R.id.infoItem);
        //listItemView.detail= (Button)convertView.findViewById(R.id.detailItem);
        //设置控件集到convertView
        convertView.setTag(listItemView);
        }else {
        listItemView = (ListItemView)convertView.getTag();
        }

        try {
            listItemView.image.setBackgroundResource(listNotifications.get(position).icon);
        }catch(Exception ex) {
            Log.d("listviewadapter","Resource ID: "+listNotifications.get(position).icon+" ,exception: "+ex.getMessage());
            listItemView.image.setBackgroundResource(R.drawable.firefox);
        }

        listItemView.title.setText(listNotifications.get(
                position).extras.get(Notification.EXTRA_TITLE).toString());
        listItemView.title.setTextSize(20);
        listItemView.info.setText(listNotifications.get(
                position).extras.get(Notification.EXTRA_TEXT).toString());
        //listItemView.info.setTextSize(15);
        //listItemView.detail.setText("DETAIL INFO");
        //注册按钮点击时间爱你
        /*listItemView.detail.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //显示物品详情
            showDetailInfo(selectID);
        }
        });*/

        return convertView;
    }




}
