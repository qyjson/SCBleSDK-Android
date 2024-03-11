package com.scble.android.sdk.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.annotation.RestrictTo;
import androidx.core.app.ActivityCompat;

/**
 * BLE检查权限工具类
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BleUtil {

    /**
     * 检查GPS是否打开
     * 根据Android 官方要求使用蓝牙时，需同时打开定位权限
     */
    public static boolean checkGPS(Context context) {
        if (context == null) {
            return false;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

    /**
     * 检查蓝牙权限
     * @param context
     * @return
     */
    public static boolean checkBlueTooth(Context context) {
        if (context == null) {
            return false;
        }
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }


}