package com.github.cyc.crashhandler.application;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.github.cyc.crashhandler.BuildConfig;
import com.github.cyc.crashhandler.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by cyc on 2017/9/13.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final String DIR = "crash";

    // CrashHandler实例
    private static CrashHandler sCrashHandler = new CrashHandler();

    private Context mContext;

    // 默认的未捕获异常处理器
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    // 用来存储应用信息和设备信息
    private Map<String, String> mInfo = new LinkedHashMap<>();

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return sCrashHandler;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        // 获取默认的未捕获异常处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置CrashHandler为默认的未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        // 处理异常
        handleException(ex);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 结束应用
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, ex);
        } else {
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private void handleException(Throwable e) {
        // Toast提示出现异常
        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, R.string.crash_tips, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        // 收集应用信息和设备信息
        collectInfo();
        // 保存崩溃信息到SD卡
        saveInfo(e);
    }

    private void collectClassInfo(Class cls) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mInfo.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void collectInfo() {
        // 收集BuildConfig类信息
        collectClassInfo(BuildConfig.class);
        // 收集Build.VERSION类信息
        collectClassInfo(Build.VERSION.class);
        // 收集Build类信息
        collectClassInfo(Build.class);
    }

    private void saveInfo(Throwable ex) {
        // 判断SD卡是否挂载
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e(TAG, "SD card is unmounted, so we can not save crash info!");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : mInfo.entrySet()) {
            stringBuilder.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 以当前时间来命名崩溃日志文件
        long timestamp = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(timestamp));
        File file = new File(dir, "crash-" + time + "-" + timestamp + ".log");

        // 保存崩溃信息到文件
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            printWriter.print(stringBuilder.toString());
            printWriter.println();
            ex.printStackTrace(printWriter);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
