package com.wanjie.wifidemo;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "liu";

    private Switch wifiOpenOrClose;
    private RecyclerView recyclerView;

    private WifiReceiver wifiReceiver;
    private WifiController mWifiController;

    private List<ScanResult> mWifiScanResult;
    private WifiListAdapter mAdapter;

    private boolean isOk2GetResult = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册广播接收者
        registerBroadcastReceiver();
        initView();
        initData();
    }

    private void initView() {
        wifiOpenOrClose = (Switch)findViewById(R.id.wifiOpenOrClose);
        wifiOpenOrClose.setOnCheckedChangeListener(this);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mAdapter = new WifiListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

    }

    private void initData() {
        mWifiController = WifiController.getInstant(getApplicationContext());

        if(mWifiController.isWifiEnable()) {
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

    private WifiReceiverActionListener listener = new WifiReceiverActionListener() {
        @Override
        public void onWifiOpened() {
            Log.d(TAG, "onWifiOpened...");
            //去扫描WIFI
            mWifiController.scanWifiAround();
        }

        @Override
        public void onWifiOpening() {
            Log.d(TAG, "onWifiOpening...");
        }

        @Override
        public void onWifiClosed() {
            Log.d(TAG, "onWifiClosed...");
            //清空集合
            mAdapter.clearList();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onWifiClosing() {
            Log.d(TAG, "onWifiClosing...");
        }

        @Override
        public void onWifiScanResultBack() {
            Log.d(TAG, "onWifiScanResultBack...");
            //在这里处理wifi的结果
            mWifiScanResult = mWifiController.getWifiScanResult();
            //扫描到结果以后,就开始更新界面
            if (null != mWifiScanResult) {
                Log.d(TAG, "size == " + mWifiScanResult.size());
                if (mAdapter != null) {
                    mAdapter.setData(mWifiScanResult);
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d(TAG, "scan result ==  null");
            }
        }

        @Override
        public void onWifiConnected(WifiInfo wifiInfo) {
            Log.d(TAG, "onWifiConnected...");
        }
    };

    /**
     * 在这里注册广播接收者
     * 这里面注册广播的话,包括:
     * 1 wifi开启状态的监听(wifi关闭,wifi打开)
     * 2 wifi连接的广播
     * 3 wifi连接状态改变的广播
     * <p>
     */
    private void registerBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();
        //设置意图过滤

        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        wifiReceiver = new WifiReceiver(listener);
        registerReceiver(wifiReceiver, filter);
    }

    /**
     *取消注册广播接收者
     */
    private void unregisterReceiver(){
        unregisterReceiver(wifiReceiver);
    }

}
