package com.lazyweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 许东 on 2018/8/6.
 */


public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
