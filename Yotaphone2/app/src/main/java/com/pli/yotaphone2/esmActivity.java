package com.pli.yotaphone2;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by Pingjiang.Li on 15/07/15.
 */
public class esmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_START);
        sendBroadcast(i);

        setContentView(R.layout.esmfront);
        title= (TextView)    findViewById(R.id.title);

        c1= (RadioButton) findViewById(R.id.choice1);
        c2= (RadioButton) findViewById(R.id.choice2);
        c3= (RadioButton) findViewById(R.id.choice3);
        c4= (RadioButton) findViewById(R.id.choice4);
        c5= (RadioButton) findViewById(R.id.choice5);
        bt= (Button)      findViewById(R.id.button);
        bt .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioGroup rg= (RadioGroup)  findViewById(R.id.radiogroup);
                esmAnswer[status-1]=((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                Log.d("mainactivitydebug", "check text :" + esmAnswer[status - 1]);

                rg.clearCheck();
                updateESM();
            }
        });

        status=1;
        updateESM();
    }
    private void updateESM()
    {
        switch (status) {
            case 1: {
                title.setText("Choose your location type.");
                c1.setText("Home");
                c2.setText("Work");
                c3.setText("University/School");
                c4.setText("Outdoor");
                c5.setText("Other");
                bt.setText("Next(1/5)");
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
                bt.setText("Next(2/5)");
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
                bt.setText("Next(3/5)");
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
                bt.setText("Next(4/5)");
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
                bt.setText("Confirm");
                status = 6;
                break;
            }
            case 6: {
                changeUI("main");
                Intent i = new Intent(MainActivity.ACTION_NOTISTUDY_ESM_COMPLETE);
                sendBroadcast(i);
                status = 1;
                break;
            }
        }
    }
}
