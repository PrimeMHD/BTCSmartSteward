package mhd.com.btcsmartsteward.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import mhd.com.btcsmartsteward.Activity.MainActivity;
import mhd.com.btcsmartsteward.Bean.BTCinfo;
import mhd.com.btcsmartsteward.NetworkUtil.HttpUtil;
import okhttp3.Call;
import okhttp3.Response;

public class UpdateDataService extends Service {


    private String responseData;
    static final int SERVICE_STOP=0;
    static final int SERVICE_PUSH_DATA=1;
    static final int START_WEBSOCKET_TRANS=3;
    static final String ACTION= "com.example.UpdataDataService";
    static final String TAG="轮询24小时交易情况服务";
    AlarmManager am;
    PendingIntent senderDelay;
    PendingIntent senderRepeating;



    public UpdateDataService() {
    }
    private BroadcastReceiver UpdateSignalReceiver=new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
            int doAction=intent.getIntExtra("WORK",-1);
            Log.d(TAG,"onReceive");
            switch(doAction){


                case SERVICE_PUSH_DATA:
                    HttpUtil.sendOkHttpRequest("https://api.huobipro.com/market/detail?symbol=btcusdt", new okhttp3.Callback(){
                        @Override
                        public void onResponse(Call call, Response response)throws IOException {
                            responseData=response.body().string();
                            Log.d(TAG,responseData);
                            String jsonStr;
                            if(responseData.contains("ok")){
                                jsonStr=responseData.substring(responseData.indexOf("\"tick\":")+"\"tick\":".length(),responseData.length()-1);
                                Log.d(TAG,jsonStr);
                                BTCinfo btcinfo=new HttpUtil().BTCinfo_parseJSONWithGSON(jsonStr);
                                Log.d(TAG,btcinfo.getHigh()+"");
                                PushDataImmediately(btcinfo);//return btcinfo to mainac through brc
                            }

                        }
                        @Override
                        public void onFailure(Call call,IOException e){
                            Log.d(TAG,e.toString());
                            Log.d(TAG,"onFail");
                            //异常处理
                        }
                    });
                    break;
                case SERVICE_STOP:
                    stopSelf();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        //instance=this;
        IntentFilter intentFilter=new IntentFilter(ACTION);
        registerReceiver(UpdateSignalReceiver,intentFilter);//动态注册服务中的广播接收机
        Log.d(TAG,"onCreate executed");
        pushDataRepeating();
    }

    @Override
    public void onDestroy(){
        //TODO
        cancelAlarmManager();

        Log.d(TAG,"onDestroy executed");
        super.onDestroy();
    }


    private void PushDataImmediately(BTCinfo btcinfo){
        //send broadcast to MainActivity
        Intent intent=new Intent();
        intent.setAction(MainActivity.ACTION_TRADE);
        intent.putExtra("btcinfo.volume",btcinfo.getVol());
        intent.putExtra("btcinfo.high", btcinfo.getHigh());
        intent.putExtra("btcinfo.low",btcinfo.getLow());
        intent.putExtra("btcinfo.open",btcinfo.getOpen());
        intent.putExtra("btcinfo.amount",btcinfo.getAmount());
        intent.putExtra("btcinfo.count",btcinfo.getCount());
        sendBroadcast(intent);
        Log.d(TAG,"sendbrtomain");

    }
    public void pushDataRepeating() {
        Intent intent = new Intent();
        intent.setAction(UpdateDataService.ACTION);
        intent.putExtra("WORK", UpdateDataService.SERVICE_PUSH_DATA);
        sendBroadcast(intent);
        //timer.schedule(task,0,1000);
       senderRepeating = PendingIntent.getBroadcast(this, 1, intent, 0);
        //开始时间
        long firstime= SystemClock.elapsedRealtime();
        am=(AlarmManager)getSystemService(ALARM_SERVICE);
        //2秒一个周期，不停的发送广播
        try {
            assert am != null;
            am.setRepeating(AlarmManager.RTC, 0, 1000, senderRepeating);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
     }

    public void cancelAlarmManager(){
        if (am != null) {
            if (senderDelay != null) {
                am.cancel(senderDelay);
                senderDelay = null;
            }
            if (senderRepeating != null) {
                am.cancel(senderRepeating);
                senderRepeating = null;
            }
            am = null;
        }
    }





}
