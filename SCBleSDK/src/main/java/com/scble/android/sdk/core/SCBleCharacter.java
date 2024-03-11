package com.scble.android.sdk.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;
import android.util.Log;


import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.scble.android.sdk.SCBleConstant;
import com.scble.android.sdk.listener.SCBleTestListener;
import com.scble.android.sdk.net.SCBleNet;
import com.scble.android.sdk.utils.ByteUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取特征
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleCharacter {
    private static String gDeviceId = "";
    private static String gDeviceMacAddress = "";
    private static String gCiphertext = "";
    private static String gCapBaseValue = "";

    private static List<String> mCapDataList = new ArrayList<>();

    private static final int MAX_CAP_GROUP = 15;


    /**
     * @param gatt
     * @return
     */
    @SuppressLint("MissingPermission")
    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt) {
        if (gatt == null) {
            return null;
        }
        BluetoothGattCharacteristic write = null;
        //遍历所有服务
        for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
            String deviceServiceUUID = bluetoothGattService.getUuid().toString();
            try {
                if (!TextUtils.isEmpty(deviceServiceUUID) && deviceServiceUUID.contains("-")) {
                    deviceServiceUUID = deviceServiceUUID.split("-")[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(deviceServiceUUID) && deviceServiceUUID.equalsIgnoreCase(SCBleConstant.SERVICE_UUID)) {
                //遍历所有特征
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                    String bluetoothGattCharacteristicUUID = bluetoothGattCharacteristic.getUuid().toString();
                    try {
                        if (!TextUtils.isEmpty(bluetoothGattCharacteristicUUID) && bluetoothGattCharacteristicUUID.contains("-")) {
                            bluetoothGattCharacteristicUUID = bluetoothGattCharacteristicUUID.split("-")[0];
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(bluetoothGattCharacteristicUUID) && bluetoothGattCharacteristicUUID.equalsIgnoreCase(SCBleConstant.WRITE_UUID)) {
                        //根据写UUID找到写特征
                        write = bluetoothGattCharacteristic;
                    } else if (!TextUtils.isEmpty(bluetoothGattCharacteristicUUID) && bluetoothGattCharacteristicUUID.equalsIgnoreCase(SCBleConstant.READ_UUID)) {
                        //根据通知UUID找到通知特征
                        gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                    }
                }

            }
        }
        return write;
    }

    /**
     * 处理特征值
     *
     * @param characteristic
     */
    public static synchronized void dealCharacteristicChanged(BluetoothGattCharacteristic characteristic, boolean isStartReceiveCap, SCBleNet.OnNetCallback onNetCallback, SCBleTestListener scBleTestListener) {
        byte[] receiveDataByte = characteristic.getValue();
        if (receiveDataByte != null && receiveDataByte.length != 0) {
            if (scBleTestListener != null) {
                scBleTestListener.receivedData(receiveDataByte);
            }
            String receiveDataStr = ByteUtil.bytesToHexString(receiveDataByte);
            Log.e("===>SCBleSDK",receiveDataStr+"====>");
            if (receiveDataStr.contains(" ")) {
                String[] receiveDataArray = receiveDataStr.split(" ");
                if (receiveDataArray.length > 1) {
                    if (receiveDataArray[0].equals("FF") && receiveDataArray[1].equals("FF")) {
                        if (receiveDataArray.length > 2) {
                            if (receiveDataArray[2].equals("0B") || receiveDataArray[2].equals("11")) {
                                if (receiveDataArray.length > 13) {
                                    for (int i = 9; i < 14; i++) {
                                        gDeviceId = gDeviceId + receiveDataArray[i];
                                    }
                                    if (receiveDataArray.length > 19) {
                                        for (int i = 14; i < 20; i++) {
                                            gDeviceMacAddress = gDeviceMacAddress + receiveDataArray[i];
                                        }
                                    }
                                    SCBleNet.requestGetDeviceCiphertext(gDeviceId, gDeviceMacAddress, new SCBleNet.OnNetCallback() {
                                        @Override
                                        public void success(String data, String api) {
                                            if (onNetCallback != null) {
                                                onNetCallback.success(data, api);
                                            }
                                        }

                                        @Override
                                        public void failed(int code, String description) {
                                            if (onNetCallback != null) {
                                                onNetCallback.failed(code, description);
                                            }
                                        }
                                    });
                                }
                            } else if (receiveDataArray[2].equals("10") || receiveDataArray[2].equals("16")) {
                                int[] newArray = ByteUtil.strArrToIntArrByHex(receiveDataArray);
                                gCiphertext = new Gson().toJson(newArray);
                                SCBleNet.requestDeviceCheck(gCiphertext, gDeviceMacAddress, new SCBleNet.OnNetCallback() {
                                    @Override
                                    public void success(String data, String api) {
                                        gCiphertext = data;
                                        if (onNetCallback != null) {
                                            onNetCallback.success(data, api);
                                        }
                                    }

                                    @Override
                                    public void failed(int code, String description) {
                                        if (onNetCallback != null) {
                                            onNetCallback.failed(code, description);
                                        }
                                    }
                                });

                            }
                        }
                        return;
                    }

                    //是否开始接收水分值
                    if (isStartReceiveCap) {
                        if (mCapDataList.size() >= MAX_CAP_GROUP) {
                            isStartReceiveCap = false;
                            return;
                        }
                        String capValue = getCapValue(receiveDataArray);
                        mCapDataList.add(capValue);
                        if (mCapDataList.size() == MAX_CAP_GROUP) {
                            List<String> newCapDataList = ByteUtil.keepMostFrequentIntegerPart(mCapDataList);
                            gCapBaseValue = String.valueOf(ByteUtil.calculateAverage(newCapDataList));
                            if (onNetCallback != null) {
                                onNetCallback.success("1", SCBleConstant.API_CAP_GET);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 处理水分数据
     *
     * @param receiveArray
     */
    private static String getCapValue(String[] receiveArray) {
        String capValue = "";
        if (receiveArray != null && receiveArray.length != 0) {
            //水分的整数部分
            String capIntegerPart = "";
            //水分的小数部分
            String capFloatPart = "";
            if (receiveArray.length > 8) {
                String tempCapIntegerPart = receiveArray[8] + receiveArray[7];
                capIntegerPart = ByteUtil.hexStrTo10(tempCapIntegerPart);
            }
            if (receiveArray.length > 12) {
                String tempCapFloatPart = receiveArray[12] + receiveArray[11] + receiveArray[10] + receiveArray[9];
                capFloatPart = ByteUtil.hexStrTo10(tempCapFloatPart);
                if (capFloatPart.length() < 6) {
                    capFloatPart = "0" + capFloatPart;
                }
            }
            capValue = capIntegerPart + "." + capFloatPart;
        }
        return capValue;
    }

    public static String getCiphertext() {
        return gCiphertext;
    }

    public static String getCapBaseValue() {
        return gCapBaseValue;
    }

    /**
     * 释放资源
     */
    public static void release() {
        gCapBaseValue = "";
        gCiphertext = "";
        mCapDataList.clear();
    }

}
