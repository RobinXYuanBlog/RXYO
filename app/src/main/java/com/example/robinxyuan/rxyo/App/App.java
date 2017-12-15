package com.example.robinxyuan.rxyo.App;

import android.app.Application;
import android.os.Handler;

/**
 * Created by robinxyuan on 2017/12/14.
 */

public class App extends Application {

    /**
     * 启动照相Intent的RequestCode.自定义相机.
     */
    public static final int TAKE_PHOTO_CUSTOM = 100;
    /**
     * 主线程Handler.
     */
    public static Handler mHandler;
    public static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        mHandler = new Handler();
//        Fresco.initialize(this, ImagePipelineConfig
//                .newBuilder(this)
//                .setDownsampleEnabled(true)
//                .build());
    }
}
