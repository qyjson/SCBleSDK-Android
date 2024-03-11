package com.scble.android.sdk.listener;

import androidx.annotation.Keep;

import java.util.List;

@Keep
/**
 * 完成监听，回调给开发者
 */
public interface SCBleCompleteListener {
    /**
     * 完成测试
     *
     * @param capBaseValue
     * @param deviceCiphertext
     * @param capTHDataList
     * @param imageList
     */
    void completeTest(String capBaseValue, String deviceCiphertext, List<List<Integer>> capTHDataList, List<String> imageList);

    /**
     * 完成分析
     *
     * @param analysisId
     * @param itemGuid
     */
    void completeAnalyze(String analysisId, String itemGuid);

    /**
     * 分析超时
     *
     * @param errCode
     * @param errMsg
     */
    void completeAnalyzeError(int errCode, String errMsg);
}
