package com.scble.android.sdk.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;


import androidx.annotation.RestrictTo;

import com.scble.android.sdk.SCBleConstant;
import com.scble.android.sdk.enums.SCBleManagerError;
import com.scble.android.sdk.enums.SCBleManagerState;
import com.scble.android.sdk.listener.SCBleListener;
import com.scble.android.sdk.listener.SCBleTestListener;
import com.scble.android.sdk.net.SCBleNet;
import com.scble.android.sdk.utils.BleUtil;
import com.scble.android.sdk.utils.ByteUtil;

import java.util.Arrays;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleCore {
    private Context mContext;
    private SCBleListener mSCBleListener;
    private BluetoothAdapter gBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBlueDevice;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristicWrite;
    private boolean mIsAllowDeviceCheck = true;
    private boolean mIsDeviceChecked = false;
    private boolean mIsCapReceived = false;
    private boolean mIsCapStartGet = false;
    private SCBleTestListener mSCBleTestListener;

    public void setSCBleTestListener(SCBleTestListener mSCBleTestListener) {
        this.mSCBleTestListener = mSCBleTestListener;
    }

    private volatile static SCBleCore instance = null;

    private SCBleCore() {
    }

    public static SCBleCore getInstance() {
        if (null == instance) {
            synchronized (SCBleCore.class) {// 同步锁
                if (null == instance) {
                    instance = new SCBleCore();
                }
            }
        }
        return instance;// 返回已存在的对象
    }


    public SCBleCore init(Context context, SCBleListener scBleListener) {
        this.mContext = context;
        this.mSCBleListener = scBleListener;
        return this;
    }

    /**
     * 开始扫描
     */
    public void prepareScan() {
        if (gBluetoothAdapter == null) {
            callbackErrorState(SCBleManagerError.SCBleManagerErrorBLEFail);
            return;
        }
        if (mContext == null) {
            callbackErrorState(SCBleManagerError.SCBleManagerErrorUnknow);
            return;
        }
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            callbackErrorState(SCBleManagerError.SCBleManagerErrorBLEFail);
            return;
        }
        if (!BleUtil.checkGPS(mContext)) {
            callbackErrorState(SCBleManagerError.SCBleManagerErrorGPSPermissionFail);
            return;
        }
        if (Build.VERSION.SDK_INT >= 31) {
            //需要申请权限
            if (BleUtil.checkBlueTooth(mContext)) {
                startScan();
            } else {
                //蓝牙权限没开启
                callbackErrorState(SCBleManagerError.SCBleManagerErrorBLEPermissionFail);
            }
            return;
        }
        startScan();
    }

    /**
     * 关闭连接释放资源
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public void disConnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        release();
    }


    /**
     * 开始蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    private void startScan() {
        if (gBluetoothAdapter.isEnabled()) {
            try {
                if (mBlueDevice != null && mBluetoothGatt != null) {
                    BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                    bluetoothManager.getConnectionState(mBlueDevice, BluetoothProfile.GATT);
                    if (bluetoothManager.getConnectionState(mBlueDevice, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED) {
                        callBackState(SCBleManagerState.SCBleManagerStateConnected);
                        return;
                    }
                }
                setAllowDeviceCheck(true);
                gBluetoothAdapter.startDiscovery();
                //filter注册广播接收器
                IntentFilter filter = new IntentFilter();
                //开始扫描蓝牙设备广播
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                //找到蓝牙设备广播
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                //扫描蓝牙设备束广播
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                mContext.registerReceiver(scanReceiver, filter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //手机蓝牙没开启
            callbackErrorState(SCBleManagerError.SCBleManagerErrorBLEPhonePermissionFail);
        }
    }

    /**
     * GATT回调
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 连接成功
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 连接断开
                mBlueDevice = null;
                callBackState(SCBleManagerState.SCBleManagerStateDisconnected);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGattCharacteristicWrite = SCBleCharacter.getCharacteristic(mBluetoothGatt);
                callBackState(SCBleManagerState.SCBleManagerStateChecking);
                //执行关灯指令
                sendCmd(SCBleConstant.CMD_LIGHT_MODE_OFF);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 发送成功
                if (Arrays.equals(characteristic.getValue(), SCBleConstant.CMD_LIGHT_MODE_OFF) && mIsAllowDeviceCheck) {
                    sendCmd(SCBleConstant.CMD_GET_DEVICE_INFO);
                    mIsCapStartGet = true;
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            SCBleCharacter.dealCharacteristicChanged(characteristic, mIsCapStartGet, new SCBleNet.OnNetCallback() {
                @Override
                public void success(String data, String api) {
                    switch (api) {
                        case SCBleConstant.API_DEVICE_CYPHER:
                            sendCmd(ByteUtil.splitStringToByteArray(data));
                            break;
                        case SCBleConstant.API_DEVICE_CHECK:
                            mIsDeviceChecked = true;
                            if (mIsDeviceChecked && mIsCapReceived) {
                                mIsDeviceChecked = false;
                                mIsCapReceived = false;
                                callBackState(SCBleManagerState.SCBleManagerStateConnected);
                            }
                            break;
                        case SCBleConstant.API_CAP_GET:
                            mIsCapReceived = true;
                            if (mIsDeviceChecked && mIsCapReceived) {
                                mIsDeviceChecked = false;
                                mIsCapReceived = false;
                                callBackState(SCBleManagerState.SCBleManagerStateConnected);
                            }
                            break;
                    }
                }

                @Override
                public void failed(int code, String description) {
                    callbackErrorState(SCBleManagerError.SCBleManagerErrorCheckFail);
                }
            }, mSCBleTestListener);
        }

    };


    /**
     * 蓝牙扫描接收器
     */
    BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    //开始扫描
                    mBlueDevice = null;
                    callBackState(SCBleManagerState.SCBleManagerStateScaning);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //扫描结果
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!TextUtils.isEmpty(device.getName()) && device.getName().equals(SCBleConstant.DEVICE_BLUETOOTH_NAME)) {
                        mBluetoothGatt = device.connectGatt(context, false, gattCallback);
                        mBlueDevice = device;
                        callBackState(SCBleManagerState.SCBleManagerStateConnecting);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //扫描完毕
                    if (mBlueDevice == null) {
                        callbackErrorState(SCBleManagerError.SCBleManagerErrorScanNoDevice);
                    }
                    break;
            }
        }
    };


    /**
     * 发送命令包装
     *
     * @param cmdByte
     */
    @SuppressLint("MissingPermission")
    public void sendCmd(byte[] cmdByte) {
        if (mBluetoothGatt != null && mBluetoothGattCharacteristicWrite != null) {
            mBluetoothGattCharacteristicWrite.setValue(cmdByte);
            mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristicWrite);
        }
    }

    /**
     * 状态监听回调-连接
     */
    public void callBackState(SCBleManagerState scBleManagerState) {
        if (mSCBleListener != null) {
            mSCBleListener.sc_bleConnectionStateDidChange(scBleManagerState);
        }
    }

    /**
     * 状态监听回调-错误
     */
    public void callbackErrorState(SCBleManagerError scBleManagerError) {
        if (mSCBleListener != null) {
            mSCBleListener.sc_bleConnectionDidFailWithError(scBleManagerError);
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (mContext != null) mContext.unregisterReceiver(scanReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mBluetoothGatt = null;
            mBlueDevice = null;
            mIsCapStartGet = false;
            mIsDeviceChecked = false;
            mIsCapReceived = false;
            SCBleCharacter.release();
        }
    }

    /**
     * 设置是否允许设备检测
     */
    public void setAllowDeviceCheck(boolean isAllow){
        this.mIsAllowDeviceCheck = isAllow;
    }

}
