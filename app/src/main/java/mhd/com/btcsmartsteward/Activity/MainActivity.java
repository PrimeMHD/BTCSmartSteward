package mhd.com.btcsmartsteward.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.guoziwei.klinelib.model.HisData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;
import mhd.com.btcsmartsteward.Bean.BTCinfo;
import mhd.com.btcsmartsteward.Bean.BTCtrade;
import mhd.com.btcsmartsteward.Bean.KLinePin;
import mhd.com.btcsmartsteward.DatabaseTool.KLDatabaseHelper;
import mhd.com.btcsmartsteward.DatabaseTool.TradeDetailDatabaseHelper;
import mhd.com.btcsmartsteward.Fragment.ParentFirstFragment;
import mhd.com.btcsmartsteward.Fragment.ParentSecondFragment;
import mhd.com.btcsmartsteward.Fragment.ParentThirdFragment;
import mhd.com.btcsmartsteward.MyKline.MyKLineChartInfoView;
import mhd.com.btcsmartsteward.R;
import mhd.com.btcsmartsteward.Service.SyncHistoryService;
import mhd.com.btcsmartsteward.Service.UpdateDataService;
import mhd.com.btcsmartsteward.Service.WebSocketService;


public class MainActivity extends SupportActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int FIRST = 0;
    private static final int SECOND = 1;
    private static final int THIRD = 2;
    private WebSocketService.KlinePeriod chosenklinePeriod = WebSocketService.KlinePeriod.min;
    BottomNavigationBar bottomNavigationBar;


    private KLDatabaseHelper klDatabaseHelper;
    private SQLiteDatabase kldb;
    private TradeDetailDatabaseHelper tradeDetailDatabaseHelper;
    private SQLiteDatabase tradedb;
    private BTCinfo mBTCinfo;
    private double Btcinfo_open = 1.0;
    private BTCtrade btctrade = new BTCtrade();
    private KLinePin kLinePin = new KLinePin();
    private WebSocketService webSocketService;
    private boolean SyncHistoryComplete = false;
    int curFragment = FIRST;
    int prePosition = FIRST;
    FragmentManager fragmentManager;
    private SupportFragment[] mFragments = new SupportFragment[3];
    SupportFragment first_root_Fragment = mFragments[FIRST];
    SupportFragment second_root_Fragment = mFragments[SECOND];
    SupportFragment third_root_Fragment = mFragments[THIRD];
    FrameLayout frameLayout;
    ParentFirstFragment tradeFragment;
    ParentSecondFragment infoFragment;
    ParentThirdFragment settingsFragment;

    private MainActivity instance;

    private double lastprice = 8888;//用来判断是涨是跌
    private boolean isGrowing = true;
    private boolean isKlineGrowing_OLD = true;
    private double lastKlinePrice = 0;
    private boolean shouldAlarmH = true;
    private boolean shouldAlarmL = true;

    public SharedPreferences.Editor editor;
    public SharedPreferences pref;
    public double settedAHP = 999999999;  //设置好的高价提醒
    public double settedALP = 0;  //设置好的低价提醒
    public double settedBIH = 0;  //设置好的持币数目
    private double waverate = 0;
    private double wavevalue = 0;
    public double settedBottomRate = 1;
    public boolean shouldWatchBottom = false;
    public double settedTopRate = 1;
    public boolean shouldWatchTop = false;
    private boolean appFirstRun = true;
    private int lastSelectedPosition = 0;

    MyKLineChartInfoView myKLineChartInfoView;


    public static final String ACTION_TRADE = "mhd.com.btcsmartsteward.Activity.MainActivity.ACTION.TRADE";
    public static final String ACTION_UPFINISH = "mhd.com.btcsmartsteward.Activity.MainActivity.ACTION.UPFINISH";

    private WebSocketService.RetInfoBinder retInfoBinder;

    public void onSetBtnTouched(View view) {
        switch (view.getId()) {
            case R.id.btn_setbtc_inhand: {
                Intent intent = new Intent(MainActivity.this, SettingActivity_setasset.class);
                int nRequestCode = 0;
                startActivityForResult(intent, nRequestCode);
            }
        }

    }

    public void onToolbarItemSelected(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        intent.putExtra("InfoNum", 0);
        int nRequestCode = 4;
        startActivityForResult(intent, nRequestCode);

    }

    private ServiceConnection webSocketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            webSocketService = ((WebSocketService.RetInfoBinder) service).getService();//返回一个WebSocketService对象

            webSocketService.setOnKlinePinUpdateListener(new WebSocketService.OnKlinePinUpdateListener() {


                @Override
                public void OnKlinePinUpdate(KLinePin kLinePin, WebSocketService.KlinePeriod klinePeriod) {

                    if (klinePeriod.equals(chosenklinePeriod)) {
                        Log.d("now", "来了新的k线");
                        wavevalue = Double.valueOf(kLinePin.getClose()) - lastKlinePrice;
                        waverate = wavevalue / lastKlinePrice;

                        Judge_Watch();
                        lastKlinePrice = Double.valueOf(kLinePin.getClose());
                        //数据库的操作放在Service中
                        addNewtoGraph(kLinePin);
                        editor.putFloat("lastKlinePrice", (float) lastKlinePrice);
                        editor.apply();
                    }


                }


            });

            webSocketService.setOnTradingUpdateListener(new WebSocketService.OnTradingUpdateListener() {


                @Override
                public void OnTradingUpdate(BTCtrade btctrade) {

                    MainActivity.this.btctrade = btctrade;

                    if (Double.valueOf(btctrade.getPrice()) >= lastprice) {
                        isGrowing = true;
                        lastprice = Double.valueOf(btctrade.getPrice());
                    } else {
                        isGrowing = false;
                        lastprice = Double.valueOf(btctrade.getPrice());
                    }

                    Judge_Alarm();


                    if (curFragment == FIRST) {
                        showResponse(btctrade);
                    } else if (curFragment == THIRD) {

                        showMyassetChange();
                    }


                }
            });//注册回调接口来接收实时变化的交易信息
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };//如果需要主动向服务发出请求，可以在这里增添代码

    private void Judge_Watch() {
        settingsFragment = (ParentThirdFragment) mFragments[THIRD];
        //检测抄底时间算法
        if (shouldWatchBottom && !isKlineGrowing_OLD && waverate > settedBottomRate) {
            Intent intentalarm = new Intent(MainActivity.this, AlarmActivity.class);
            int nRequestCode = 1;
            intentalarm.putExtra("AlarmType", 'B');
            startActivityForResult(intentalarm, nRequestCode);
            shouldWatchBottom = false;
        } else if (!shouldWatchBottom && waverate > 0.01 && settingsFragment.mSwitchs[1].isChecked()) {
            shouldWatchBottom = true;
        }

        //检测止盈算法
        if (shouldWatchTop && isKlineGrowing_OLD && -waverate > settedBottomRate) {
            Intent intentalarm = new Intent(MainActivity.this, AlarmActivity.class);
            int nRequestCode = 1;
            intentalarm.putExtra("AlarmType", 'T');
            startActivityForResult(intentalarm, nRequestCode);
            shouldWatchTop = false;
        } else if (!shouldWatchTop && waverate < -0.01 && settingsFragment.mSwitchs[1].isChecked()) {
            shouldWatchTop = true;

        }
        isKlineGrowing_OLD = wavevalue > 0;
    }

    private void Judge_Alarm() {
        //唤醒价格区间报警Activity
        if (shouldAlarmL && Double.valueOf(btctrade.getPrice()) < settedALP) {
            Intent intentalarm = new Intent(MainActivity.this, AlarmActivity.class);
            int nRequestCode = 1;
            intentalarm.putExtra("AlarmType", 'L');
            startActivityForResult(intentalarm, nRequestCode);
            shouldAlarmL = false;
        } else if (shouldAlarmH && Double.valueOf(btctrade.getPrice()) > settedAHP) {
            Intent intentalarm = new Intent(MainActivity.this, AlarmActivity.class);
            int nRequestCode = 2;
            intentalarm.putExtra("AlarmType", 'H');
            startActivityForResult(intentalarm, nRequestCode);
            shouldAlarmH = false;
        } else if (!shouldAlarmL && Double.valueOf(btctrade.getPrice()) > settedALP + 100) {
            shouldAlarmL = true;
        } else if (!shouldAlarmH && Double.valueOf(btctrade.getPrice()) < settedAHP - 100) {
            shouldAlarmH = true;
        }
        //+-100是为了去抖动干扰

    }

    /**
     * 网络连接是否正常 * * @return true:有网络 false:无网络
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public BTCtrade getBtctrade() {

        return MainActivity.this.btctrade;
    }

    public MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        btctrade.setPrice("0");
        klDatabaseHelper = new KLDatabaseHelper(this, "kline.db", null, 1);
        kldb = klDatabaseHelper.getWritableDatabase();
        tradeDetailDatabaseHelper = new TradeDetailDatabaseHelper(this, "btc_trade.db", null, 1);
        tradedb = tradeDetailDatabaseHelper.getWritableDatabase();


        InitPreferenceData();
        editor = pref.edit();


        if (appFirstRun) {
            editor.putBoolean("appFirstRun", false);
            editor.apply();
            //editor.clear();
            initDataBaseInstallRun();
        }
        //当APP为安装后第一次运行的时候，就进行数据库初始化操作
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(MainActivity.ACTION_TRADE);
        intentFilter1.addAction(MainActivity.ACTION_UPFINISH);
        registerReceiver(MainActivityReceiver, intentFilter1);//动态注册服务中的广播接收机
        initView();
        Log.d("now", "准备更新数据");
        Log.d("now",""+isNetworkConnected(MainActivity.this));
        startService(new Intent(MainActivity.this, UpdateDataService.class));
        startService(new Intent(MainActivity.this, SyncHistoryService.class));
        //历史数据同步完成后开始更新数据


    }

    private void InitPreferenceData() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //settedAHP=pref.getFloat("settedAHP",999999999);
        //settedALP=pref.getFloat("settedALP",0);
        //shouldAlarmH=pref.getBoolean("shouldAlarmH",false);
        //shouldAlarmL=pref.getBoolean("shouldAlarmL",false);
        appFirstRun = pref.getBoolean("appFirstRun", true);
        settedBIH = pref.getFloat("settedBIH", 0);
        lastKlinePrice = pref.getFloat("lastKlinePrice", 8888);
        //settedBottomRate=pref.getFloat("settedBottomRate",1);
        //settedTopRate=pref.getFloat("settedTopRate",1);
        //shouldWatchBottom=pref.getBoolean("shouldWatchBottom",false);
        //shouldWatchTop=pref.getBoolean("shouldWatchTop",false);


    }


//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }


    private BroadcastReceiver MainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTION_TRADE)) {
                final Intent innerintentcopy = intent;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 在这里进行UI操作，将结果显示到界面上
                        TextView tf_open = (TextView) findViewById(R.id.tf_open);
                        TextView tf_amount = (TextView) findViewById(R.id.tf_amount);
                        TextView tf_low = (TextView) findViewById(R.id.tf_low);
                        TextView tf_high = (TextView) findViewById(R.id.tf_high);
                        TextView tf_count = (TextView) findViewById(R.id.tf_count);
                        TextView tf_vol = (TextView) findViewById(R.id.tf_vol);
                        Btcinfo_open = innerintentcopy.getFloatExtra("btcinfo.open", -1);
                        tf_count.setText("" + innerintentcopy.getLongExtra("btcinfo.count", -1));
                        tf_vol.setText("" + innerintentcopy.getFloatExtra("btcinfo.volume", -1));
                        tf_high.setText("" + innerintentcopy.getFloatExtra("btcinfo.high", -1));
                        tf_open.setText("" + innerintentcopy.getFloatExtra("btcinfo.open", -1));
                        tf_low.setText("" + innerintentcopy.getFloatExtra("btcinfo.low", -1));
                        tf_amount.setText("" + innerintentcopy.getFloatExtra("btcinfo.volume", -1));

                    }
                });
            } else if (intent.getAction().equals(MainActivity.ACTION_UPFINISH)) {

                SyncHistoryComplete = true;
                if (chosenklinePeriod == WebSocketService.KlinePeriod.day) {
                    initData("kltable_1day");
                } else if (chosenklinePeriod == WebSocketService.KlinePeriod.min) {
                    initData("kltable_1min");
                }
                Intent wss_intent = new Intent(MainActivity.this, WebSocketService.class);
                bindService(wss_intent, webSocketServiceConnection, Context.BIND_AUTO_CREATE);
                //绑定服务

            }

            Log.d("MainActivityReceiver", "onReceive");

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(MainActivityReceiver);
        //TODO
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        infoFragment = (ParentSecondFragment) mFragments[SECOND];
        if (curFragment == SECOND && keyCode == KeyEvent.KEYCODE_BACK && infoFragment.webView.canGoBack()) {
            infoFragment.webView.goBack();//返回上个页面
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出整个应用程序
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void settingSHOW(final int inspect_num) {
        settingsFragment = (ParentThirdFragment) mFragments[2];

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                switch (inspect_num) {
                    case 0: {
                        settingsFragment.btc_in_hand.setText("" + String.format("%.2f", settedBIH));
                        settingsFragment.myasset.setText("" + settedBIH * Double.valueOf(btctrade.getPrice()));
                        break;
                    }
                    case 1: {
                        settingsFragment.inspect1_set.setText("高价提示:" + settedAHP + "\n低价提示:" + settedALP);
                        break;
                    }
                    case 2: {
                        settingsFragment.inspect2_set.setText("回升幅度:" + "+" + String.format("%.3f", settedBottomRate * 100) + "%");
                        break;
                    }
                    case 3: {
                        settingsFragment.inspect3_set.setText("回撤幅度:" + "-" + String.format("%.3f", settedTopRate * 100) + "%");
                        break;
                    }
                }


            }
        });
    }

    private void showMyassetChange() {
        settingsFragment = (ParentThirdFragment) mFragments[2];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                settingsFragment.btc_in_hand.setText("" + String.format("%.2f", settedBIH));
                if (isGrowing) {//涨了
                    String tempstr = String.format("%.2f", settedBIH * Double.valueOf(btctrade.getPrice()));
                    settingsFragment.myasset.setText(tempstr);
                    settingsFragment.myasset.setTextColor(getResources().getColor(R.color.main_color_red));

                } else {//跌了
                    String tempstr = String.format("%.2f", settedBIH * Double.valueOf(btctrade.getPrice()));
                    settingsFragment.myasset.setText(tempstr);
                    settingsFragment.myasset.setTextColor(getResources().getColor(R.color.main_color_green));

                }
            }
        });
    }


    public void showResponse(final BTCtrade btctrade) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                Date date = new Date(btctrade.getTs());
                //Log.d("time",);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String datestr = sdf.format(date);
                if (isGrowing) {//升值
                    tradeFragment.trade_Text.setText(String.format("%.3f", Double.valueOf(btctrade.getPrice())));
                    tradeFragment.trade_Text.setTextColor(getResources().getColor(R.color.main_color_red));
                } else {
                    tradeFragment.trade_Text.setText(String.format("%.3f", Double.valueOf(btctrade.getPrice())));
                    tradeFragment.trade_Text.setTextColor(getResources().getColor(R.color.main_color_green));
                }
                tradeFragment.trade_amount_Text.setText(String.format("%.5f", Double.valueOf(btctrade.getAmount())));
                if (btctrade.getDirection().equals("buy"))
                    tradeFragment.trade_type_Text.setText("buy买进");
                else
                    tradeFragment.trade_type_Text.setText("sell卖出");

                tradeFragment.trade_time_Text.setText("" + datestr);


                double changerate_24h = (Double.valueOf(btctrade.getPrice()) - Btcinfo_open) / Btcinfo_open;

                if (changerate_24h >= 0) {
                    tradeFragment.trade_price_huge.setText(String.format("%.2f", Double.valueOf(btctrade.getPrice())));
                    tradeFragment.trade_price_huge.setTextColor(getResources().getColor(R.color.increasing_color1));
                    tradeFragment.trade_price_rate.setTextColor(getResources().getColor(R.color.increasing_color1));
                    tradeFragment.trade_price_rate.setText("+" + String.format("%.3f", changerate_24h * 100) + "%");
                } else {
                    tradeFragment.trade_price_huge.setText(String.format("%.2f", Double.valueOf(btctrade.getPrice())));
                    tradeFragment.trade_price_huge.setTextColor(getResources().getColor(R.color.main_color_green));
                    tradeFragment.trade_price_rate.setTextColor(getResources().getColor(R.color.main_color_green));
                    tradeFragment.trade_price_rate.setText(String.format("%.3f", changerate_24h * 100) + "%");
                }


            }
        });
    }

    //将已有数据初始载入到k线上
    private void initData(final String tablename) {


        tradeFragment = (ParentFirstFragment) mFragments[0];
        tradeFragment.mKLineView.post(new Runnable() {
            @Override
            public void run() {

                SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault());
                Cursor cursor = kldb.query(tablename, null, null, null, null, null, null);


                //如果有，先弄些历史数据画在上面
                if (cursor != null && cursor.moveToLast()) {
                    for (int i = 0; i < 1200; i++) {
                        if (!cursor.moveToPrevious())
                            break;
                    }
                    while (cursor.moveToNext()) {
                        String thigh = cursor.getString(cursor.getColumnIndex("high"));
                        String tlow = cursor.getString(cursor.getColumnIndex("low"));
                        String topen = cursor.getString(cursor.getColumnIndex("open"));
                        String tclose = cursor.getString(cursor.getColumnIndex("close"));
                        int tvolume = (int) (Double.valueOf(cursor.getString(cursor.getColumnIndex("vol"))) * 1);
                        long tdate = cursor.getLong(cursor.getColumnIndex("id"));

                        HisData data = new HisData();
                        data.setHigh(Double.valueOf(thigh));
                        data.setLow(Double.valueOf(tlow));
                        data.setOpen(Double.valueOf(topen));
                        data.setClose(Double.valueOf(tclose));
                        data.setVol(tvolume);
                        data.setDate(tdate * 1000);//TODO 这里可能有错
                        tradeFragment.hisDatas.add(data);
                    }
                    cursor.close();
                }

                //myKLineChartInfoView=new MyKLineChartInfoView(MainActivity.this);
                //tradeFragment.mKLineView.setChartInfoView(myKLineChartInfoView);
                tradeFragment.mKLineView.initData(tradeFragment.hisDatas);
                tradeFragment.mKLineView.setLimitLine();

            }
        });

    }

    //private void addNewtoGraph(final int volume,final double open,final double close,final double high,final double low,final long timestamp) {
    private void addNewtoGraph(KLinePin kLinePin) {
        //tradeFragment=(ParentFirstFragment)mFragments[0];
        long timestamp = kLinePin.getId();

        int volume = (int) (Double.valueOf(kLinePin.getVol()) * 1);
        double open = Double.valueOf(kLinePin.getOpen());
        double close = Double.valueOf(kLinePin.getClose());
        double high = Double.valueOf(kLinePin.getHigh());
        double low = Double.valueOf(kLinePin.getLow());
        final HisData newData = new HisData(open, close, high, low, volume, timestamp);


        tradeFragment.mKLineView.post(new Runnable() {
            @Override
            public void run() {
                int index = (int) (Math.random() * 100);


                tradeFragment.hisDatas.add(newData);
                Log.d("now", "We add new to Graph");
                //myKLineChartInfoView=new MyKLineChartInfoView(MainActivity.this);
                //tradeFragment.mKLineView.setChartInfoView(myKLineChartInfoView);
                tradeFragment.mKLineView.addData(newData);

            }
        });
    }

    //安装好APP第一次运行就建立数据库
    private void initDataBaseInstallRun() {

        klDatabaseHelper.createTable("kltable_1day");
        klDatabaseHelper.createTable("kltable_1min");
        klDatabaseHelper.createTable("kltable_1day_everychange");
        klDatabaseHelper.createTable("kltable_1min_everychange");
        tradeDetailDatabaseHelper.createTable();

        //人为插入第一条数据防止报错
        KLinePin ZerokLinePin = new KLinePin(1510369871, "0", "0", "0", "0", "0", "0", 0);
        BTCtrade Zerobtctrade = new BTCtrade("0", "0", "0", "0", 0);
        kldb.insert("kltable_1day", null, ZerokLinePin.getContentValues());
        kldb.insert("kltable_1min", null, ZerokLinePin.getContentValues());
        kldb.insert("kltable_1day_everychange", null, ZerokLinePin.getContentValues());
        kldb.insert("kltable_1min_everychange", null, ZerokLinePin.getContentValues());
        tradedb.insert("bit_trade", null, Zerobtctrade.getContentValues());

    }

    private void initView() {


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.layFrame);
        fragmentManager = getSupportFragmentManager();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prePosition = FIRST;
        curFragment = FIRST;
        fragmentManager = getSupportFragmentManager();
        first_root_Fragment = findFragment(ParentFirstFragment.class);
        if (first_root_Fragment == null) {
            mFragments[FIRST] = ParentFirstFragment.newInstance();
            mFragments[SECOND] = ParentSecondFragment.newInstance();
            mFragments[THIRD] = ParentThirdFragment.newInstance();

            loadMultipleRootFragment(R.id.layFrame, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD]
            );
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = first_root_Fragment;
            mFragments[SECOND] = findFragment(ParentSecondFragment.class);
            mFragments[THIRD] = findFragment(ParentThirdFragment.class);

        }
        second_root_Fragment = mFragments[SECOND];
        third_root_Fragment = mFragments[THIRD];
        bottomNavigationBar = findViewById(R.id.bottomNavigationBar);
        bottomNavigationBar.clearAll();
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_dashboard_black_24dp, "行情").setActiveColorResource(R.color.colorPrimary))//.setBadgeItem(badgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_home_black_24dp, "资讯").setActiveColorResource(R.color.colorPrimary))
                .addItem(new BottomNavigationItem(R.drawable.ic_notifications_black_24dp, "助手").setActiveColorResource(R.color.colorPrimary))
                .setFirstSelectedPosition(0)
                .initialise();

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                curFragment = position;
                showHideFragment(mFragments[position], mFragments[prePosition]);
                prePosition = position;
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                final SupportFragment currentFragment = mFragments[position];
                int count = currentFragment.getChildFragmentManager().getBackStackEntryCount();
                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    if (currentFragment instanceof ParentFirstFragment) {
                        currentFragment.popToChild(ParentFirstFragment.class, false);
                    } else if (currentFragment instanceof ParentSecondFragment) {
                        currentFragment.popToChild(ParentSecondFragment.class, false);
                    } else if (currentFragment instanceof ParentThirdFragment) {
                        currentFragment.popToChild(ParentThirdFragment.class, false);
                    }

                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data.getStringExtra("chibishu") == "")
                return;
            settedBIH = Double.valueOf(data.getStringExtra("chibishu"));
            settingSHOW(0);
            editor.putFloat("settedBIH", (float) settedBIH);
            editor.apply();
            //editor.clear();
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            String djiastr = data.getStringExtra("dijiatixing");
            String gaojiastr = data.getStringExtra("gaojiatixing");
            if (!Objects.equals(djiastr, "") && !Objects.equals(gaojiastr, "")) {
                settedALP = Double.valueOf(djiastr);
                settedAHP = Double.valueOf(gaojiastr);
            } else {
                settedALP = 0;
                settedAHP = 999999999;
            }
            shouldAlarmL = true;
            shouldAlarmH = true;
            editor.putBoolean("shouldAlarmL", shouldAlarmL);
            editor.putBoolean("shouldAlarmH", shouldAlarmH);
            editor.putFloat("settedALP", (float) settedALP);
            editor.putFloat("settedAHP", (float) settedAHP);
            editor.apply();
            //editor.clear();
            settingSHOW(1);

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            String settedBottomRatestr = data.getStringExtra("settedBottomRate");
            if (!Objects.equals(settedBottomRatestr, "")) {
                settedBottomRate = Double.valueOf(settedBottomRatestr);
            } else {
                settedBottomRate = 1;

            }
            shouldWatchBottom = true;
            editor.putFloat("settedBottomRate", (float) settedBottomRate);
            editor.putBoolean("shouldWatchBottom", shouldWatchBottom);
            editor.apply();
            //editor.clear();
            settingSHOW(2);

        } else if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            String settedTopRatestr = data.getStringExtra("settedTopRate");
            if (!Objects.equals(settedTopRatestr, "")) {
                settedTopRate = Double.valueOf(settedTopRatestr);
            } else {
                settedTopRate = 1;
            }
            shouldWatchTop = true;
            editor.putFloat("settedTopRate", (float) settedTopRate);
            editor.putBoolean("shouldWatchTop", shouldWatchTop);
            editor.apply();
            //editor.clear();
            settingSHOW(3);
        }
    }

    public void setRadios() {
        tradeFragment = (ParentFirstFragment) mFragments[0];
        tradeFragment.rgIndex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (i == R.id.cb_vol) {

                    tradeFragment.mKLineView.showVolume();

                } else if (i == R.id.cb_macd) {

                    tradeFragment.mKLineView.showMacd();

                } else if (i == R.id.cb_kdj) {

                    tradeFragment.mKLineView.showKdj();

                }
            }
        });
        ((RadioButton) tradeFragment.rgIndex.getChildAt(0)).setChecked(true);

        tradeFragment.rgKType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (i == R.id.cb_minline) {
                    chosenklinePeriod = WebSocketService.KlinePeriod.min;
                    if (SyncHistoryComplete)
                        initData("kltable_1min");

                } else if (i == R.id.cb_dayline) {
                    chosenklinePeriod = WebSocketService.KlinePeriod.day;
                    if (SyncHistoryComplete)
                        initData("kltable_1day");

                }
            }
        });
        ((RadioButton) tradeFragment.rgKType.getChildAt(1)).setChecked(true);

    }

    public void setmSwitchs() {
        settingsFragment = (ParentThirdFragment) mFragments[2];
        settingsFragment.mSwitchs[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity_setinterval.class);
                    int nRequestCode = 1;
                    startActivityForResult(intent, nRequestCode);
                } else {
                    shouldAlarmH = false;
                    shouldAlarmL = false;
                    settedAHP = 999999999;
                    settedALP = 0;
                    editor.putFloat("settedAHP", (float) settedBIH);
                    editor.putFloat("settedBLP", (float) settedBIH);
                    editor.putBoolean("shouldAlarmH", shouldAlarmH);
                    editor.putBoolean("shouldAlarmL", shouldAlarmL);

                    settingsFragment.inspect1_set.setText("No Settings");
                    editor.apply();
                    //editor.clear();

                }
            }
        });
        settingsFragment.mSwitchs[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity_setbottom.class);
                    int nRequestCode = 2;
                    startActivityForResult(intent, nRequestCode);
                } else {
                    shouldWatchBottom = false;
                    settedBottomRate = 1;

                    editor.putBoolean("shouldWatchBottom", shouldWatchBottom);
                    editor.putFloat("settedBottomRate", (float) settedBottomRate);
                    settingsFragment.inspect2_set.setText("No Settings");
                    editor.apply();
                    //editor.clear();

                }
            }
        });
        settingsFragment.mSwitchs[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity_settop.class);
                    int nRequestCode = 3;
                    startActivityForResult(intent, nRequestCode);
                } else {
                    shouldWatchTop = false;
                    settedTopRate = 1;
                    editor.putBoolean("shouldWatchTop", shouldWatchTop);
                    editor.putFloat("settedTopRate", (float) settedTopRate);
                    settingsFragment.inspect3_set.setText("No Settings");
                    editor.apply();
                    //editor.clear();

                }
            }
        });
        settingsFragment.HelperInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                intent.putExtra("InfoNum", 1);
                int nRequestCode = 4;
                startActivityForResult(intent, nRequestCode);
            }
        });

    }

}
