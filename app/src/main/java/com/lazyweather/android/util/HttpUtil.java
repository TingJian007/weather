package com.lazyweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 许东 on 2018/8/5.
 */


//和服务器进行交互
public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
