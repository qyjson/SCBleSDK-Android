package com.scble.android.sdk.listener;


/**
 * 检测过程监听
 */
public interface SCBleTestListener {
    void start();

    void receivedData(byte[] receivedData);

    void end();
}
