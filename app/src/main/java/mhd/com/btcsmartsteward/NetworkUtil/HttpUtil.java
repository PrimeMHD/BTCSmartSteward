package mhd.com.btcsmartsteward.NetworkUtil;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

import mhd.com.btcsmartsteward.Bean.BTCinfo;
import mhd.com.btcsmartsteward.Bean.BTCtrade;
import mhd.com.btcsmartsteward.Bean.KLinePin;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {


    public static void sendOkHttpRequest(final String address, final Callback callback) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public BTCinfo BTCinfo_parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        BTCinfo btcinfo=gson.fromJson(jsonData,BTCinfo.class);
        return btcinfo;
    }
    public List<BTCtrade> BTCtrades_parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        return gson.fromJson(jsonData,new TypeToken<List<BTCtrade>>(){}.getType());

    }
    public List<KLinePin> KLinePin_parseJSONArrayWithGSON(String jsonData){
        Gson gson=new Gson();
        return gson.fromJson(jsonData,new TypeToken<List<KLinePin>>(){}.getType());
    }
    public KLinePin KLinePin_parseJSONWithGSON(String jsonData){
        Gson gson=new Gson();
        KLinePin kLinePin=gson.fromJson(jsonData,KLinePin.class);
        Log.d("ssssss","toJsonExecuted");
        return kLinePin;
    }

}
