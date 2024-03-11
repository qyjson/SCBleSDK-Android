package com.scble.android.sdk;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.scble.android.sdk.bean.DetectionBean;
import com.scble.android.sdk.core.SCBleCore;
import com.scble.android.sdk.enums.SCBleManagerState;
import com.scble.android.sdk.listener.SCBleListener;
import com.scble.android.sdk.listener.SCBleTestListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SDK入口
 */
public class SCBleSDK {
    private String mApiKey;
    private String mMemberGuid;
    private HashMap<String, Object> mExtraData;

    private SCBleCore mScBleCore;

    private Context mContext;

    private SCBleListener mScBleListener;

    private static SCBleSDK INSTANCE;


    @Keep
    /**
     * SDK 初始化
     *
     * @param apiKey
     * @param memberGuid
     * @param extraData
     */
    public static void init(Context context, String apiKey, String memberGuid, SCBleListener scBleListener) {
        if (INSTANCE == null) {
            INSTANCE = new SCBleSDK();
        }
        INSTANCE.mApiKey = apiKey;
        INSTANCE.mMemberGuid = memberGuid;
        INSTANCE.mContext = context;
        INSTANCE.mScBleListener = scBleListener;
    }
    @Keep
    /**
     * 开始连接设备
     */
    public static void startConnect() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        INSTANCE.mScBleCore = SCBleCore.getInstance().init(INSTANCE.mContext, INSTANCE.mScBleListener);
        INSTANCE.mScBleCore.prepareScan();
    }

    @Keep
    /**
     * 开始测试
     */
    public static void startTest(SCBleTestListener scBleTestListener) {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        if (scBleTestListener != null) {
            scBleTestListener.start();
        }
        INSTANCE.mScBleCore.setSCBleTestListener(scBleTestListener);
    }

    @Keep
    /**
     * 断开连接设备
     */
    public static void disconnectDevice() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        INSTANCE.mScBleCore.disConnect();
    }

    @Keep
    /**
     * 开启设备灯光
     */
    public static void openLight() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        INSTANCE.mScBleCore.sendCmd(SCBleConstant.CMD_LIGHT_MODE_WHITE);
    }

    @Keep
    /**
     * 开启设备灯光-指定
     */
    public static void openLightWithCmd(byte[] cmdByte) {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        INSTANCE.mScBleCore.sendCmd(cmdByte);
    }


    @Keep
    /**
     * 关闭设备灯光
     */
    public static void closeLight() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        INSTANCE.mScBleCore.setAllowDeviceCheck(false);
        INSTANCE.mScBleCore.sendCmd(SCBleConstant.CMD_LIGHT_MODE_OFF);
    }


    @Keep
    /**
     * 获取检测部位
     */
    public static List<DetectionBean> testRegions() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return null;
        }
        List<DetectionBean> detectionBeanList = new ArrayList<>();
        DetectionBean detectionForeHead = new DetectionBean();
        detectionForeHead.setDetectionPosition("forehead");
        detectionForeHead.setDetectionPositionName("前额");
        detectionBeanList.add(detectionForeHead);

        DetectionBean detectionCanthus = new DetectionBean();
        detectionCanthus.setDetectionPosition("canthus");
        detectionCanthus.setDetectionPositionName("眼角");
        detectionBeanList.add(detectionCanthus);

        DetectionBean detectionNoseWing = new DetectionBean();
        detectionNoseWing.setDetectionPosition("nose_wing");
        detectionNoseWing.setDetectionPositionName("鼻翼");
        detectionBeanList.add(detectionNoseWing);

        DetectionBean detectionCheek = new DetectionBean();
        detectionCheek.setDetectionPosition("cheek");
        detectionCheek.setDetectionPositionName("脸颊");
        detectionBeanList.add(detectionCheek);

        DetectionBean detectionMouth = new DetectionBean();
        detectionMouth.setDetectionPosition("mouth_corner");
        detectionMouth.setDetectionPositionName("嘴角");
        detectionBeanList.add(detectionMouth);

        return detectionBeanList;
    }

    public static String getApiKey() {
        if (INSTANCE == null) {
            return null;
        }
        return INSTANCE.mApiKey;
    }

    public static String getMemberGuid() {
        if (INSTANCE == null) {
            return null;
        }
        return INSTANCE.mMemberGuid;
    }

    @Keep
    public static void release() {
        if (INSTANCE == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 未初始化SDK");
            return;
        }
        if (INSTANCE.mScBleCore == null) {
            Log.e(TAG, "SCBleSDK ERROR ===> 请先连接设备");
            return;
        }
        INSTANCE.mScBleCore.release();
    }


}