package com.lazyweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 许东 on 2018/8/5.
 */

public class Province extends DataSupport {

    private int id;         //字段

    private String provinceName;   //省名

    private int provinceCode;     //省代号


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
