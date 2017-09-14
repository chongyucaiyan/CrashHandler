package com.github.cyc.crashhandler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initContentView();
        checkPermission();
    }

    private void initContentView() {
        findViewById(R.id.btn_main_make_a_crash).setOnClickListener(this);
    }

    private void checkPermission() {
        // 检查写外部存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 申请写外部存储权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_main_make_a_crash) {
            // 点击按钮时，抛出一个异常
            throw new RuntimeException("For test CrashHandler!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予
                Toast.makeText(this, R.string.main_permission_granted_tips, Toast.LENGTH_SHORT).show();
            } else {
                // 权限拒绝
                Toast.makeText(this, R.string.main_permission_denied_tips, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
