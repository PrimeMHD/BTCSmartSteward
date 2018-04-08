package mhd.com.btcsmartsteward.Bean;

import android.content.ContentValues;

/**
 * Created by Mi HD on 2017/12/15.
 */

/*每笔交易对象*/
public class BTCtrade {
    private String id;  //Trade unique ID
    private String amount;  //Trade amount
    private String price;//Trade price
    private String direction;    //Trade type (0 - buy; 1 - sell).
    //private long time;   //Trade timestamp.
    private long ts;
    //private long tradeid;//Trade buy order id.

    public ContentValues getContentValues(){
        ContentValues contentValues=new ContentValues();
        contentValues.put("id",id);
        contentValues.put("amount",amount);
        contentValues.put("price",price);
        contentValues.put("direction",direction);
        //contentValues.put("time",time);
        contentValues.put("ts",ts);
        //contentValues.put("tradeid",tradeid);
        return contentValues;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

//    public long getTradeid() {
//        return tradeid;
//    }

//    public void setTradeid(long tradeid) {
//        this.tradeid = tradeid;
//    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

//    public long getTime() {
//        return time;
//    }
//
//    public void setTime(long time) {
//        this.time = time;
//    }


    public BTCtrade(String id, String amount, String price, String direction, long ts) {
        this.id = id;
        this.amount = amount;
        this.price = price;
        this.direction = direction;
        this.ts = ts;
    }

    public BTCtrade(){

    }
}
