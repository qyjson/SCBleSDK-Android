package com.scble.android.sdk.enums;

import androidx.annotation.Keep;

@Keep
/**
 * 错误状态枚举
 */
public enum SCBleManagerError {
    SCBleManagerErrorUnknow("未知错误"),
    SCBleManagerErrorScanNoDevice("未搜索到设备"),
    SCBleManagerErrorConnectFail("设备连接失败"),
    SCBleManagerErrorCheckFail("设备校验失败"),
    SCBleManagerErrorBLEFail("设备不支持蓝牙"),
    SCBleManagerErrorGPSPermissionFail("GPS未开启"),
    SCBleManagerErrorBLEPermissionFail("蓝牙权限未开启"),
    SCBleManagerErrorBLEPhonePermissionFail("手机蓝牙未开启");
    private String desc;
    private SCBleManagerError(String desc){
        this.desc = desc;
    }
}
