package com.lazyweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);  //从SharedPreferences文件中读取缓存数据
        if (prefs.getString("weather",null) != null){                           //如果不为空，表示已经请求过天气数据了
            Intent intent = new Intent(this,WeatherActivity.class);    //则直接跳转WeatherActivity
            startActivity(intent);
            finish();
        }
    }
}
