package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

import com.scble.android.sdk.SCBleSDK;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleBaseReq {
    public String memberGuid = SCBleSDK.getMemberGuid();
}
