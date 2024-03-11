package com.scble.android.sdk.enums;

import androidx.annotation.Keep;

@Keep
/**
 * 设备状态枚举
 */
public enum SCBleManagerState{
    SCBleManagerStateScaning("正在扫描中"),
    SCBleManagerStateConnecting("正在连接设备"),
    SCBleManagerStateChecking("正在校验设备"),
    SCBleManagerStateConnected("设备已连接"),
    SCBleManagerStateDisconnected("设备已断开连接");
    private String desc;
    private SCBleManagerState(String desc){
        this.desc = desc;
    }
}
