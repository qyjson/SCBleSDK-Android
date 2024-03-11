package com.scble.android.sdk.listener;


import androidx.annotation.Keep;

import com.scble.android.sdk.enums.SCBleManagerError;
import com.scble.android.sdk.enums.SCBleManagerState;

@Keep
/**
 * 相关回调监听
 */
public interface SCBleListener {
   void sc_bleConnectionStateDidChange(SCBleManagerState scBleManagerState);
   void sc_bleConnectionDidFailWithError(SCBleManagerError scBleManagerError);
}
