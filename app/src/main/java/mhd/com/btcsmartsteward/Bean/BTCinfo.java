package mhd.com.btcsmartsteward.Bean;

/**
 * Created by Mi HD on 2017/12/13.
 */

/*24小时的行情数据
* 不需要入库
* */

public class BTCinfo {
    private long id; //消息id,
    private long ts;//24小时统计时间,
    private float amount; //24小时成交量,
    private float open; //前推24小时成交价,
    private float close; //当前成交价,
    private float high; //近24小时最高价,
    private float low; //近24小时最低价,
    private long count; //近24小时累积成交数,
    private float vol; //近24小时累积成交额, 即 sum(每一笔成交价 * 该笔的成交量)

    public BTCinfo(long id, long ts, float amount, float open, float close, float high, float low, long count, float vol) {
        this.id = id;
        this.ts = ts;
        this.amount = amount;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.count = count;
        this.vol = vol;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public float getVol() {
        return vol;
    }

    public void setVol(float vol) {
        this.vol = vol;
    }
}
