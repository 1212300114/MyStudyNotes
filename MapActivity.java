package net.xinhuamm.dangzhengtemp.activitys;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import net.xinhuamm.dangzhengtemp.R;
import net.xinhuamm.dangzhengtemp.view.PoiOverlay;
import net.xinhuamm.temp.base.BaseActivity;
import net.xinhuamm.temp.ioc.ViewInject;
import net.xinhuamm.temp.utils.ToastUtil;

/**
 * @说明 登录页面
 * @作者 JJJ
 * @时间 2015/11/16
 * @版权 Copyright(c) 2015 jjj-版权所有
 */
public class MapActivity extends BaseActivity implements View.OnClickListener, OnGetPoiSearchResultListener {

    @ViewInject(id = R.id.imgLeft, click = "onClick")
    private ImageView ivLeft;
    @ViewInject(id = R.id.map)
    private MapView mapView;
    @ViewInject(id = R.id.tvTitle)
    private TextView tvTitle;
    private BaiduMap baiduMap;
    private PoiSearch poiSearch;

    private LocationClient client;
    private BDLocationListener listener;

    private double longitude, latitude;
    private String key;

    @Override
    public int getLayoutResID() {
        return R.layout.activity_map;
    }

    @Override
    public void onActivityCreatedCallBack() {
        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
        baiduMap.setMyLocationEnabled(true);//设置显示我的位置
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//设置为普通地图
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration( //设置我的位置的设置
                MyLocationConfiguration.LocationMode.COMPASS, true, null));
        //init poiSearch
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(this);
//    poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").keyword("银行").pageNum(10));
//    poiSearch.searchNearby()
        client = new LocationClient(getApplicationContext());
        listener = new CustomLocationListener();
        client.registerLocationListener(listener);
        initLocationOption();
        client.start();
//        this.searchNearby("银行");
        key = getIntent().getExtras().getString("key");
        if (!TextUtils.isEmpty(key)) {
            tvTitle.setText(key);
        }
        Log.e(key, key);
    }

    //搜索的配置以及开始搜索
    private void searchNearby(String key) {
        PoiNearbySearchOption option = new PoiNearbySearchOption();
        option.location(new LatLng(latitude, longitude));
        option.keyword(key);
        option.radius(5000);
        option.pageCapacity(10);
        option.pageNum(1);
        poiSearch.searchNearby(option);
    }

    //定位的配置
    private void initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        client.setLocOption(option);
    }

    private class CustomOverlay extends PoiOverlay {
        public CustomOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {
            super.onPoiClick(i);

            PoiInfo info = getPoiResult().getAllPoi().get(i);
            poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(info.uid));
            TextView textView = new TextView(getApplicationContext());
            textView.setText(info.name);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            InfoWindow infoWindow = new InfoWindow(textView, info.location, -40);
            baiduMap.showInfoWindow(infoWindow);
            Log.e("!!!!", i + "");
            return true;
        }
    }

    private class CustomLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mapView == null) {
                return;
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            Log.e("++++", "received message" + location.getLocType() + "---" +
                    location.getAddress() + "---" + location.getAddrStr() + longitude + "-----" + latitude);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(latitude)
                    .longitude(longitude).build();
            baiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(u);
            if (!TextUtils.isEmpty(key))
                searchNearby(key);
        }
    }


    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        //获取搜索结果
        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            ToastUtil.showToast(MapActivity.this, "查询无结果");
            return;
        }
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            baiduMap.clear();
            PoiOverlay overlay = new CustomOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(poiResult);
            overlay.addToMap();
            overlay.zoomToSpan();
            ToastUtil.showToast(MapActivity.this, "共查询到" + poiResult.getTotalPoiNum() + "个结果，已为您显示最近10个");
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {


        //获取地点详情结果
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgLeft:
                finishactivity(this);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        poiSearch.destroy();
        client.stop();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        client.stop();
        super.onPause();
    }
}
