package com.lazyweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 许东 on 2018/8/5.
 */


//和服务器进行交互
public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();                           //创建OkHttpClient对象
        Request request = new Request.Builder()
                .url(address)                                               //请求接口，传参
                .build();                                                   //创建Request对象
        client.newCall(request).enqueue(callback);                          //得到Request对象，传入1回调
    }
}
