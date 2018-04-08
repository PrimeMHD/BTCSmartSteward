package mhd.com.btcsmartsteward.Fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.data.LineData;
import com.guoziwei.klinelib.chart.KLineView;
import com.guoziwei.klinelib.model.HisData;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;
import mhd.com.btcsmartsteward.Activity.MainActivity;
import mhd.com.btcsmartsteward.DatabaseTool.KLDatabaseHelper;
import mhd.com.btcsmartsteward.R;

//import me.yokeyword.fragmentation.SupportFragment;


public class ParentFirstFragment extends SupportFragment implements View.OnClickListener {


    public TextView trade_Text;
    public TextView trade_amount_Text;
    public TextView trade_type_Text;
    public TextView trade_time_Text;
    public TextView trade_price_huge;
    public TextView trade_price_rate;
    private Button testbtn;
    public KLineView mKLineView;
    public RadioGroup rgIndex;
    public RadioGroup rgKType;
    public int mType;
    private KLDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    MainActivity mainActivity;
    //MyKLineChartInfoView myKLineChartInfoView;
    private int colorHomeBg;
    private int colorLine;
    private int colorText;
    private int colorMa5;
    private int colorMa10;
    private int colorMa20;
    private int itemcount;
    private LineData lineData;
    public List<HisData> hisDatas = new ArrayList<>();

    public ParentFirstFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ParentFirstFragment newInstance() {
        ParentFirstFragment fragment = new ParentFirstFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);

        //fragment.setArguments(args);
        return fragment;
    }

    public void RefreshingText() {
        //responseText=(TextView)getView().findViewById(R.id.response_text);
        //trade_Text=(TextView)getView().findViewById(R.id.trade_text);
        // 在这里进行UI操作，将结果显示到界面上
        trade_Text.setText("ddd");
        //mainActivity.showResponse(mainActivity.getBtctrade().price);

    }


    @Override
    public void onClick(View v) {
        trade_Text.setText("dddd");

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) this.getActivity();




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_parentfirst, container, false);
        trade_Text = (TextView) view.findViewById(R.id.trade_text);
        trade_amount_Text = (TextView) view.findViewById(R.id.trade_amount_text);
        trade_time_Text = (TextView) view.findViewById(R.id.trade_time_text);
        trade_type_Text = (TextView) view.findViewById(R.id.trade_type_text);
        trade_price_rate=(TextView)view.findViewById(R.id.tv_updownrate);
        trade_price_huge=(TextView)view.findViewById(R.id.tv_currentprice_huge);
        //mChart = (CombinedChart) view.findViewById(R.id.chart1);
        mKLineView = (KLineView) view.findViewById(R.id.kline_chart);
        rgIndex=(RadioGroup)view.findViewById(R.id.rg_index);
        rgKType=(RadioGroup)view.findViewById(R.id.rg_kltype);
        // 分时图
        // kLineView.initChartPriceData(hisDatas);
        // K线图
        //kLineView.initChartKData(hisDatas);
        mainActivity.setRadios();
        return view;
    }

    private void initData() {

        //dbHelper = new KLDatabaseHelper(mainActivity, "kldb.db", null, 1);
        //db = dbHelper.getWritableDatabase();
       /*
        try {
            db = mainActivity.kldb;
        } catch (Exception e) {

        }
        if (db == null)
            return;
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault());
        Cursor cursor = db.query("kltable", null, null, null, null, null, null);



        //如果有，先弄些历史数据画在上面
        if (cursor.moveToLast()) {
            for (int i = 0; i < 120; i++) {
                if (!cursor.moveToPrevious())
                    break;
            }
            while (cursor.moveToNext()) {
                //String tprice = cursor.getString(cursor.getColumnIndex("price"));
                String thigh = cursor.getString(cursor.getColumnIndex("klhigh"));
                String tlow = cursor.getString(cursor.getColumnIndex("kllow"));
                String topen = cursor.getString(cursor.getColumnIndex("klopen"));
                String tclose = cursor.getString(cursor.getColumnIndex("klclose"));
                int tvolume = cursor.getInt(cursor.getColumnIndex("klvolume"));
                long tdate = cursor.getLong(cursor.getColumnIndex("timestamp"));
                //Date date=new Date(tdate*1000);
                HisData data = new HisData();
                data.setHigh(Double.valueOf(thigh));
                data.setLow(Double.valueOf(tlow));
                data.setOpen(Double.valueOf(topen));
                data.setClose(Double.valueOf(tclose));
                data.setVol(tvolume);
                //data.setVol((int) (1000000000 * Double.valueOf(tvolume)));
                data.setDate(tdate * 60 * 1000);
                hisDatas.add(data);
            }
        }

        //String curprice=cursor.getString(cursor.getColumnIndex("price"));
        //Log.d("vitalvery",curprice);

        cursor.close();
*/
        //final List<HisData> hisData = ;
        mKLineView.setLastClose(56.81);

/*
        mKLineView.setChartInfoView(mainActivity.myKLineChartInfoView);
        if (mType == 0)

        {
            //mKLineView.setChartInfoView(myKLineChartInfoView);
            mKLineView.initChartKData(hisDatas);
        } else

        {
            mKLineView.initChartPriceData(hisDatas);
        }
        */
        mKLineView.setLimitLine();
    }





/*
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mKLineView.post(new Runnable() {
                    @Override
                    public void run() {
                        mKLineView.refreshData((float) (56.8 + 0.1 * Math.random()));
                    }
                });
            }
        }, 1000, 1000);
        */
}



//@Override
//public void onAttach(Context context) {
//   super.onAttach(context);
//   this.mActivity=(Activity) context;
//}



