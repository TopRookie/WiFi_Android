package com.hehongdan.wifi_android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 类描述：测试WiFi各个功能
 *
 * @author hehongdan
 * @version v2018/8/25
 * @date 2018/8/25
 *
 */
@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * WiFi状态监听
     */
    private WifiStateListener mWifiStateListener = new WifiStateListener() {
        @Override
        public void onCurrentState(State state) {
            Message message = new Message();
            message.what = mWhat;
            String string;
            switch (state){
                case OPENED:
                    string = "WiFi已打开";
                    message.obj = string;
                    mMyHandler.sendMessage(message);
                    HHDLog.v(string);
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
                case FAILED:
                    HHDLog.e("连接失败");
                    tv_state.setText("连接失败");
                    break;
                case DISCONNECTED:
                    HHDLog.v("已经断开");
                    tv_state.setText("已经断开");
                    break;
                case FORGET_SUCCESS:
                    string = "忘记成功";
                    message.obj = string;
                    mMyHandler.sendMessage(message);
                    HHDLog.v(string);
                    break;
                case FORGET_FAILURE:
                    string = "忘记失败";
                    message.obj = string;
                    mMyHandler.sendMessage(message);
                    HHDLog.v(string);
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
                        HHDLog.v("WiFi扫描返回=null");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private static class MyHandler extends Handler{
        WeakReference<MainActivity> weakReference;
        public MyHandler(MainActivity activity){
            weakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            String obj;
            switch (msg.what) {
                case mWhat:
                    obj = (String) msg.obj;
                    activity.tv_state.setText(obj);
                    break;
                default:
                    break;
            }
        }
    }

    private MyHandler mMyHandler = new MyHandler(MainActivity.this);
    private static final int mWhat = 1;
    private static MyLogger HHDLog = MyLogger.HHDLog();
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
    /** WiFi忘记 */
    private Button bnt_forget;
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
        btn_disconnect = (Button) findViewById(R.id.btn_scan);
        btn_disconnect.setOnClickListener(this);
        bnt_forget = (Button) findViewById(R.id.bnt_forget);
        bnt_forget.setOnClickListener(this);
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
                int id = mWifiController.getNetworkIdFromScanResult(mWifiScanResult.get(position));
                mWifiController.forget(id, new ProxyForgetListener(),mWifiStateListener);
                Toast.makeText(mContext, "需要忘记的网络ID=" + id, Toast.LENGTH_LONG).show();
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
                Toast.makeText(mContext,"选择底部列表，短按进行连接",Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_scan:
                mWifiController.scanAround();
                break;
            case R.id.btn_disconnect:
                Toast.makeText(mContext,"正在断开...",Toast.LENGTH_LONG).show();
                mWifiController.disconnectCurrent();
                break;
            case R.id.bnt_forget:
                Toast.makeText(mContext,"选择底部列表，长按进行忘记",Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mWifiStateReceiver = new WifiStateReceiver(mWifiStateListener);
        mWifiController = WifiController.getInstant(getApplicationContext());
        if (mWifiController.isWifiEnabled()) {
            //wifiOpenOrClose.setChecked(true);
            mWifiController.scanAround();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播接收者
        registerWifiBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销广播接收者
        //unregisterWifiBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HHDLog.e("界面销毁");
    }

    //    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        mWifiController.setWifiEnabled(isChecked);
//    }

    /**
     * 在这里注册广播接收者
     */
    private void registerWifiBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        /*设置意图过滤
        wifi开启状态的监听(wifi关闭,wifi打开)*/
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //wifi连接的广播
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //wifi连接状态改变的广播
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
    private void unregisterWifiBroadcastReceiver() {
        unregisterReceiver(mWifiStateReceiver);
    }

}
