package com.lazyweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.lazyweather.android.db.City;
import com.lazyweather.android.db.County;
import com.lazyweather.android.db.Province;
import com.lazyweather.android.util.HttpUtil;
import com.lazyweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by 许东 on 2018/8/5.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;

    //市列表
    private List<City> cityList;

    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //当前选中的级别
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);   //初始化ArrayAdapter
        listView.setAdapter(adapter);                                                                //设置为listView的适配器
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {              //listView的点击事件
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //根据当前级别进行判断
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){         //判断当前级别是LEVEL_COUNTY
                    String weatherId = countyList.get(i).getWeatherId();
                    if (getActivity() instanceof MainActivity) {             //在碎片中调用getActivity判断当前碎片是否在MainActivity中
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);//则启动WeatherActivity
                        intent.putExtra("weather_id", weatherId);   //将当前县的天气id传入
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity) {   //在碎片中调用getActivity判断当前碎片是否在WeatherActivity中
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();           //关闭滑动菜单
                        activity.swipeRefresh.setRefreshing(true);     //显示下拉刷新进度条
                        activity.requestWeather(weatherId);              //请求新城市的天气信息
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {            //backButton的点击事件
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //查询全国所有的省，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryProvinces() {
        titleText.setText("中国");               //头布局标题设置成中国
        //visibility属性VISIBLE、INVISIBLE、GONE的区别:       VISIBLE：设置控件可见    INVISIBLE：设置控件不可见     GONE：设置控件隐藏
        // 而INVISIBLE和GONE的主要区别是：当控件visibility属性为INVISIBLE时，界面保留了view控件所占有的空间；而控件属性为GONE时，界面则不保留view控件所占有的空间。
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);       //读取省级数据接口
        if (provinceList.size() > 0) {
            dataList.clear();                                     //清空dataList中的元素
            for (Province province : provinceList) {             //遍历省数据
                dataList.add(province.getProvinceName());        //添加元素到dataList
            }
            adapter.notifyDataSetChanged();                      //当发现数据集有改变的情况，或者读取到数据的新状态时，就会调用notifyDataSetChanged()方法
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    //查询省内所有的市，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }

    }

    //查询市内所有的县，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }

    }

    //根据传入的地址和类型从服务器上查询省市县数据
    private void queryFromServer(String address, final String province) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {              //调用HttpUtil中的sendOkHttpRequest方法向服务器发送请求
            public void onResponse(Call call, Response response) throws IOException {                   //响应数据回调
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(province)) {
                    result = Utility.handleProvinceResponse(responseText);           //调用Utility中的handleProvinceResponse方法解析处理服务器返回的数据并存储到数据库
                } else if ("city".equals(province)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(province)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {               //runOnUiThread实现从子线程切换到主线程的操作
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(province)) {
                                queryProvinces();                              //直接将数据显示在主页面
                            } else if ("city".equals(province)) {
                                queryCities();
                            } else if ("county".equals(province)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread（）方法回到主线程
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}


