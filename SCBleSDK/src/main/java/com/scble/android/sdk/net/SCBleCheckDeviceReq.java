package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleCheckDeviceReq extends SCBleBaseReq {
    public String ciphertext;
    public String macAddr;
}
