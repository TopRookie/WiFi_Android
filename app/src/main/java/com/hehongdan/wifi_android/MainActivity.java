package com.hehongdan.wifi_android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.hehongdan.wifi_android.test.WifiReceiver;
import com.hehongdan.wifi_android.test.WifiReceiverActionListener;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 *
 */
@RuntimePermissions
public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener {

    /** WiFi状态监听器 */
    private WifiReceiverActionListener listener = new WifiReceiverActionListener() {
        @Override
        public void onWifiOpened() {
            Log.v(TAG, "WiFi已打开...");
            //去扫描WIFI
            mWifiController.scanWifiAround();
        }

        @Override
        public void onWifiOpening() {
            Log.v(TAG, "WiFi打开中...");
        }

        @Override
        public void onWifiClosed() {
            Log.v(TAG, "WiFi已关闭...");
            //清空集合
            //mAdapter.clearList();
            //mAdapter.notifyDataSetChanged();
            mHHDAdapter.clearListData();
            mHHDAdapter.notifyDataSetChanged();
        }

        @Override
        public void onWifiClosing() {
            Log.v(TAG, "WiFi关闭中...");
        }

        @Override
        public void onWifiScanResultBack() {
            Log.v(TAG, "WiFi扫描返回...");
            //在这里处理wifi的结果
            mWifiScanResult = mWifiController.getWifiScanResult();
            //扫描到结果以后,就开始更新界面
            if (null != mWifiScanResult) {
                Log.v(TAG, "WiFi扫描返回个数=" + mWifiScanResult.size());
                /*if (mAdapter != null) {
                    mAdapter.setData(mWifiScanResult);
                    mAdapter.notifyDataSetChanged();
                }*/
                if (null != mHHDAdapter){
                    mHHDAdapter.setListData(mWifiScanResult);
                    mHHDAdapter.notifyDataSetChanged();
                }
            } else {
                Log.v(TAG, "WiFi扫描返回=null");
            }
        }

        @Override
        public void onWifiConnected(WifiInfo wifiInfo) {
            Log.e(TAG, "WiFi已连接（测试连接两次）...");
        }
    };

    private static final String TAG = "MainActivity";

    /** WiFi控制器 */
    private WifiController mWifiController;
    /** WiFi状态监听器 */
    private WifiReceiver wifiReceiver;


    /** 扫描结果视图列表 */
    private RecyclerView recyclerView;
    /** 扫描结果数据列表 */
    private List<ScanResult> mWifiScanResult;
    /** 扫描结果适配器 */
    private WifiListAdapter mHHDAdapter;
    //private WifiListAdapter mAdapter;
    //private WifiListAdapter.OnItemClickListener itemListener;

    /** WiFi开关 */
    private Switch wifiOpenOrClose;

    private Context mContext;

    /**
     * 需要定位权限（待处理）
     */
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    void needLocation() {
    }

    /**
     * 获得定位权限回调（待处理）
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        initView();
        initData();
        //注册广播接收者
        registerBroadcastReceiver();
        //动态授权
        MainActivityPermissionsDispatcher.needLocationWithPermissionCheck(this);
    }

    /**
     * 区分加密类型进行对应连接
     *
     * @param scanResult 扫描结果
     */
    private void connectWifi(final ScanResult scanResult) {
        final String ssid = scanResult.SSID;
        final EditText editText = new EditText(mContext);
        editText.setHint("请输入密码");

        final WifiController.SecurityMode mSecurityMode = mWifiController.getSecurityMode(scanResult);
        final WifiConfiguration mWifiConfiguration = mWifiController.isExsits(scanResult.SSID);
        if (null == mWifiConfiguration) {
            if (mSecurityMode == WifiController.SecurityMode.OPEN){
                mWifiController.connectionWifiByPassword(scanResult, null, new WifiController.OnWifiConnectListener() {
                    @Override
                    public void onStart(String SSID) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onFailure(String SSID) {

                    }
                });
            } else if (mSecurityMode == WifiController.SecurityMode.WPA){
                new AlertDialog.Builder(mContext)
                        .setTitle(ssid)
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String password = editText.getText().toString().trim();
                                Toast.makeText(mContext, "实现连接，" + editText.getText(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "连接WiFi的加密方式=" + mSecurityMode);
                                mWifiController.connect(mWifiController.createWifiConfiguration(scanResult.SSID, password, mSecurityMode));
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }

        }else {
            //已经连接过
            boolean isExist = mWifiController.connect(ssid);
        }

    }


    /**
     * 初始化视图
     */
    private void initView() {
        wifiOpenOrClose = (Switch) findViewById(R.id.wifiOpenOrClose);
        wifiOpenOrClose.setOnCheckedChangeListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //mAdapter = new WifiListAdapter(this, newListener());
        mHHDAdapter = new WifiListAdapter(this,mWifiScanResult);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(mAdapter);
        recyclerView.setAdapter(mHHDAdapter);

        mHHDAdapter.setOnItemClickListener(new WifiListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                connectWifi(mWifiScanResult.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mWifiController = WifiController.getInstant(getApplicationContext());

        if (mWifiController.isWifiEnable()) {
            wifiOpenOrClose.setChecked(true);
            mWifiController.scanWifiAround();
        }
    }

    @Override
    protected void onDestroy() {
        //注销广播接收者
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mWifiController.setWifiEnabled(isChecked);
    }

    /**
     * 在这里注册广播接收者
     * 这里面注册广播的话,包括:
     * 1 wifi开启状态的监听(wifi关闭,wifi打开)
     * 2 wifi连接的广播
     * 3 wifi连接状态改变的广播
     * <p>
     */
    private int count = 0;
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        //设置意图过滤
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        Log.e(TAG, "测试次数注册广播次数=" + (++count));
        wifiReceiver = new WifiReceiver(listener);
        registerReceiver(wifiReceiver, filter);
    }

    /**
     * 取消注册广播接收者
     */
    private void unregisterReceiver() {
        unregisterReceiver(wifiReceiver);
    }
}
