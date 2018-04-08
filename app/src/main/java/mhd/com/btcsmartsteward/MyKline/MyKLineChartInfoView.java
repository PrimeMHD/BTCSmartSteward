package mhd.com.btcsmartsteward.MyKline;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.guoziwei.klinelib.chart.ChartInfoView;
import com.guoziwei.klinelib.model.HisData;
import com.guoziwei.klinelib.util.DateUtils;
import com.guoziwei.klinelib.util.DoubleUtil;

/**
 * Created by Mi HD on 2017/12/21.
 */

public class MyKLineChartInfoView extends ChartInfoView {


    private TextView mTvOpenPrice;
    private TextView mTvClosePrice;
    private TextView mTvHighPrice;
    private TextView mTvLowPrice;
    private TextView mTvChangeRate;
    private TextView mTvVol;
    private TextView mTvTime;
    private View mVgChangeRate;

    public MyKLineChartInfoView(Context context) {
        this(context, null);
    }

    public MyKLineChartInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyKLineChartInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(com.guoziwei.klinelib.R.layout.view_kline_chart_info, this);
        mTvTime = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_time);
        mTvOpenPrice = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_open_price);
        mTvClosePrice = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_close_price);
        mTvHighPrice = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_high_price);
        mTvLowPrice = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_low_price);
        mTvChangeRate = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_change_rate);
        mTvVol = (TextView) findViewById(com.guoziwei.klinelib.R.id.tv_vol);
        mVgChangeRate = findViewById(com.guoziwei.klinelib.R.id.vg_change_rate);
    }

    @Override
    public void setData(double lastClose, HisData data) {
        //mTvTime.setText(DateUtils.formatData(data.getDate()));//2018.3.20暂时删掉
        mTvClosePrice.setText(DoubleUtil.formatDecimal(data.getClose()));
        mTvOpenPrice.setText(DoubleUtil.formatDecimal(data.getOpen()));
        mTvHighPrice.setText(DoubleUtil.formatDecimal(data.getHigh()));
        mTvLowPrice.setText(DoubleUtil.formatDecimal(data.getLow()));
//        mTvChangeRate.setText(String.format(Locale.getDefault(), "%.2f%%", (data.getClose()- data.getOpen()) / data.getOpen() * 100));
        mTvChangeRate.setVisibility(GONE);
        mVgChangeRate.setVisibility(GONE);

        mTvVol.setText(data.getVol() + "");
        removeCallbacks(mRunnable);
        postDelayed(mRunnable, 2000);
    }
}
