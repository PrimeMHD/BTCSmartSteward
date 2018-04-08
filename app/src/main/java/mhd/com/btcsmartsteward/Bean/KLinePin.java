package mhd.com.btcsmartsteward.Bean;

import android.content.ContentValues;

/**
 * Created by Mi HD on 2018/3/10.
 */
/*K线类*/
public class KLinePin {
    private long id;
    private String open;
    private String close;
    private String low;
    private String high;
    private String amount;
    private String vol;
    private long count;

    public KLinePin(long id, String open, String close, String low, String high, String amount, String vol, long count) {
        this.id = id;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.amount = amount;
        this.vol = vol;
        this.count = count;
    }
    public KLinePin(){

    }
    public ContentValues getContentValues(){
        ContentValues contentValues=new ContentValues();
        contentValues.put("id",id);
        contentValues.put("open",open);
        contentValues.put("close",close);
        contentValues.put("low",low);
        contentValues.put("high",high);
        contentValues.put("amount",amount);
        contentValues.put("vol",vol);
        contentValues.put("count",count);
        return contentValues;
    }

    public long getId() {
        return id;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public String getLow() {
        return low;
    }

    public String getHigh() {
        return high;
    }

    public String getAmount() {
        return amount;
    }

    public String getVol() {
        return vol;
    }

    public long getCount() {
        return count;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
