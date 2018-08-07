package android.sunshow.net.deviceinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int AMAP_REQUEST_CODE = 201;

    TextView tv_brand;
    TextView tv_code;

    TextView tv_imei;
    TextView tv_android_version;

    TextView tv_location_longitude;
    TextView tv_location_latitude;
    TextView tv_location_address;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = (aMapLocation) -> {
        Log.e(TAG, String.format("address=%s, longitude=%s, latitude=%s", aMapLocation.getAddress(), aMapLocation.getLongitude(), aMapLocation.getLatitude()));

        tv_location_longitude.setText(String.valueOf(aMapLocation.getLongitude()));
        tv_location_latitude.setText(String.valueOf(aMapLocation.getLatitude()));
        tv_location_address.setText(aMapLocation.getAddress());
    };

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String[] mAMapPermissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            // Manifest.permission.WRITE_EXTERNAL_STORAGE,
            // Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_brand = findViewById(R.id.tv_brand);
        tv_code = findViewById(R.id.tv_code);
        tv_imei = findViewById(R.id.tv_imei);
        tv_android_version = findViewById(R.id.tv_android_version);
        tv_location_longitude = findViewById(R.id.tv_location_longitude);
        tv_location_latitude = findViewById(R.id.tv_location_latitude);
        tv_location_address = findViewById(R.id.tv_location_address);

        Button btnTest = findViewById(R.id.btn_test);
        btnTest.setOnClickListener(v -> {
            boolean allGranted = true;

            for (String permission : mAMapPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }

            if (allGranted) {
                startTestDeviceInfo();
            } else {
                ActivityCompat.requestPermissions(this, mAMapPermissions, AMAP_REQUEST_CODE);
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void startTestDeviceInfo() {
        // location
        {
            //初始化定位
            mLocationClient = new AMapLocationClient(getApplicationContext());
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);

            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取一次定位结果：
            //该方法默认为false。
            mLocationOption.setOnceLocation(true);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置是否允许模拟位置,默认为true，允许模拟位置
            mLocationOption.setMockEnable(true);

            mLocationClient.setLocationOption(mLocationOption);

            mLocationClient.startLocation();
        }

        tv_brand.setText(Build.BRAND);
        tv_code.setText(Build.MODEL);
        tv_android_version.setText(Build.VERSION.RELEASE);

        // IMEI
        {
            try {
                final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    tv_imei.setText(tm.getDeviceId());
                }
            } catch (Exception e) {
                Log.e(TAG, "读取IMEI出错");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AMAP_REQUEST_CODE) {
            // 省略掉了对应权限判断和授权结果判断
            startTestDeviceInfo();
        }
    }
}
