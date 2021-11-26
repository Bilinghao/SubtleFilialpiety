package com.example.weatherforecast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.bumptech.glide.Glide;
import com.example.weatherforecast.bean.Weather;
import com.example.weatherforecast.util.HttpUtil;
import com.google.gson.Gson;
import com.qweather.plugin.bean.hew.Lifestyle;
import com.qweather.plugin.view.HorizonView;
import com.qweather.plugin.view.LeftLargeView;
import com.qweather.plugin.view.QWeatherConfig;
import com.qweather.sdk.bean.IndicesBean;
import com.qweather.sdk.bean.WarningBean;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.IndicesType;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private LocationClient mLocationClient = null;
    private MyLocationListener myLocationListener = new MyLocationListener();

    private double longitude, latitude;
    private String locationID;
    private String addr;

    private Button btnLoc;
    private ImageView imgBingPic;
    private TextView tvTitleCity, tvTitleUpdateTime;
    private TextView tvNowTmp, tvNowTextDay, tvNowTmpAll, tvNowWind;
    private TextView tvDate7d, tvText7d, tvTmpMax7d, tvTmpMin7d;
    private TextView tvAqi, tvPm25;
    private TextView tvWarningTitle, tvWarningTime, tvWarningText;
    private TextView tvMoveIndex, tvWearIndex, tvComfortIndex, tvUvIndex;

    private LinearLayout weather7dLayout;

    private Weather weather;
    private List<Weather> weatherList;
    private List<IndicesType> types = new ArrayList<IndicesType>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        setContentView(R.layout.activity_main);
        loadBingPic();

        HeConfig.init("HE2111241527371613",  "a4076b12f26b47fba22c91c71b4fd903");
        HeConfig.switchToDevService(); //切换开发版服务

        initViews();

        verifyPermission();  //验证权限是否获取，开启定位
        initLocation();
//        //放到onReceiveLocation中
//        initHeWeather();



    }

    private void getBingImg() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = sharedPreferences.getString("bing_pic", null);
        if (bingPic != null) {

            Glide.with(this).load(bingPic).into(imgBingPic);
        } else {
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(imgBingPic);
                    }
                });
            }
        });
    }

    private void initViews() {
        imgBingPic = (ImageView) findViewById(R.id.img_bing_pic);
        btnLoc = findViewById(R.id.btn_title_location);

        tvTitleCity = findViewById(R.id.tv_title_city);
        tvTitleUpdateTime = findViewById(R.id.tv_title_update_time);
        tvNowTmp = findViewById(R.id.tv_tmp_now);
        tvNowTextDay = findViewById(R.id.tv_text_day);
        tvNowTmpAll = findViewById(R.id.tv_tmp_all);
        tvNowWind = findViewById(R.id.tv_wind);
        tvAqi = findViewById(R.id.tv_aqi);
        tvPm25 = findViewById(R.id.tv_pm25);
        tvWarningTitle = findViewById(R.id.tv_warning_title);
        tvWarningTime = findViewById(R.id.tv_warning_time);
        tvWarningText = findViewById(R.id.tv_warning_text);
        tvMoveIndex = findViewById(R.id.tv_move_index);
        tvComfortIndex = findViewById(R.id.tv_comfort_index);
        tvWearIndex = findViewById(R.id.tv_wear_index);
        tvUvIndex = findViewById(R.id.tv_uv_index);

        weather7dLayout = findViewById(R.id.layout_weather7d);

    }

    private void initHeWeather() {

        locationID = longitude + "," + latitude;
        weather = new Weather();

//        types = new ArrayList<IndicesType>();

        if (types.size() == 0) {
            types.add(IndicesType.COMF);
            types.add(IndicesType.SPT);
            types.add(IndicesType.DRSG);
            types.add(IndicesType.UV);
        }

        QWeather.getGeoCityLookup(getApplicationContext(), locationID , new QWeather.OnResultGeoListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("initHeWeather", "error" + throwable);
            }

            @Override
            public void onSuccess(GeoBean geoBean) {
                Log.e("initHeWeather", "success" + geoBean.getCode() + geoBean.getLocationBean().size() );
                List<GeoBean.LocationBean> locationBeanList = geoBean.getLocationBean();
                Log.e("getGeo", locationBeanList.get(0).getName());
                weather.setTitle(locationBeanList.get(0).getName());
                tvTitleCity.setText(weather.getTitle());

//                for (GeoBean.LocationBean locationBean : locationBeanList) {
//                    Log.e("initHeWeather" ,locationBean.getCountry() + locationBean.getAdm1() + locationBean.getAdm2()  + locationBean.getName());
//                    locationID = locationBean.getId();
//                }
            }
        });

        QWeather.getWeatherNow(getApplicationContext(), locationID, Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("getWeather", "weather error:" + throwable);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherNowBean) {
                Log.e("getWeather", "weather success:" + new Gson().toJson(weatherNowBean));
                if (Code.OK == weatherNowBean.getCode()) {
                    WeatherNowBean.NowBaseBean now = weatherNowBean.getNow();
                    weather.setUpdateTime(now.getObsTime().substring(11, 16));
                    weather.setTempNow(now.getTemp());
                    weather.setTextDay(now.getText());
                    weather.setWindDir(now.getWindDir());
                    weather.setWindScale(now.getWindScale());

                    tvTitleUpdateTime.setText(now.getObsTime().substring(11, 16));
                    tvNowTextDay.setText(now.getText());
                    tvNowTmp.setText(now.getTemp() + "º");
//                    tvNowTmpAll.setText(weather.getTempMax() + "º" + "  " + weather.getTempMin() + "º");
                    tvNowWind.setText("风向:" + now.getWindDir() + "  风力:" + now.getWindScale());

                } else {
                    //在此查看返回数据失败的原因
                    Code code = weatherNowBean.getCode();
                    Log.e("getWeather", "failed code: " + code);
                }
            }
        });

        QWeather.getWeather7D(getApplicationContext(), locationID, Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("qwe7d", "error：" + throwable);
            }

            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                List<WeatherDailyBean.DailyBean> dailyBeanList = weatherDailyBean.getDaily();
                weather.setTempMax(dailyBeanList.get(0).getTempMax());
                weather.setTempMin(dailyBeanList.get(0).getTempMin());
                tvNowTmpAll.setText(dailyBeanList.get(0).getTempMax() + "º" + "  " + dailyBeanList.get(0).getTempMin() + "º");

                weatherList = new ArrayList<Weather>();
                for (WeatherDailyBean.DailyBean dailyBean : dailyBeanList) {
                    weather = new Weather();
                    weather.setDate(dailyBean.getFxDate());
                    weather.setTextDay(dailyBean.getTextDay());
                    weather.setTempMax(dailyBean.getTempMax());
                    weather.setTempMin(dailyBean.getTempMin());
                    weatherList.add(weather);

                    View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.weather7d_item, weather7dLayout, false);
                    tvDate7d = view.findViewById(R.id.tv_date_7day);
                    tvText7d = view.findViewById(R.id.tv_text_7day);
                    tvTmpMax7d = view.findViewById(R.id.tv_tmp_max_7day);
                    tvTmpMin7d = view.findViewById(R.id.tv_tmp_min_7day);
                    tvDate7d.setText(dailyBean.getFxDate().substring(5,10));
                    tvText7d.setText(dailyBean.getTextDay() + "º");
                    tvTmpMax7d.setText(dailyBean.getTempMax() + "º");
                    tvTmpMin7d.setText(dailyBean.getTempMin() + "º");
                    weather7dLayout.addView(view);

                }
//                showWeather7dInfo(weatherList);

            }
        });

        QWeather.getAirNow(getApplicationContext(), locationID, Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("getAirNow", "error:" + throwable);
            }

            @Override
            public void onSuccess(AirNowBean airNowBean) {
                weather.setAqi(airNowBean.getNow().getAqi());
                weather.setPm25(airNowBean.getNow().getPm2p5());

                tvAqi.setText(airNowBean.getNow().getAqi());
                tvPm25.setText(airNowBean.getNow().getPm2p5());
            }
        });

        QWeather.getIndices1D(getApplicationContext(),locationID,Lang.ZH_HANS, types,new QWeather.OnResultIndicesListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("getIndices1d", "error：" + throwable);
            }

            @Override
            public void onSuccess(IndicesBean indicesBean) {

//                weather.setComfortIndex(indicesBean.getDailyList().get(8).getCategory());
//                weather.setMoveIndex(indicesBean.getDailyList().get(1).getCategory());
//                weather.setWearIndex(indicesBean.getDailyList().get(3).getCategory());
//                weather.setUvIndex(indicesBean.getDailyList().get(5).getCategory());

                tvComfortIndex.setText(indicesBean.getDailyList().get(0).getCategory());
                tvWearIndex.setText(indicesBean.getDailyList().get(1).getCategory());
                tvMoveIndex.setText(indicesBean.getDailyList().get(2).getCategory());
                tvUvIndex.setText(indicesBean.getDailyList().get(3).getCategory());

            }
        });

        QWeather.getWarning(getApplicationContext(), locationID, Lang.ZH_HANS, new QWeather.OnResultWarningListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("getWarning", "error:" + throwable);
            }

            @Override
            public void onSuccess(WarningBean warningBean) {
                if (warningBean.getWarningList().size() > 0 ) {
                    tvWarningTitle.setText(warningBean.getWarningList().get(0).getTitle());
                    tvWarningTime.setText(warningBean.getWarningList().get(0).getPubTime());
                    tvWarningText.setText(warningBean.getWarningList().get(0).getText());
                } else {
                    Log.e("getWarning", "code" + warningBean.getCode());
                }

            }
        });

//        showWeatherInfo(weather);

    }

    private void showWeather7dInfo(List<Weather> weatherList) {
        for (Weather weather : weatherList) {
            View view = LayoutInflater.from(this).inflate(R.layout.weather7d_item, weather7dLayout, false);
            tvDate7d = view.findViewById(R.id.tv_date_7day);
            tvText7d = view.findViewById(R.id.tv_text_7day);
            tvTmpMax7d = view.findViewById(R.id.tv_tmp_max_7day);
            tvTmpMin7d = view.findViewById(R.id.tv_tmp_min_7day);
            tvDate7d.setText(weather.getDate());
            tvText7d.setText(weather.getTextDay() + "º");
            tvTmpMax7d.setText(weather.getTempMax() + "º");
            tvTmpMin7d.setText(weather.getTempMin() + "º");
            weather7dLayout.addView(view);
        }


    }

    private void showWeatherInfo(Weather weather) {
        //title
        tvTitleCity.setText(weather.getTitle());
        tvTitleUpdateTime.setText(weather.getUpdateTime());
        //now
        tvNowTextDay.setText(weather.getTextDay());
        tvNowTmp.setText(weather.getTempNow() + "º");
        tvNowTmpAll.setText(weather.getTempMax() + "º" + "  " + weather.getTempMin() + "º");
        tvNowWind.setText("风向:" + weather.getWindDir() + "  风力:" + weather.getWindScale());
        //aqi
        tvAqi.setText(weather.getAqi());
        tvPm25.setText(weather.getPm25());
        //life
        tvComfortIndex.setText(weather.getComfortIndex());
        tvMoveIndex.setText(weather.getMoveIndex());
        tvWearIndex.setText(weather.getWearIndex());
        tvUvIndex.setText(weather.getUvIndex());
        //warning
        tvWarningTitle.setText(weather.getWarningTitle());
        tvWarningTime.setText(weather.getWarningTime());
        tvWarningText.setText(weather.getWarningText());

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        option.setScanSpan(5000); //设置请求间隔
        option.setOpenGps(true);
        option.setIsNeedAddress(true);  //地址信息
        option.setNeedNewVersionRgc(true);  //新版本地址信息
        option.setIsNeedLocationDescribe(true); //地区描述
        mLocationClient.setLocOption(option);
    }

    //验证权限
    private void verifyPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }
    //开启位置请求
    private void requestLocation() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //经度
            float radius = bdLocation.getRadius(); //获取定位精度
            String coorType = bdLocation.getCoorType(); //坐标类型
            int errorCode = bdLocation.getLocType(); //定位类型、错误码
            String locType = null;
            String addr = bdLocation.getAddrStr();
            String country = bdLocation.getCountry();
            String province = bdLocation.getProvince();
            String city = bdLocation.getCity();
            String district = bdLocation.getDistrict(); //区县
            String street = bdLocation.getStreet(); //街道
            String adcode = bdLocation.getAdCode();
            String town = bdLocation.getTown(); //乡镇
            String locDescribe = bdLocation.getLocationDescribe();  //地区描述

            if (errorCode == BDLocation.TypeGpsLocation) {
                locType = "GPS";
            } else if (errorCode == BDLocation.TypeNetWorkLocation){
                locType = "网络";
                latitude = bdLocation.getLatitude(); //纬度
                longitude = bdLocation.getLongitude();
                initHeWeather();
                btnLoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alterDialogShow();
                    }
                });

            } else {
                locType = String.valueOf(errorCode);
            }
//            Log.e("location","经度：" + longitude + "纬度：" + latitude + "定位精度：" + radius + "坐标类型：" + coorType + "请求方式：" + locType);
//            Log.e("locationDetail", "addr：" + addr + "," + "地址：" + country + province + city + district + street + "adcode:" + adcode + "town:" + town);
//            Log.e("locationDescribe","地区描述"  + locDescribe);



        }
    }

    private void alterDialogShow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        QWeather.getGeoCityLookup(getApplicationContext(), locationID , new QWeather.OnResultGeoListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("initHeWeather", "error" + throwable);
            }

            @Override
            public void onSuccess(GeoBean geoBean) {
                Log.e("initHeWeather", "success" + geoBean.getCode() + geoBean.getLocationBean().size() );
                List<GeoBean.LocationBean> locationBeanList = geoBean.getLocationBean();
                dialog.setTitle("定位提示：");
                dialog.setMessage("您当前的位置是" + locationBeanList.get(0).getName() + "\n" + "是否切换？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (locationBeanList.get(0).getName().equals(tvTitleCity.getText())) {
                            weather7dLayout.removeAllViews();
                            initHeWeather();
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}