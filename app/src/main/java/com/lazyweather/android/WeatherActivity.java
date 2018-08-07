package com.lazyweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lazyweather.android.gson.Forecast;
import com.lazyweather.android.gson.Weather;
import com.lazyweather.android.service.AutoUpdateService;
import com.lazyweather.android.util.HttpUtil;
import com.lazyweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 许东 on 2018/8/6.
 */

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public Button navButton;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {   //当前版本号大于等于21（5.0及以上的系统）才继续执行下面的代码
            View decorView = getWindow().getDecorView();    //调用 getWindow().getDecorView()方法拿到当前活动的decorView
            decorView.setSystemUiVisibility(                  //调用setSystemUiVisibility改变系统UI的显示
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);  //活动的布局显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);  //将状态栏设置成透明
        }
        setContentView(R.layout.activity_weather);


        //初始化各控件
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//设置下拉刷新进度条的颜色
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);   //打开滑动菜单
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic",null);        //从SharedPreferences中读取缓存的背景图片
        if (bingPic != null){                                            //如果有缓存的话
            Glide.with(this).load(bingPic).into(bingPicImg);    //则使用Glide来加载这张图片
        }else {
            loadBingPic();                                               //没有，则调用loadBingPic方法请求今日的必应背景图
        }
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;            //定义mWeatherId变量用来记录城市的天气id
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");              //从Intent从取出天气id
            weatherLayout.setVisibility(View.INVISIBLE);                               //ScrollView设置不可见
            requestWeather(mWeatherId);                                                 //调用requestWeather方法从服务器请求天气数据
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {    //设置下拉刷新的监听器
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);       //调用requestWeather请求天气信息
            }
        });


    }

    //加载背景图
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {               //通过HttpUtil中的sendOkHttpRequest获取到必应背景图的链接
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();                 //返回字符串给bingPic
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();  //将连接缓存到SharedPreferences中
                editor.putString("bing_pic", bingPic);        //向键为bing_pic中的Editor中放入值 bingPic
                editor.apply();//提交preference的修改数据，，，，apply没有返回值而commit返回boolean表明修改是否提交成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {    //切换主线程
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);     //使用Glide加载图片，WeatherActivity.this这样在每次请求天气时也会自动刷新图片
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }


    //根据天气id请求城市天气信息
    void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=f615e41f9a124f1eb975be5cc23270cb";       //拼装出接口地址
        this.mWeatherId = weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {                  //调用HttpUtil中的sendOkHttpRequest方法来向接口地址发出请求
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();                           //回调，返回字符串
                final Weather weather = Utility.handleWeatherResponse(responseText);           //将返回的JSON数据转换成Weather对象
                runOnUiThread(new Runnable() {                                                 //当前线程切换至主线程
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {                 //说明请求天气成功
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();//将返回的数据缓存到SharedPreferences中
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);                   //调用showWeatherInfo方法进行内容显示
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);     //false表示刷新事件结束，隐藏刷新进度条
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        //从Weather对象中获取数据，然后显示到相应的控件
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {            //for循环处理每天的天气信息
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);  //动态加载forecast_item.xml布局并设置相应的数据
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);    //添加到父布局中
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);  //启动AutoUpdateService服务


    }
}
