package com.scble.android.sdk.bean;

import androidx.annotation.Keep;

@Keep
public class DetectionBean {
    private String detectionPosition;
    private String detectionPositionName;

    public String getDetectionPosition() {
        return detectionPosition;
    }

    public void setDetectionPosition(String detectionPosition) {
        this.detectionPosition = detectionPosition;
    }

    public String getDetectionPositionName() {
        return detectionPositionName;
    }

    public void setDetectionPositionName(String detectionPositionName) {
        this.detectionPositionName = detectionPositionName;
    }
}
