package com.scble.android.sdk.net;

import android.os.Handler;
import android.os.Looper;


import androidx.annotation.Keep;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scble.android.sdk.SCBleConstant;
import com.scble.android.sdk.SCBleSDK;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SCBleNet {
    private static SCBleNetInterceptor mScBleNetInterceptor = new SCBleNetInterceptor.Builder().
            addParam("memberGuid", SCBleSDK.getMemberGuid()).
            addHeaderParam("Authorization", "Bearer " + SCBleSDK.getApiKey()).
            build();

    private static OkHttpClient mSCOkHttpClient = new OkHttpClient.Builder()
            .writeTimeout(1L, TimeUnit.MINUTES)
            .connectTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .addInterceptor(mScBleNetInterceptor)
            .build();

    private static int mCount = 0;


    public interface OnNetCallback {
        void success(String data, String api);

        void failed(int code, String description);
    }

    /**
     * 获取设备密钥
     *
     * @param deviceCode
     * @param macAddress
     * @param onNetCallback
     */
    public static void requestGetDeviceCiphertext(String deviceCode, String macAddress, OnNetCallback onNetCallback) {
        SCBleGetDeviceReq scBleGetDeviceReq = new SCBleGetDeviceReq();
        scBleGetDeviceReq.deviceCode = deviceCode;
        scBleGetDeviceReq.macAddr = macAddress;
        String requestJson = new Gson().toJson(scBleGetDeviceReq);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson);
        Request request = new Request.Builder().post(requestBody).url(SCBleConstant.API_PREFIX + "/" + SCBleConstant.API_DEVICE_CYPHER).build();
        mSCOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onNetCallback != null) {
                    onNetCallback.failed(0, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    String data = response.body().string();
                    Type respType = new TypeToken<SCBleBaseRes<SCBleGetDeviceRes>>() {
                    }.getType();
                    SCBleBaseRes<SCBleGetDeviceRes> res = new Gson().fromJson(data, respType);
                    if (onNetCallback != null && res.code == 200) {
                        onNetCallback.success(res.data.ciphertext, SCBleConstant.API_DEVICE_CYPHER);
                    } else {
                        if (onNetCallback != null) {
                            onNetCallback.failed(res.code, res.description);
                        }
                    }
                } else {
                    if (onNetCallback != null) {
                        onNetCallback.failed(0, "请求失败");
                    }
                }
            }
        });
    }

    /**
     * 设备检测
     *
     * @param ciphertext
     * @param macAddr
     * @param onNetCallback
     */
    public static void requestDeviceCheck(String ciphertext, String macAddr, OnNetCallback onNetCallback) {
        SCBleCheckDeviceReq scBleCheckDeviceReq = new SCBleCheckDeviceReq();
        scBleCheckDeviceReq.ciphertext = ciphertext;
        scBleCheckDeviceReq.macAddr = macAddr;
        String requestJson = new Gson().toJson(scBleCheckDeviceReq);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson);
        Request request = new Request.Builder().post(requestBody).url(SCBleConstant.API_PREFIX + "/" + SCBleConstant.API_DEVICE_CHECK).build();
        mSCOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onNetCallback != null) {
                    onNetCallback.failed(0, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    String data = response.body().string();
                    Type respType = new TypeToken<SCBleBaseRes<SCBleCheckDeviceRes>>() {
                    }.getType();
                    SCBleBaseRes<SCBleCheckDeviceRes> res = new Gson().fromJson(data, respType);
                    if (onNetCallback != null) {
                        if (res.code != 200) {
                            if (onNetCallback != null) {
                                onNetCallback.failed(res.code, res.description);
                            }
                            return;
                        }
                        if (!res.data.legal) {
                            if (onNetCallback != null) {
                                onNetCallback.failed(0, "校验失败");
                            }
                            return;
                        } else {
                            onNetCallback.success(res.data.ciphertext, SCBleConstant.API_DEVICE_CHECK);
                        }

                    }
                } else {
                    if (onNetCallback != null) {
                        onNetCallback.failed(0, "请求失败");
                    }
                }
            }
        });
    }

    /**
     * 提交数据-皮肤分析
     *
     * @param testType
     * @param capBaseValue
     * @param deviceCiphertext
     * @param capTHDataList
     * @param detectionPosition
     * @param analysisId
     * @param whitePic64
     * @param parallelPic64
     * @param crossPic64
     * @param uvPic64
     * @param onNetCallback
     */
    public static void requestSkinAnalyze(String testType,
                                          String capBaseValue,
                                          String deviceCiphertext,
                                          List<List<Integer>> capTHDataList,
                                          String detectionPosition,
                                          String analysisId,
                                          String whitePic64,
                                          String parallelPic64,
                                          String crossPic64,
                                          String uvPic64,
                                          OnNetCallback onNetCallback) {
        SCBleSkinAnalysisReq scBleSkinAnalysisReq = new SCBleSkinAnalysisReq();
        scBleSkinAnalysisReq.testType = testType;
        scBleSkinAnalysisReq.capBaseValue = Double.valueOf(capBaseValue);
        scBleSkinAnalysisReq.deviceCiphertext = deviceCiphertext;
        scBleSkinAnalysisReq.capTHDataList = capTHDataList;
        scBleSkinAnalysisReq.detectionPosition = detectionPosition;
        scBleSkinAnalysisReq.analysisId = analysisId;
        scBleSkinAnalysisReq.whitePic64 = whitePic64.replaceAll("\\n", "");
        scBleSkinAnalysisReq.parallelPic64 = parallelPic64.replaceAll("\\n", "");
        scBleSkinAnalysisReq.crossPic64 = crossPic64.replaceAll("\\n", "");
        scBleSkinAnalysisReq.uvPic64 = uvPic64.replaceAll("\\n", "");
        String requestJson = new Gson().toJson(scBleSkinAnalysisReq);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson);
        Request request = new Request.Builder().post(requestBody).url(SCBleConstant.API_PREFIX + "/" + SCBleConstant.API_SKIN_ANALYSIS).build();
        mSCOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onNetCallback != null) {
                    onNetCallback.failed(0, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    String data = response.body().string();
                    Type respType = new TypeToken<SCBleBaseRes<SCBleSkinAnalysisRes>>() {
                    }.getType();
                    SCBleBaseRes<SCBleSkinAnalysisRes> res = new Gson().fromJson(data, respType);
                    if (onNetCallback != null && res.code == 200) {
                        onNetCallback.success(new Gson().toJson(res.data), SCBleConstant.API_SKIN_ANALYSIS);
                    } else {
                        if (onNetCallback != null) {
                            onNetCallback.failed(res.code, res.description);
                        }
                    }
                } else {
                    if (onNetCallback != null) {
                        onNetCallback.failed(0, "请求失败");
                    }
                }
            }
        });
    }


    public static void requestSkinAnalyzePoll(String itemGuid, int retryCurrentCount, int retryMaxCount,
                                              OnNetCallback onNetCallback) {
        mCount = retryCurrentCount;
        SCBleSkinAnalysisPollReq scBleSkinAnalysisPollReq = new SCBleSkinAnalysisPollReq();
        scBleSkinAnalysisPollReq.itemGuid = itemGuid;
        String requestJson = new Gson().toJson(scBleSkinAnalysisPollReq);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson);
        Request request = new Request.Builder().post(requestBody).url(SCBleConstant.API_PREFIX + "/" + SCBleConstant.API_SKIN_ANALYSIS_POLLING).build();
        mSCOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCount = 0;
                if (onNetCallback != null) {
                    onNetCallback.failed(0, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    String data = response.body().string();
                    Type respType = new TypeToken<SCBleBaseRes<SCBleSkinAnalysisPollRes>>() {
                    }.getType();
                    SCBleBaseRes<SCBleSkinAnalysisPollRes> res = new Gson().fromJson(data, respType);
                    if (res.data.status != 1) {
                        if (mCount >= retryMaxCount - 1) {
                            mCount = 0;
                            if (onNetCallback != null) {
                                onNetCallback.failed(0, "提交数据超时");
                            }
                            return;
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCount = mCount + 1;
                                requestSkinAnalyzePoll(itemGuid, mCount, retryMaxCount, onNetCallback);
                            }
                        }, 3000L);
                        return;
                    }
                    if (onNetCallback != null) {
                        mCount = 0;
                        onNetCallback.success("1", SCBleConstant.API_SKIN_ANALYSIS_POLLING);
                    }
                } else {
                    mCount = 0;
                    if (onNetCallback != null) {
                        onNetCallback.failed(0, "请求失败");
                    }
                }
            }
        });
    }

    /**
     * 提交的检测数据是最后一个部位调用
     *
     * @param analysisId
     * @param onNetCallback
     */
    public static void requestSkinAnalyzeFinish(String analysisId,
                                                OnNetCallback onNetCallback) {
        SCBleSkinAnalysisFinishReq scBleSkinAnalysisFinishReq = new SCBleSkinAnalysisFinishReq();
        scBleSkinAnalysisFinishReq.analysisId = analysisId;
        String requestJson = new Gson().toJson(scBleSkinAnalysisFinishReq);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson);
        Request request = new Request.Builder().post(requestBody).url(SCBleConstant.API_PREFIX + "/" + SCBleConstant.API_SKIN_ANALYSIS_FINISH).build();
        mSCOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onNetCallback != null) {
                    onNetCallback.failed(0, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200) {
                    String data = response.body().string();
                    Type respType = new TypeToken<SCBleBaseRes<SCBleSkinAnalysisFinishRes>>() {
                    }.getType();
                    SCBleBaseRes<SCBleSkinAnalysisFinishRes> res = new Gson().fromJson(data, respType);
                    if (res.code != 200) {
                        if (onNetCallback != null) {
                            onNetCallback.failed(res.code, res.description);
                        }
                        return;
                    }
                    if (onNetCallback != null) {
                        onNetCallback.success("1", SCBleConstant.API_SKIN_ANALYSIS_FINISH);
                    }
                } else {
                    if (onNetCallback != null) {
                        onNetCallback.failed(0, "请求失败");
                    }
                }
            }
        });
    }


}
