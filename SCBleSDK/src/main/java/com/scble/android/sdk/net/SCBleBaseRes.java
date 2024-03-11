package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleBaseRes<T> {
    public int code;
    public String description;

    public T data;
}
