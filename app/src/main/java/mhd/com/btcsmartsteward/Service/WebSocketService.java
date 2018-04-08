package mhd.com.btcsmartsteward.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import mhd.com.btcsmartsteward.Activity.MainActivity;
import mhd.com.btcsmartsteward.Bean.BTCtrade;
import mhd.com.btcsmartsteward.Bean.KLinePin;
import mhd.com.btcsmartsteward.CommonUtils.StringUtil;
import mhd.com.btcsmartsteward.DatabaseTool.KLDatabaseHelper;
import mhd.com.btcsmartsteward.DatabaseTool.TradeDetailDatabaseHelper;
import mhd.com.btcsmartsteward.NetworkUtil.HttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import okio.GzipSource;

public class WebSocketService extends Service {


    static final String wssAddress = "wss://api.huobipro.com/ws";
    static final String TAG = "WebSocket实时更新服务";
    public final String TOPIC_MARKET_KLINE_1min = "market.btcusdt.kline.1min";
    public final String TOPIC_MARKET_KLINE_1day = "market.btcusdt.kline.1day";
    public final String TOPIC_TRADE_DETAIL = "market.btcusdt.trade.detail";

    public enum KlinePeriod {day, min};


    private static OkHttpClient mOkHttpClient;
    private static WebSocket mWebSocket;
    private OnTradingUpdateListener onTradingUpdateListener;
    private OnKlinePinUpdateListener onKlinePinUpdateListener;


    private SQLiteDatabase tradedb;
    private SQLiteDatabase kldb;
    private KLDatabaseHelper klDatabaseHelper;
    private TradeDetailDatabaseHelper tradeDetailDatabaseHelper;


    private KLinePin mKLinePin;
    private BTCtrade mBtCtrade;
    private static Map<String, String> topicDic = new LinkedHashMap<String, String>();
    private static boolean isOpened = false;

    private BTCtrade btctrade;

    private KLinePin kLinePin_1day_everychange;
    private KLinePin kLinePin_1min_everychange;
    private KLinePin kLinePin_1day_everychange_OLD;
    private KLinePin kLinePin_1min_everychange_OLD;
    private boolean shouldInitOLD_1day=true;
    private boolean shouldInitOLD_1min=true;
    private RetInfoBinder mBinder = new RetInfoBinder();
    private Handler handler = new Handler();


    public void setOnTradingUpdateListener(OnTradingUpdateListener onTradingUpdateListener) {
        this.onTradingUpdateListener = onTradingUpdateListener;
    }//注册回调接口的方法，供外部调用

    public void setOnKlinePinUpdateListener(OnKlinePinUpdateListener onKlinePinUpdateListener) {
        this.onKlinePinUpdateListener = onKlinePinUpdateListener;
    }//注册回调接口的方法，供外部调用


    public WebSocketService() {
    }//构造函数


    //public final String MARKET_DEPTH = "market.{0}.depth.{1}";
    //public final String MARKET_TRADE_DETAIL = "market.{0}.trade.detail";
    //public final String MARKET_DETAIL = "market.{0}.detail";
    //private static boolean isOpened=false;
    public interface OnTradingUpdateListener {
        void OnTradingUpdate(BTCtrade btctrade);

    }//新建一个回调接口

    public interface OnKlinePinUpdateListener {
        void OnKlinePinUpdate(KLinePin kLinePin, KlinePeriod klinePeriod);
    }//新建一个回调接口

    private void initOKWebSocket() {
        mOkHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(wssAddress)
                .build();
        //建立连接
        mOkHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                mWebSocket = webSocket;
                Log.d(TAG, "WS OPEN");
                isOpened = true;

                //订阅kline数据
                Subscribe(TOPIC_MARKET_KLINE_1min, "id0");
                Subscribe(TOPIC_TRADE_DETAIL, "id1");
                Subscribe(TOPIC_MARKET_KLINE_1day, "id2");

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                //Log.d(TAG,"WS ON MESSAGE");
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                String responseData;
                Log.d(TAG, "WS OnMessageReceived");


                String decompressedStr = new StringUtil().decompressForGzip(bytes);
                Log.d(TAG + "解压后的字符串", decompressedStr);
                String tempSTR = decompressedStr.substring(decompressedStr.indexOf("\"status\":") + "\"status\":\"".length(), decompressedStr.indexOf("\"status\":") + "\"status\":\"".length() + 2);
                //Log.d(TAG+"ru ok",tempSTR);
                if (decompressedStr.contains("\"ping\":")) {
                    Log.d(TAG, "相应心跳");
                    responseData = decompressedStr.replace("ping", "pong");
                    webSocket.send(responseData);
                }//响应心跳包
                else if (decompressedStr.contains("error")) {
                    Log.e(TAG, "订阅错误！");
                }//订阅失败的情况
                else if (decompressedStr.contains("ok")) {

                    Log.i(TAG, "订阅成功");

                }//订阅成功的情况
                else {
                    String ch = decompressedStr.substring(decompressedStr.indexOf("\"ch\":") + "\"ch\":".length(), decompressedStr.indexOf(",\"ts\":"));
                    Log.d(TAG, "收到有用信息,来自频道" + ch);
                    //Log.d(TAG+"订阅ch:",ch);
                    switch (ch) {
                        case '\"' + TOPIC_MARKET_KLINE_1day + '\"': {
                            int mLocationJsonStart = decompressedStr.indexOf("\"tick\":") + "\"tick\":".length();
                            String JsonStrConvertedFromDecompressedStr = decompressedStr.substring(mLocationJsonStart, decompressedStr.length() - 1);
                            //Log.d(TAG + "转成json数据为", JsonStrConvertedFromDecompressedStr);
                            kLinePin_1day_everychange = new HttpUtil().KLinePin_parseJSONWithGSON(JsonStrConvertedFromDecompressedStr);
                            if (kLinePin_1day_everychange != null) {
                                kldb.insert("kltable_1day_everychange", null, kLinePin_1day_everychange.getContentValues());
                                if (shouldInitOLD_1day) {
                                    kLinePin_1day_everychange_OLD = kLinePin_1day_everychange;
                                    shouldInitOLD_1day = false;
                                } else if (kLinePin_1day_everychange.getId() / 60 / 60 / 24 != kLinePin_1day_everychange_OLD.getId() / 60 / 60 / 24) {
                                    if (kLinePin_1day_everychange_OLD != null) {
                                        kldb.insert("kltable_1day", null, kLinePin_1day_everychange_OLD.getContentValues());
                                        Log.d(TAG, "kltable_1day表插入一条新数据,id:" + kLinePin_1day_everychange_OLD.getId());

                                        if (onKlinePinUpdateListener != null) {
                                            onKlinePinUpdateListener.OnKlinePinUpdate(kLinePin_1day_everychange_OLD, KlinePeriod.day);
                                        }
                                    }
                                }


                                kLinePin_1day_everychange_OLD = kLinePin_1day_everychange;
                            }
                            break;
                        }
                        case '\"' + TOPIC_MARKET_KLINE_1min + '\"': {
                            int mLocationJsonStart = decompressedStr.indexOf("\"tick\":") + "\"tick\":".length();
                            String JsonStrConvertedFromDecompressedStr = decompressedStr.substring(mLocationJsonStart, decompressedStr.length() - 1);
                            //Log.d(TAG + "转成json数据为", JsonStrConvertedFromDecompressedStr);
                            kLinePin_1min_everychange = new HttpUtil().KLinePin_parseJSONWithGSON(JsonStrConvertedFromDecompressedStr);
                            if (kLinePin_1min_everychange != null) {
                                kldb.insert("kltable_1min_everychange", null, kLinePin_1min_everychange.getContentValues());
                                Log.d(TAG, "1min插入成功");
                                if (shouldInitOLD_1min) {
                                    kLinePin_1min_everychange_OLD = kLinePin_1min_everychange;
                                    shouldInitOLD_1min = false;
                                } else if (kLinePin_1min_everychange.getId() / 60  != kLinePin_1min_everychange_OLD.getId() / 60 ) {
                                    if (kLinePin_1min_everychange_OLD != null) {
                                        kldb.insert("kltable_1min", null, kLinePin_1min_everychange_OLD.getContentValues());
                                        Log.d(TAG, "kltable_1min表插入一条新数据,id:" + kLinePin_1min_everychange_OLD.getId());

                                        if (onKlinePinUpdateListener != null) {
                                            Log.d("now","调用回调");
                                            onKlinePinUpdateListener.OnKlinePinUpdate(kLinePin_1min_everychange_OLD, KlinePeriod.min);
                                        }
                                    }
                                }


                                kLinePin_1min_everychange_OLD = kLinePin_1min_everychange;
                            }

                            break;
                        }
                        case '\"' + TOPIC_TRADE_DETAIL + '\"': {
                            int mLocationJsonStart = decompressedStr.indexOf("\"data\":") + "\"data\":".length();
                            String JsonStrConvertedFromDecompressedStr = decompressedStr.substring(mLocationJsonStart, decompressedStr.length() - 2);
                            //Log.d(TAG + "转成json数据为", JsonStrConvertedFromDecompressedStr);

                            List<BTCtrade> btCtrades = new HttpUtil().BTCtrades_parseJSONWithGSON(JsonStrConvertedFromDecompressedStr);
                            for (BTCtrade btCtrade : btCtrades) {
                                if (btCtrade != null) {
                                    tradedb.insert("bit_trade", null, btCtrade.getContentValues());
                                    Log.d(TAG, "交易数据库插入一条新数据" + btCtrade.getId());
                                    if (onTradingUpdateListener != null) {
                                        onTradingUpdateListener.OnTradingUpdate(btCtrade);
                                    }
                                }
                            }

                            break;
                        }
                    }


                }//处理订阅数据


                Log.d(TAG, bytes.toString());


            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d(TAG, "WS ON CLOSING");

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                for (Map.Entry<String, String> entry : topicDic.entrySet())
                    UnSubscribe(entry.getKey(), entry.getValue());
                Log.d(TAG, "WS ON CLOSED");

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.d(TAG, "WS ON FAILIAR");
                initOKWebSocket();//启动WebSocket
            }
        });
    }

//    private void initSocket() {
//
//        try {
//            uri = new URI(wssAddress);
//            Log.d(TAG, "GET URI");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            Log.d(TAG, "LINKFAILED");
//        }
//        if (null == mWebSocketClient) {
//            Log.d(TAG, "Mwsc IS NULL");
//            mWebSocketClient = new WebSocketClient(uri) {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//                    Log.i(TAG, "websocket open");
//                    isOpened = true;
//
////                    Subscribe(MARKET_KLINE,"id01");
////                    for(Map.Entry<String,String> entry:topicDic.entrySet()){
////                        SendSubscribeTopic(entry.getValue());
////                    }
//
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    //TODO here wu need to decompress
//                    //TODO 在这里实现心跳吗
//                    Log.d(TAG, message);
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    Log.d(TAG, "链接关闭");
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    Log.d(TAG, "连接失败");
//                }
//            };
//            mWebSocketClient.connect();
//            Log.d(TAG, "ye" + mWebSocketClient.isConnecting());
//        }
//
//    }

    private void SendSubscribeTopic(String msg) {
        mWebSocket.send(msg);
    }

    private void Subscribe(String topic, String id) {
        if (topicDic.containsKey(topic))
            return;
        String msg = "{\"sub\": \"" + topic + "\",\"id\": \"" + id + "\"}";
        topicDic.put(topic, msg);
        if (isOpened)
            SendSubscribeTopic(msg);
    }

    private void UnSubscribe(String topic, String id) {
        if (topicDic.size() == 0 || !isOpened)
            return;
        String msg = "{\"unsub\": \"" + topic + "\",\"id\": \"" + id + "\"}";
        topicDic.remove(topic);
        SendSubscribeTopic(msg);

    }


    public class RetInfoBinder extends Binder {

        public BTCtrade getBTCtrade() {
            Log.d("vital", "" + btctrade.getPrice());
            return btctrade;
        }

        public WebSocketService getService() {
            return WebSocketService.this;
        }//获得当前service的实例

    }
    //暂时没用


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate executed");


        tradeDetailDatabaseHelper = new TradeDetailDatabaseHelper(this, "btc_trade.db", null, 1);
        klDatabaseHelper = new KLDatabaseHelper(this, "kline.db", null, 1);
        tradedb = tradeDetailDatabaseHelper.getWritableDatabase();
        kldb = klDatabaseHelper.getWritableDatabase();


        initOKWebSocket();//启动WebSocket


    }

    @Override
    public void onDestroy() {
        //TODO
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return mBinder;

    }
}
