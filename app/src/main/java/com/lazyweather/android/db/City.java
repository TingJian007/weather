package com.lazyweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 许东 on 2018/8/5.
 */

public class City extends DataSupport {

    private int id;

    private String cityName;       //市名

    private int cityCode;          //市代号

    private int provinceId;          //市所属的省id


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
