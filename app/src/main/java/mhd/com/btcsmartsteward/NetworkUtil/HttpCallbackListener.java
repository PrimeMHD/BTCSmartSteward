package mhd.com.btcsmartsteward.NetworkUtil;

public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);

}