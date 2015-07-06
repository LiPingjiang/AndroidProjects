package com.pingjiangli.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.app.PendingIntent;


public class MainActivity extends ActionBarActivity {

    private Button sendNotiBt;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendNotiBt = (Button) findViewById(R.id.sendNotiBt);
        sendNotiBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showDefaultNotification();
            }
        });

    }
    // 默认显示的的Notification
    private void showDefaultNotification() {
        // 定义Notication的各种属性
        CharSequence title = "i am new";
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification noti = new Notification(icon, title, when + 10000);
        noti.flags = Notification.FLAG_INSISTENT;

        // 创建一个通知
        Notification mNotification = new Notification();

        // 设置属性值
        mNotification.icon = R.drawable.icon;
        mNotification.tickerText = "NotificationTest";
        mNotification.when = System.currentTimeMillis(); // 立即发生此通知

        // 带参数的构造函数,属性值如上
        // Notification mNotification = = new Notification(R.drawable.icon,"NotificationTest", System.currentTimeMillis()));

        // 添加声音效果
        mNotification.defaults |= Notification.DEFAULT_SOUND;

        // 添加震动,后来得知需要添加震动权限 : Virbate Permission
        //mNotification.defaults |= Notification.DEFAULT_VIBRATE ;

        //添加状态标志

        //FLAG_AUTO_CANCEL          该通知能被状态栏的清除按钮给清除掉
        //FLAG_NO_CLEAR                 该通知能被状态栏的清除按钮给清除掉
        //FLAG_ONGOING_EVENT      通知放置在正在运行
        //FLAG_INSISTENT                通知的音乐效果一直播放
        mNotification.flags = Notification.FLAG_INSISTENT ;

        //将该通知显示为默认View
        PendingIntent contentIntent = PendingIntent.getActivity
                (MainActivity.this, 0,new Intent("android.settings.SETTINGS"), 0);
        mNotification.setLatestEventInfo(MainActivity.this, "通知类型：默认View", "一般般哟。。。。",contentIntent);

        // 设置setLatestEventInfo方法,如果不设置会App报错异常
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //注册此通知
        // 如果该NOTIFICATION_ID的通知已存在，会显示最新通知的相关信息 ，比如tickerText 等
        mNotificationManager.notify(2, mNotification);

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
}
