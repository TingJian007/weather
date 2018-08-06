package com.lazyweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 许东 on 2018/8/6.
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperatrue temperature;

    @SerializedName("cond")
    public More more;

    public class Temperatrue {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
