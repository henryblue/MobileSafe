package com.mobilesafe.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class BaseAcitivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 返回按钮点击事件
     * @param view
     */
    public void backOut(View view) {
        finish();
    }

}
