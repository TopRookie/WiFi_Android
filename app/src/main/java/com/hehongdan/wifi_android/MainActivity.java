package com.hehongdan.wifi_android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hehongdan.wifi_android.test.WifiController;
import com.hehongdan.wifi_android.test.WifiStateListener;
import com.hehongdan.wifi_android.test.WifiStateReceiver;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 *
 */
@RuntimePermissions
public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener,View.OnClickListener {
    /**
     * WiFi状态监听
     */
    private WifiStateListener HHDlistener = new WifiStateListener() {
        @Override
        public void onCurrentState(State state) {
            switch (state){
                case OPENED:
                    HHDLog.v("WiFi已打开");
                    tv_state.setText("WiFi已打开");
                    break;
                case CLOSED:
                    HHDLog.v("WiFi已关掉");
                    tv_state.setText("WiFi已关掉");
                    break;
                case CONNECTING:
                    HHDLog.v("连接中...");
                    tv_state.setText("连接中...");
                    break;
                case AUTHENTICATING:
                    HHDLog.v("验证中...");
                    tv_state.setText("验证中...");
                    break;
                case OBTAINING_IPADDR:
                    HHDLog.v("获取IP中...");
                    tv_state.setText("获取IP中...");
                    break;
                case CONNECTED:
                    HHDLog.e("已经连接（调用一次）=" + mWifiController.getCurrentSsid());
                    tv_state.setText("成功连接：" + mWifiController.getCurrentSsid());
                    break;
                case SCAN_RESULT:
                    HHDLog.v("WiFi扫描返回...");
                    //在这里处理wifi的结果
                    mWifiScanResult = mWifiController.getWifiScanResult();
                    //扫描到结果以后,就开始更新界面
                    if (null != mWifiScanResult) {
                        Toast.makeText(MainActivity.this, "WiFi扫描返回结果=" + mWifiScanResult.size(), Toast.LENGTH_SHORT).show();
                        if (null != mHHDAdapter) {
                            mHHDAdapter.setListData(mWifiScanResult);
                            mHHDAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.v(TAG, "WiFi扫描返回=null");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private MyLogger HHDLog = MyLogger.HHDLog();
    private static final String TAG = "MainActivity";
    /** WiFi控制器 */
    private WifiController mWifiController;
    /** WiFi状态广播接收器 */
    private WifiStateReceiver mWifiStateReceiver;
    /** 扫描结果视图列表 */
    private RecyclerView mRecyclerView;
    /** 扫描结果数据列表 */
    private List<ScanResult> mWifiScanResult;
    /** 扫描结果适配器 */
    private WifiListAdapter mHHDAdapter;
    /** WiFi开关 */
    //private Switch wifiOpenOrClose;
    /** WiFi状态 */
    private TextView tv_state;
    /** WiFi打开 */
    private Button btn_open;
    /** WiFi关闭 */
    private Button btn_close;
    /** WiFi连接 */
    private Button btn_connect;
    /** WiFi断开 */
    private Button btn_disconnect;
    /** 上下文 */
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
        mWifiController.connect(scanResult, new WifiController.INeedPassword() {
            @Override
            public void isNeed(boolean need) {
                if (need) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(ssid)
                            .setView(editText)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String password = editText.getText().toString().trim();
                                    mWifiController.connectByPassword(scanResult, password);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }
        });
    }


    /**
     * 初始化视图
     */
    private void initView() {
        tv_state = (TextView) findViewById(R.id.tv_state);
        btn_open = (Button) findViewById(R.id.btn_open);
        btn_open.setOnClickListener(this);
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(this);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);
        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(this);
        //wifiOpenOrClose = (Switch) findViewById(R.id.wifiOpenOrClose);
        //wifiOpenOrClose.setOnCheckedChangeListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //mAdapter = new WifiListAdapter(this, newListener());
        mHHDAdapter = new WifiListAdapter(this,mWifiScanResult);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mHHDAdapter);

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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId){
            case R.id.btn_open:
                mWifiController.openWifi();
                break;
            case R.id.btn_close:
                mWifiController.closeWifi();
                break;
            case R.id.btn_connect:
                Toast.makeText(mContext,"选择底部列表进行连接",Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_scan:
                mWifiController.scanWifiAround();
                break;
            case R.id.btn_disconnect:
                Toast.makeText(mContext,"正在断开...",Toast.LENGTH_LONG).show();
                mWifiController.disconnectCurrent();
                break;

            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mWifiStateReceiver = new WifiStateReceiver(HHDlistener);
        mWifiController = WifiController.getInstant(getApplicationContext());
        if (mWifiController.isWifiEnabled()) {
            //wifiOpenOrClose.setChecked(true);
            mWifiController.scanWifiAround();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播接收者
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销广播接收者
        unregisterReceiver();
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
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        //设置意图过滤
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //网络连接发生变化
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //网络连接发生变化
        //RSSI（信号强度）已经改变
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        registerReceiver(mWifiStateReceiver, filter);
    }

    /**
     * 取消注册广播接收者
     */
    private void unregisterReceiver() {
        unregisterReceiver(mWifiStateReceiver);
    }

}
