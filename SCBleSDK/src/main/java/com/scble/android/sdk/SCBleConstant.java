package com.scble.android.sdk;

import androidx.annotation.RestrictTo;

/**
 * 相关常量
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleConstant {
    //关闭灯光
    public static final byte[] CMD_LIGHT_MODE_OFF = new byte[]{(byte) 0xF0};
    //日光
    public static final byte[] CMD_LIGHT_MODE_WHITE = new byte[]{(byte) 0xF1};
    //偏振
    public static final byte[] CMD_LIGHT_MODE_PARALLEL = new byte[]{(byte) 0xF2};
    //混合偏振
    public static final byte[] CMD_LIGHT_MODE_CROSS = new byte[]{(byte) 0xF4};
    //UV
    public static final byte[] CMD_LIGHT_MODE_UV = new byte[]{(byte) 0xF8};

    //获取设备标识ID和设备MAC地址
    public static final byte[] CMD_GET_DEVICE_INFO = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x06, (byte) 0x49, (byte) 0x64, (byte) 0x43, (byte) 0x6F, (byte) 0x64, (byte) 0x65};
    //要连接的设备名
    public static final String DEVICE_BLUETOOTH_NAME = "SkinCam";
    //主服务的uuid
    public static final String SERVICE_UUID = "0000FF00";
    //特征写的uuid
    public static final String WRITE_UUID = "0000FF02";
    //特征读的uuid
    public static final String READ_UUID = "0000FF01";
    //接口前缀
    public static final String API_PREFIX = "https://panddi.co/api/v1.0";
    public static final String API_DEVICE_CHECK = "device.check";
    public static final String API_DEVICE_CYPHER = "device.ciphertext.get";
    public static final String API_CAP_GET = "device.cap.get";
    public static final String API_SKIN_ANALYSIS = "skin.analysis.slice";
    public static final String API_SKIN_ANALYSIS_POLLING = "skin.analysis.slice.polling";
    public static final String API_SKIN_ANALYSIS_FINISH = "skin.analysis.finish";



}
