package com.scble.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.gyf.immersionbar.ImmersionBar;
import com.scble.demo.R;

/**
 * 测试完成
 */
public class SkinFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_finish);
        initImmersionBar();
    }

    private void initImmersionBar() {
        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarColor(R.color.white)     //状态栏颜色，不写默认透明色
                .navigationBarColor(R.color.white)
                .autoDarkModeEnable(true)
                .init();  //必须调用方可应用以上所配置的参数
    }

}