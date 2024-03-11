package com.scble.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.scble.android.sdk.SCBleSDK;
import com.scble.android.sdk.bean.DetectionBean;
import com.scble.android.sdk.listener.SCBleCompleteListener;
import com.scble.android.sdk.utils.BleImageUtil;
import com.scble.android.sdk.view.SCCameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * 皮肤测试界面
 */
public class SkinTestActivity extends AppCompatActivity {

    private ImageView ivClose;
    private ImageView ivBack;
    private ImageView ivSubmit;
    private TextView tvDetection;
    private ImageView ivPhoto;
    private TextView tvLightWhite;
    private TextView tvLightParalle;
    private TextView tvLightCross;
    private TextView tvLightUV;
    private View view_complete;
    private SCCameraView scCameraView;
    private int[] viewIds = new int[]{R.id.tv_light_white, R.id.tv_light_paralle, R.id.tv_light_cross, R.id.tv_light_UV};

    private List<DetectionBean> mDetectionList = new ArrayList<>();

    private int currentDetectionIndex = 0;

    private SkinTestBean skinTestBean;

    private String analysisId;
    private String detectionPosition;

    private ProgressDialog progressDialog;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_test);
        ivClose = findViewById(R.id.iv_skin_test_close);
        tvDetection = findViewById(R.id.tv_detection);
        scCameraView = findViewById(R.id.sc_camera_view);
        view_complete = findViewById(R.id.view_complete);
        ivBack = view_complete.findViewById(R.id.iv_skin_back);
        ivSubmit = view_complete.findViewById(R.id.iv_skin_submit);
        ivPhoto = view_complete.findViewById(R.id.iv_skin_photo);
        tvLightWhite = view_complete.findViewById(R.id.tv_light_white);
        tvLightParalle = view_complete.findViewById(R.id.tv_light_paralle);
        tvLightCross = view_complete.findViewById(R.id.tv_light_cross);
        tvLightUV = view_complete.findViewById(R.id.tv_light_UV);
        mDetectionList.clear();
        mDetectionList.addAll(SCBleSDK.testRegions());

        initImmersionBar();
        initData();
        initListener();
    }

    private void initImmersionBar() {
        ImmersionBar.with(this)
                .fitsSystemWindows(false)
                .transparentStatusBar()     //状态栏颜色，不写默认透明色
                .navigationBarColor(R.color.white)
                .autoDarkModeEnable(true)
                .init();  //必须调用方可应用以上所配置的参数
    }

    private void initData() {
        view_complete.setVisibility(View.GONE);
        String name = (currentDetectionIndex + 1) + "/" + mDetectionList.size() + " 拍摄" + mDetectionList.get(currentDetectionIndex).getDetectionPositionName();
        detectionPosition = mDetectionList.get(currentDetectionIndex).getDetectionPosition();
        tvDetection.setText(name);
        //打开日光模式的灯
        SCBleSDK.openLight();
    }




    private void initListener() {
        view_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //demo简单实现，防止点击穿透
            }
        });
        scCameraView.setScBleCompleteListener(new SCBleCompleteListener() {
            @Override
            public void completeTest(String capBaseValue, String deviceCiphertext, List<List<Integer>> capTHDataList, List<String> imageList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //完成某个部位的一次测试
                        skinTestBean = new SkinTestBean();
                        skinTestBean.capBaseValue = capBaseValue;
                        skinTestBean.deviceCiphertext = deviceCiphertext;
                        skinTestBean.capTHDataList = capTHDataList;
                        skinTestBean.imageList = imageList;
                        view_complete.setVisibility(View.VISIBLE);
                        tvLightWhite.performClick();
                    }
                });
            }

            @Override
            public void completeAnalyze(String mAnalysisId, String itemGuid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissMsg();
                        if (currentDetectionIndex == mDetectionList.size() - 1) {
                            //所有部位检测完成
                            Toast.makeText(SkinTestActivity.this, "全部完毕，结束-成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SkinTestActivity.this, SkinFinishActivity.class));
                            finish();
                        } else {
                            //完成某个部位的一次分析
                            currentDetectionIndex = currentDetectionIndex + 1;
                            analysisId = mAnalysisId;
                            initData();
                        }

                    }
                });
            }

            @Override
            public void completeAnalyzeError(int errCode, String errMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissMsg();
                    }
                });
            }
        });
        scCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scCameraView.startTest();
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view_complete.setVisibility(View.GONE);
                SCBleSDK.openLight();
            }
        });

        ivSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skinTestBean == null) {
                    return;
                }
                showMsg("提交数据中，请稍后");
                boolean isLastRegion = currentDetectionIndex == mDetectionList.size() - 1;
                scCameraView.submitSkinAnalyze(skinTestBean.capBaseValue,
                        skinTestBean.deviceCiphertext,
                        skinTestBean.capTHDataList,
                        isLastRegion,
                        detectionPosition,
                        analysisId,
                        skinTestBean.imageList.get(0),
                        skinTestBean.imageList.get(1),
                        skinTestBean.imageList.get(2),
                        skinTestBean.imageList.get(3)
                );
            }
        });
        tvLightWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStyle(view.getId());
            }
        });

        tvLightParalle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStyle(view.getId());
            }
        });

        tvLightCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStyle(view.getId());
            }
        });

        tvLightUV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStyle(view.getId());
            }
        });
    }

    /**
     * 切换样式
     *
     * @param viewId
     */
    private void changeStyle(int viewId) {
        for (int i = 0; i < viewIds.length; i++) {
            if (viewId == viewIds[i]) {
                view_complete.findViewById(viewIds[i]).setBackgroundResource(R.drawable.bg_skin_test_bg_white);
                ((TextView) view_complete.findViewById(viewIds[i])).setTextColor(Color.parseColor("#000000"));
                ivPhoto.setImageBitmap(BleImageUtil.base64ToBitmap(skinTestBean.imageList.get(i)));
            } else {
                view_complete.findViewById(viewIds[i]).setBackgroundResource(R.drawable.bg_skin_test_bg);
                ((TextView) view_complete.findViewById(viewIds[i])).setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
    }

    private void showMsg(String msg) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(msg);
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private void dismissMsg() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissMsg();
        SCBleSDK.closeLight();
        super.onDestroy();
    }
}