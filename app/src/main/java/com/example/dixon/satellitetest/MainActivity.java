package com.example.dixon.satellitetest;

//import android.support.v7..app.AppCompatActivity;
import android.os.Bundle;

import java.util.Iterator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {
    private EditText editText;
    private LocationManager lm;
    private static final String TAG="GpsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=(EditText)findViewById(R.id.editText);
        editText.setText("Standing by\n");
        lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //判斷GPS是否正常启動
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "請開启GPS導航...", Toast.LENGTH_SHORT).show();
            //返回開启GPS導航設置界面
            /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);*/
            //return;
        }
        //为獲取地理位置信息時設置查詢條件
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        //獲取位置信息
        //如果不設置查詢要求，getLastKnownLocation方法傳人的参數为LocationManager.GPS_PROVIDER
        Location location= lm.getLastKnownLocation(bestProvider);
        //updateView(location);
        //監聽狀態
        lm.addGpsStatusListener(listener);
        //绑定監聽，有4個参數
        //参數1，設備：有GPS_PROVIDER和NETWORK_PROVIDER兩種
        //参數2，位置信息更新周期，單位毫秒
        //参數3，位置變化最小距離：當位置距離變化超過此值時，將更新位置信息
        //参數4，監聽
        //備注：参數2和3，如果参數3不为0，則以参數3为准；参數3为0，則通過時間來定時更新；兩者为0，則隨時刷新

        // 1秒更新一次，或最小位移變化超過1米更新一次；
        //注意：此處更新准確度非常低，推薦在service裏面启動一個Thread，在run中sleep(10000);然後執行handler.sendMessage(),更新位置
        lm.requestLocationUpdates(bestProvider, 3000, 1, locationListener);
    }

    //位置監聽
    private LocationListener locationListener=new LocationListener() {

        /**
         * 位置信息變化時觸發
         */
        public void onLocationChanged(Location location) {
            //updateView(location);
            Log.i(TAG, "時間："+location.getTime());
            Log.i(TAG, "經度："+location.getLongitude());
            Log.i(TAG, "緯度："+location.getLatitude());
            Log.i(TAG, "海拔："+location.getAltitude());
        }

        /**
         * GPS狀態變化時觸發
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS狀態为可見時
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "當前GPS狀態为可見狀態");
                    break;
                //GPS狀態为服務區外時
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "當前GPS狀態为服務區外狀態");
                    break;
                //GPS狀態为暫停服務時
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "當前GPS狀態为暫停服務狀態");
                    break;
            }
        }

        /**
         * GPS開启時觸發
         */
        public void onProviderEnabled(String provider) {
            Location location=lm.getLastKnownLocation(provider);
            //updateView(location);
        }

        /**
         * GPS禁用時觸發
         */
        public void onProviderDisabled(String provider) {
            //updateView(null);
        }
    };

    //狀態監聽
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    editText.setText("First fix：\n");
                    break;
                //衛星狀態改變
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "衛星狀態改變");
                    editText.setText("Satellites:");
                    //獲取當前狀態
                    GpsStatus gpsStatus=lm.getGpsStatus(null);
                    //獲取衛星顆數的默認最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //創建一個迭代器保存所有衛星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        editText.append("\nAzi:");
                        editText.append(String.valueOf(s.getAzimuth()));
                        editText.append("    Ele:");
                        editText.append(String.valueOf(s.getElevation()));
                        editText.append("    PRN:");
                        editText.append(String.valueOf(s.getPrn()));
                        editText.append("    SNR:");
                        editText.append(String.valueOf(s.getSnr()));
                        count++;
                    }
                    editText.append("\nCount："+count);
                    break;
                //定位启動
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启動");
                    editText.setText("GPS on\n");
                    break;
                //定位結束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位結束");
                    editText.setText("GPS off\n");
                    break;
            }
        }
    };

    /**
     * 返回查詢條件
     * @return
     */
    private Criteria getCriteria(){
        Criteria criteria=new Criteria();
        //設置定位精確度 Criteria.ACCURACY_COARSE比較粗略，Criteria.ACCURACY_FINE則比較精細
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //設置是否要求速度
        criteria.setSpeedRequired(false);
        // 設置是否允許運營商收費
        criteria.setCostAllowed(false);
        //設置是否需要方位信息
        criteria.setBearingRequired(false);
        //設置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 設置對電源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }
}
