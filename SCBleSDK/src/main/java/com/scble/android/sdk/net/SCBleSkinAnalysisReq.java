package com.scble.android.sdk.net;

import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

import java.util.List;

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleSkinAnalysisReq extends SCBleBaseReq {
    public String testType;
    public double capBaseValue;
    public String deviceCiphertext;
    public List<List<Integer>> capTHDataList;
    public String detectionPosition;
    public String analysisId;
    public String whitePic64;
    public String parallelPic64;
    public String crossPic64;
    public String uvPic64;
}
