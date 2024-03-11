package com.scble.demo;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.scble.android.sdk.SCBleSDK;
import com.scble.android.sdk.enums.SCBleManagerError;
import com.scble.android.sdk.enums.SCBleManagerState;
import com.scble.android.sdk.listener.SCBleListener;

/**
 * 主界面
 */
public class SkinMainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    public static final int PERMISSION_CAMERA_REQUEST_CODE = 1000;
    public static final int PERMISSION_GPS_REQUEST_CODE = 1001;
    public static final int PERMISSION_BLUETOOTH_REQUEST_CODE = 1002;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_main);
        initImmersionBar();
        SCBleSDK.init(this, "sk-9d6a454c0b4c4cdf9af12b1965a5ef65", "8be15121-b336-4eaf-8ccd-00a725fdb9c3", new SCBleListener() {
            @Override
            public void sc_bleConnectionStateDidChange(SCBleManagerState scBleManagerState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (scBleManagerState) {
                            case SCBleManagerStateScaning:
                                showMsg("正在扫描设备");
                                break;
                            case SCBleManagerStateConnecting:
                                showMsg("正在连接设备");
                                break;
                            case SCBleManagerStateChecking:
                                showMsg("正在校验设备");
                                break;
                            case SCBleManagerStateConnected:
                                dismissMsg();
                                startTestPage();
                                break;
                            case SCBleManagerStateDisconnected:
                                if (isDestroyed()) {
                                    return;
                                }
                                dismissMsg();
                                SCBleSDK.release();
//                                if (MyApplication.getInstance().getCurrentActivity() != null && MyApplication.getInstance().getCurrentActivity().getLocalClassName().equals("SkinTestActivity")) {
//                                    MyApplication.getInstance().getCurrentActivity().finish();
//                                }
                                break;
                            default:
                                dismissMsg();
                                break;
                        }
                    }
                });
            }

            @Override
            public void sc_bleConnectionDidFailWithError(SCBleManagerError scBleManagerError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissMsg();
                        switch (scBleManagerError) {
                            case SCBleManagerErrorUnknow:
                                Toast.makeText(SkinMainActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                break;
                            case SCBleManagerErrorScanNoDevice:
                                Toast.makeText(SkinMainActivity.this, "未搜索到设备", Toast.LENGTH_SHORT).show();
                                break;
                            case SCBleManagerErrorConnectFail:
                                Toast.makeText(SkinMainActivity.this, "设备连接失败", Toast.LENGTH_SHORT).show();
                                break;
                            case SCBleManagerErrorCheckFail:
                                Toast.makeText(SkinMainActivity.this, "设备校验失败", Toast.LENGTH_SHORT).show();
                                SCBleSDK.disconnectDevice();
                                break;
                            case SCBleManagerErrorBLEFail:
                                Toast.makeText(SkinMainActivity.this, "手机不支持蓝牙", Toast.LENGTH_SHORT).show();
                                break;
                            case SCBleManagerErrorBLEPhonePermissionFail:
                                Toast.makeText(SkinMainActivity.this, "手机蓝牙未开启", Toast.LENGTH_SHORT).show();
                                break;
                            case SCBleManagerErrorBLEPermissionFail:
                                if (Build.VERSION.SDK_INT >= 23 &&
                                        (ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PERMISSION_GRANTED)
                                ) {
                                    ActivityCompat.requestPermissions(SkinMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_BLUETOOTH_REQUEST_CODE);
                                }
                                break;
                            case SCBleManagerErrorGPSPermissionFail:
                                if (Build.VERSION.SDK_INT >= 23 &&
                                        (ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED)
                                ) {
                                    ActivityCompat.requestPermissions(SkinMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_GPS_REQUEST_CODE);
                                }
                                break;
                        }
                    }
                });
            }
        });
        findViewById(R.id.tv_skin_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SCBleSDK.startConnect();
            }
        });

    }

    /**
     * 跳转到测试页面
     */
    private void startTestPage() {
        if (Build.VERSION.SDK_INT >= 23 &&
                (ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.CAMERA) != PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(SkinMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(SkinMainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAMERA_REQUEST_CODE);
            return;
        }
        startActivity(new Intent(SkinMainActivity.this, SkinTestActivity.class));
    }

    /**
     * 沉浸式状态栏
     */
    private void initImmersionBar() {
        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarColor(R.color.white)     //状态栏颜色，不写默认透明色
                .navigationBarColor(R.color.white)
                .autoDarkModeEnable(true)
                .init();  //必须调用方可应用以上所配置的参数
    }


    @Override
    protected void onDestroy() {
        dismissMsg();
        super.onDestroy();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA_REQUEST_CODE:
                //判断相机和读写权限
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                    startTestPage();
                }
                break;
            case PERMISSION_GPS_REQUEST_CODE:
            case PERMISSION_BLUETOOTH_REQUEST_CODE:
                //判断蓝牙权限
                //判断GPS定位权限
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                    SCBleSDK.startConnect();
                }
                break;
        }
    }
}