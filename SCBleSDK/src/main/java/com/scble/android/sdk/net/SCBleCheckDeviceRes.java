package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleCheckDeviceRes {
    public String ciphertext;
    public boolean legal;
    public String deviceId;
}
