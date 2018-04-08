package mhd.com.btcsmartsteward.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import mhd.com.btcsmartsteward.Activity.MainActivity;
import mhd.com.btcsmartsteward.Bean.BTCtrade;
import mhd.com.btcsmartsteward.Bean.KLinePin;
import mhd.com.btcsmartsteward.CommonUtils.StringUtil;
import mhd.com.btcsmartsteward.DatabaseTool.KLDatabaseHelper;
import mhd.com.btcsmartsteward.NetworkUtil.HttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SyncHistoryService extends Service {

    private boolean isSyncing=true;
    private boolean minfinish=false;
    private boolean dayfinish=false;
    private static OkHttpClient mOkHttpClient;
    private WebSocket mWebSocket;
    static final String wssAddress = "wss://api.huobipro.com/ws";
    static final String TAG="同步历史的服务";
    //static final String TAG="SyncHistoryService";
    public final String TOPIC_MARKET_KLINE_1min = "market.btcusdt.kline.1min";
    public final String TOPIC_MARKET_KLINE_1day = "market.btcusdt.kline.1day";
    private SQLiteDatabase kldb;
    private boolean finishInitWebsockt=false;

    private void initOKWebSocket() {
        mOkHttpClient=new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(wssAddress)
                .build();
        //建立连接
        mOkHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                mWebSocket=webSocket;
                finishInitWebsockt=true;
                Log.d(TAG,"WS OPEN");
                syncDBtoLatest();

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.d(TAG,"WS ON MESSAGE_text");
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                String responseData;
                String decompressedStr =new StringUtil().decompressForGzip(bytes);
                Log.d(TAG+"解压数据为", decompressedStr);
                //String tempSTR = decompressedStr.substring(decompressedStr.indexOf("\"status\":") + "\"status\":\"".length(), decompressedStr.indexOf("\"status\":") + "\"status\":\"".length() + 2);

                if (decompressedStr.contains("\"ping\":")) {
                    Log.d(TAG, "相应心跳");
                    responseData = decompressedStr.replace("ping", "pong");
                    Log.d(TAG,responseData);
                    webSocket.send(responseData);
                }//响应心跳包
                else if (decompressedStr.contains("error")) {
                    Log.e(TAG, "订阅错误！");
                    Log.e(TAG, "错误信息为" + decompressedStr);
                }//订阅失败的情况
                else{
                    Log.d(TAG, "收到有用信息");
                    String ch = decompressedStr.substring(decompressedStr.indexOf("\"rep\":") + "\"rep\":".length(), decompressedStr.indexOf(",\"status\":"));
                    if (ch.equals('\"' + TOPIC_MARKET_KLINE_1day + '\"')) {
                        int mLocationJsonStart = decompressedStr.indexOf("\"data\":") + "\"data\":".length();
                        String JsonStrConvertedFromDecompressedStr = decompressedStr.substring(mLocationJsonStart, decompressedStr.length() - 1);
                        Log.d(TAG + "转成json数据为", JsonStrConvertedFromDecompressedStr);
                        List<KLinePin> kLinePins = new HttpUtil().KLinePin_parseJSONArrayWithGSON(JsonStrConvertedFromDecompressedStr);
                        for (KLinePin kLinePin_1day : kLinePins) {
                            Log.d(TAG,"1day插入数据库,id:"+kLinePin_1day.getId());
                            if(kLinePin_1day!=null)
                                kldb.insert("kltable_1day", null, kLinePin_1day.getContentValues());
                        }

                        if (minfinish) {
                            sendBroadcast(new Intent().setAction(MainActivity.ACTION_UPFINISH));//告诉MainActivity已经同步好历史数据
                            Log.d(TAG,"同步历史数据完成");
                            mWebSocket.close(0,"enongh");
                            stopSelf();
                        }
                        dayfinish = true;

                    } else if (ch.equals('\"' + TOPIC_MARKET_KLINE_1min + '\"')) {
                        int mLocationJsonStart = decompressedStr.indexOf("\"data\":") + "\"data\":".length();
                        String JsonStrConvertedFromDecompressedStr = decompressedStr.substring(mLocationJsonStart, decompressedStr.length() - 1);
                        Log.d(TAG + "转成json数据为", JsonStrConvertedFromDecompressedStr);
                        List<KLinePin> kLinePins = new HttpUtil().KLinePin_parseJSONArrayWithGSON(JsonStrConvertedFromDecompressedStr);
                        for (KLinePin kLinePin_1min : kLinePins) {
                            Log.d(TAG,"1min插入数据库,id:"+kLinePin_1min.getId());
                            if(kLinePin_1min!=null)
                                kldb.insert("kltable_1min", null, kLinePin_1min.getContentValues());
                        }

                        if (dayfinish) {
                            sendBroadcast(new Intent().setAction(MainActivity.ACTION_UPFINISH));//告诉MainActivity已经同步好历史数据
                            Log.d(TAG,"同步历史数据完成");
                            mWebSocket.close(0,"enongh");
                            stopSelf();
                        }
                        minfinish = true;

                    }//处理返回数据


                }
            }
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d(TAG,"WS ON CLOSING");

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d(TAG,"WS ON CLOSED ");

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);

                Log.d(TAG,"WS ON FAILIAR ");

            }
        });
    }







    public SyncHistoryService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        KLDatabaseHelper klDatabaseHelper = new KLDatabaseHelper(this, "kline.db", null, 1);
        kldb= klDatabaseHelper.getWritableDatabase();
        initOKWebSocket();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"服务销毁成功");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void syncDBtoLatest(){
        long from_1day,from_1min;
        long to;
        String msg1="";
        String msg2="";
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault());
        Cursor cursor;
        cursor = kldb.query("kltable_1day", null, null, null, null, null, null);
        if (cursor!=null&&cursor.moveToLast()){
            from_1day=cursor.getLong(cursor.getColumnIndex("id"))+1;
            to= new Date().getTime();
            if(to/1000-from_1day>299*24*3600){
                msg2="{\n" +
                        "  \"req\": \""+ TOPIC_MARKET_KLINE_1day+"\",\n" +
                        "  \"id\": \"id10\",\n" +
                        "  \"from\":" +(to/1000-299*24*3600) +
                        "  ,\"to\": "+to/1000 +
                        "}";
            }
            else {
                msg2="{\n" +
                        "  \"req\": \""+ TOPIC_MARKET_KLINE_1day+"\",\n" +
                        "  \"id\": \"id10\",\n" +
                        "  \"from\":" +from_1day+
                        "  ,\"to\": "+to/1000 +
                        "}";
            }
            cursor.close();
            mWebSocket.send(msg2);

        }

        cursor = kldb.query("kltable_1min", null, null, null, null, null, null);
        if (cursor!=null&&cursor.moveToLast()){
            from_1min=cursor.getLong(cursor.getColumnIndex("id"))+1;
            to= new Date().getTime();

            if(to/1000-from_1min>299*60){
                msg1 ="{\n" +
                        "  \"req\": \""+ TOPIC_MARKET_KLINE_1min+"\",\n" +
                        "  \"id\": \"id10\",\n" +
                        "  \"from\":" +(to/1000-299*60)+
                        "  ,\"to\":"+to/1000 +
                        "}";
            }
            else{
                msg1 ="{\n" +
                        "  \"req\": \""+ TOPIC_MARKET_KLINE_1min+"\",\n" +
                        "  \"id\": \"id10\",\n" +
                        "  \"from\":" +from_1min+
                        "  ,\"to\":"+to/1000 +
                        "}";
            }
            cursor.close();
            mWebSocket.send(msg1);

        }
        Log.d(TAG,msg1);
        Log.d(TAG,msg2);
    }


}
