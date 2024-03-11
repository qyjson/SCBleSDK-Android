package com.scble.android.sdk.view;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.util.AttributeSet;

import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.scble.android.sdk.SCBleConstant;
import com.scble.android.sdk.SCBleSDK;
import com.scble.android.sdk.core.SCBleCharacter;
import com.scble.android.sdk.listener.SCBleCompleteListener;
import com.scble.android.sdk.listener.SCBleTestListener;
import com.scble.android.sdk.net.SCBleNet;
import com.scble.android.sdk.net.SCBleSkinAnalysisReq;
import com.scble.android.sdk.net.SCBleSkinAnalysisRes;
import com.scble.android.sdk.utils.BleImageUtil;
import com.scble.android.sdk.utils.ByteUtil;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Keep
public class SCCameraView extends FrameLayout {

    private PreviewView mPreviewView;
    private ListenableFuture<ProcessCameraProvider> mProcessCameraProviderListenableFuture;
    private ImageCapture mImageCapture;
    private Context mContext;
    private List<List<Integer>> mCapTHDataList;
    private List<String> mImagesList;
    private boolean mIsSkinTestEnd = true;
    private SCBleCompleteListener mSCBleCompleteListener;

    public SCCameraView(@NonNull Context context) {
        this(context, null);
    }

    public SCCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SCCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SCCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initCamera();
        if (mPreviewView != null) {
            addView(mPreviewView);
        }
    }

    /**
     * 初始化Camera配置
     */
    private void initCamera() {
        if (mContext == null) {
            return;
        }
        mPreviewView = new PreviewView(mContext);
        mProcessCameraProviderListenableFuture = ProcessCameraProvider.getInstance(mContext);
        mProcessCameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = mProcessCameraProviderListenableFuture.get();
                processCameraProvider.unbindAll();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                mImageCapture = new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                processCameraProvider.bindToLifecycle((LifecycleOwner) mContext, cameraSelector, preview, mImageCapture);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    @Keep
    /**
     * 开始检测
     */
    public void startTest() {
        if (!mIsSkinTestEnd) {
            return;
        }
        SCBleSDK.startTest(new SCBleTestListener() {
            @Override
            public void start() {
                mIsSkinTestEnd = false;
                if (mCapTHDataList == null) {
                    mCapTHDataList = new ArrayList<>();
                }
                mCapTHDataList.clear();
                if (mImagesList == null) {
                    mImagesList = new ArrayList<>();
                }
                mImagesList.clear();
                //开启设备日光
                SCBleSDK.openLight();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startCapture("SCBle-Light-Photo");
                //开启设备偏振光
                SCBleSDK.openLightWithCmd(SCBleConstant.CMD_LIGHT_MODE_PARALLEL);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startCapture("SCBle-Parallel-Photo");
                //开启设备混合偏振光
                SCBleSDK.openLightWithCmd(SCBleConstant.CMD_LIGHT_MODE_CROSS);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startCapture("SCBle-Cross-Photo");
                //开启设备UV光
                SCBleSDK.openLightWithCmd(SCBleConstant.CMD_LIGHT_MODE_UV);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                startCapture("SCBle-UV-Photo");
            }

            @Override
            public void receivedData(byte[] receivedData) {
                if (!mIsSkinTestEnd) {
                    List<Integer> result = ByteUtil.byteArrToList(receivedData);
                    mCapTHDataList.add(result);
                }
            }

            @Override
            public void end() {
            }
        });
    }


    /**
     * 拍摄照片
     */
    private void startCapture(String fileName) {
        if (mContext == null) {
            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        mImageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        mContext.getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                ContextCompat.getMainExecutor(mContext),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        if (mImagesList.size() < 4) {
                            try {
                                mImagesList.add(BleImageUtil.convertImageUriToBase64(mContext, outputFileResults.getSavedUri(), getMeasuredWidth(), getMeasuredHeight()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (mImagesList.size() == 4) {
                            mIsSkinTestEnd = true;
                            SCBleSDK.closeLight();
                            if (mSCBleCompleteListener != null) {
                                mSCBleCompleteListener.completeTest(SCBleCharacter.getCapBaseValue(), SCBleCharacter.getCiphertext(), mCapTHDataList, mImagesList);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                });
    }

    @Keep
    public void setScBleCompleteListener(SCBleCompleteListener scBleCompleteListener) {
        this.mSCBleCompleteListener = scBleCompleteListener;
    }

    @Keep
    /**
     * 皮肤分析
     */
    public void submitSkinAnalyze(String capBaseValue,
                                  String deviceCiphertext,
                                  List<List<Integer>> capTHDataList,
                                  boolean isLastRegion,
                                  String detectionPosition,
                                  String analysisId,
                                  String whitePic64,
                                  String parallelPic64,
                                  String crossPic64,
                                  String uvPic64) {
        SCBleNet.requestSkinAnalyze("skin_test", capBaseValue, deviceCiphertext, capTHDataList, detectionPosition, analysisId, whitePic64, parallelPic64, crossPic64, uvPic64, new SCBleNet.OnNetCallback() {
            @Override
            public void success(String data, String api) {
                SCBleSkinAnalysisRes scBleSkinAnalysisRes = new Gson().fromJson(data, SCBleSkinAnalysisRes.class);
                SCBleNet.requestSkinAnalyzePoll(scBleSkinAnalysisRes.itemGuid, 0, 20, new SCBleNet.OnNetCallback() {
                    @Override
                    public void success(String data, String api) {
                        if (isLastRegion) {
                            SCBleNet.requestSkinAnalyzeFinish(scBleSkinAnalysisRes.analysisId, new SCBleNet.OnNetCallback() {
                                @Override
                                public void success(String data, String api) {
                                    if (mSCBleCompleteListener != null) {
                                        mSCBleCompleteListener.completeAnalyze(scBleSkinAnalysisRes.analysisId, scBleSkinAnalysisRes.itemGuid);
                                    }
                                }

                                @Override
                                public void failed(int code, String description) {
                                    if (mSCBleCompleteListener != null) {
                                        mSCBleCompleteListener.completeAnalyzeError(code, description);
                                    }
                                }
                            });
                            return;
                        }
                        if (mSCBleCompleteListener != null) {
                            mSCBleCompleteListener.completeAnalyze(scBleSkinAnalysisRes.analysisId, scBleSkinAnalysisRes.itemGuid);
                        }
                    }

                    @Override
                    public void failed(int code, String description) {
                        if (mSCBleCompleteListener != null) {
                            mSCBleCompleteListener.completeAnalyzeError(code, description);
                        }
                    }
                });
            }

            @Override
            public void failed(int code, String description) {
                if (mSCBleCompleteListener != null) {
                    mSCBleCompleteListener.completeAnalyzeError(code, description);
                }
            }
        });

    }


}
