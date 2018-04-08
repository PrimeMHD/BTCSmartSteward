package mhd.com.btcsmartsteward.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mhd.com.btcsmartsteward.R;


public class AlarmActivity extends AppCompatActivity {

    private TextView alarmtext;
    private Button OKIkonw;
    private MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmtext=(TextView)findViewById(R.id.alarm_text);
        OKIkonw=(Button)findViewById(R.id.OKiknow);
        Intent intent=this.getIntent();
        if(intent.getCharExtra("AlarmType",'Z')=='H'){
            mp=MediaPlayer.create(this,R.raw.happy);
            mp.start();
            alarmtext.setText("超过预设高价");
        }
        else if(intent.getCharExtra("AlarmType",'Z')=='L'){
            alarmtext.setText("低于预设低价");
            mp=MediaPlayer.create(this,R.raw.tank);
            mp.start();
        }
        else if(intent.getCharExtra("AlarmType",'Z')=='B'){
            alarmtext.setText("准备抄底");
            mp=MediaPlayer.create(this,R.raw.happy);
            mp.start();
        }
        else if(intent.getCharExtra("AlarmType",'Z')=='T'){
            alarmtext.setText("止盈回调");
            mp=MediaPlayer.create(this,R.raw.tank);
            mp.start();
        }

        OKIkonw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mp!=null){
            mp.stop();
            mp.release();
            mp=null;
        }
    }
}
