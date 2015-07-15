package com.pingjiangli.esmtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;


public class MainActivity extends Activity {

    final String TAG = "esmtestmainactivity";
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt     = (Button)      findViewById(R.id.button);
        /*TextView title= (TextView)    findViewById(R.id.title);
        RadioButton c1= (RadioButton) findViewById(R.id.choice1);
        RadioButton c2= (RadioButton) findViewById(R.id.choice2);
        RadioButton c3= (RadioButton) findViewById(R.id.choice3);
        RadioButton c4= (RadioButton) findViewById(R.id.choice4);
        RadioButton c5= (RadioButton) findViewById(R.id.choice5);


        title.setText("Choose your location type.");
        c1.setText("Home");
        c2.setText("Work");
        c3.setText("University/School");
        c4.setText("Outdoor");
        c5.setText("Other");
        bt.setText("Next(1/5)");*/

        bt .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioGroup  rg= (RadioGroup)  findViewById(R.id.radiogroup);
                rg.clearCheck();
                updateUI();
            }
        });

        status=1;
        updateUI();

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
    }

    private void updateUI(){

        TextView title= (TextView)    findViewById(R.id.title);

        RadioButton c1= (RadioButton) findViewById(R.id.choice1);
        RadioButton c2= (RadioButton) findViewById(R.id.choice2);
        RadioButton c3= (RadioButton) findViewById(R.id.choice3);
        RadioButton c4= (RadioButton) findViewById(R.id.choice4);
        RadioButton c5= (RadioButton) findViewById(R.id.choice5);
        Button bt     = (Button)      findViewById(R.id.button);

        switch (status){
            case 1:{
                title.setText("Choose your location type.");
                c1.setText("Home");
                c2.setText("Work");
                c3.setText("University/School");
                c4.setText("Outdoor");
                c5.setText("Other");
                bt.setText("Next(1/5)");
                status=2;
                break;}
            case 2:{
                title.setText("Are you alone or with someone?");
                c1.setText("Alone");
                c2.setText("With friends");
                c3.setText("With colleague");
                c4.setText("With strangers");
                c5.setText("Other");
                bt.setText("Next(2/5)");
                status=3;
                break;}
            case 3:{
                title.setText("Will you feel uncomfortable when others see this notification?");
                c1.setText("Very uncomfortable");
                c2.setText("uncomfortable");
                c3.setText("Neither comfortable nor uncomfortable");
                c4.setText("Comfortable");
                c5.setText("Very comfortable");
                bt.setText("Next(3/5)");
                status=4;
                break;}
            case 4:{
                title.setText("Is this notification important?");
                c1.setText("Very important");
                c2.setText("Important");
                c3.setText("Neither important nor unimportant");
                c4.setText("Unimportant");
                c5.setText("Very unimportant");
                bt.setText("Next(4/5)");
                status=5;
                break;}
            case 5:{
                title.setText("Is this notification urgent?");
                c1.setText("Very urgent");
                c2.setText("Urgent");
                c3.setText("Neither urgent nor unurgent");
                c4.setText("Unurgent");
                c5.setText("Very unurgent");
                bt.setText("Confirm");
                status=1;
                break;}

        }

    }
}
