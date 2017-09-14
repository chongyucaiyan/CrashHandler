package com.github.cyc.crashhandler.application;

import android.app.Application;

/**
 * Created by cyc on 2017/9/13.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化CrashHandler
        CrashHandler.getInstance().init(this);
    }
}
