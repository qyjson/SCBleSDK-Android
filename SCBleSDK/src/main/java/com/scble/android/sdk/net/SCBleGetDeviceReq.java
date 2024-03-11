package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleGetDeviceReq extends SCBleBaseReq {
    public String deviceCode;
    public String macAddr;
}
