package com.lazyweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 许东 on 2018/8/6.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
