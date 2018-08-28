package com.hehongdan.wifi_android.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 *
 * Created by MYL on 2018/8/24.
 */

public class WifiStateReceiver extends BroadcastReceiver {
    
    private MyLogger HHDLog = MyLogger.HHDLog();
    private final String TAG = "HHDWifiStateReceiver";
    private final HHDWifiReceiverActionListener mListener;
    /** 缓存网络状态（连接、连接中、断开、断开中、暂停、未知） */
    private NetworkInfo.State connectState;
    /** 缓存网络类型（WIFI、MOBILE...） */
    private int networkType;
    /** 是否启用去重功能（避免广播多次调用） */
    private boolean isRemoveDuplicates;
    /** 临时屏蔽广播 */
    private boolean temporaryShield = true;

    /**
     * 构造方法
     *
     * @param mListener 广播接收器的回调监听器
     */
    public WifiStateReceiver(HHDWifiReceiverActionListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 构造方法
     *
     * @param mListener         广播接收器的回调监听器
     * @param removeDuplicates  是否去重复广播
     */
    public WifiStateReceiver(HHDWifiReceiverActionListener mListener, @NonNull boolean removeDuplicates) {
        this.mListener = mListener;
        this.isRemoveDuplicates = removeDuplicates;
    }

    /**
     * 解析广播
     *
     * @param context   上下文
     * @param intent    意图
     */
    private void parseBroadcast(Context context, Intent intent){
        final String action = intent.getAction();
        //扫描结果
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
            HHDLog.v("WiFi扫描结果");
            mListener.onCurrentState(HHDWifiReceiverActionListener.State.SCAN_RESULT);
            //TODO 返回wifiManager.getScanResults();addAll(scanResults)
            //WiFi状态发生改变
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
            /**
             * @see #WIFI_STATE_DISABLED    已经禁用
             * @see #WIFI_STATE_DISABLING   正在禁用
             * @see #WIFI_STATE_ENABLED     已经启用
             * @see #WIFI_STATE_ENABLING    正在启用
             * @see #WIFI_STATE_UNKNOWN     未知
             */
            //WiFiT状态
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState){
                //已经启用
                case WifiManager.WIFI_STATE_ENABLED:
                    //通知扫描
                    //wifiManager.startScan();
                    HHDLog.v("WiFi状态改变，已经启用（打开）");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.OPENED);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //wifi关闭发出的广播
                    HHDLog.v("WiFi状态改变，已经禁用（关闭）");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.CLOSED);
                    break;

                default:
                    break;
            }
            //网络状态发生改变
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
            //网络信息
            NetworkInfo info_ = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            /**
             * CONNECTED,已经连接,
             * CONNECTING,正在连接,
             * DISCONNECTED,已经断开,
             * DISCONNECTING,正在断开,
             * SUSPENDED,暂停,
             * UNKNOWN;未知;
             */
            if (NetworkInfo.State.DISCONNECTED.equals(info_)){
                HHDLog.v("网络状态改变，已经无连接--------------------------------------------------");
                mListener.onCurrentState(HHDWifiReceiverActionListener.State.DISCONNECTED);
            }else if (NetworkInfo.State.CONNECTED.equals(info_.getState())){
                HHDLog.e("网络状态改变，已经连接="+"WiFi名称");
                //mListener.onConnected();
                if (isRemoveDuplicates) {
                    if (temporaryShield) {
                        mListener.onCurrentState(HHDWifiReceiverActionListener.State.CONNECTED);
                        temporaryShield = false;
                    }
                } else {
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.CONNECTED);
                }
                //粗粒度的网络状态（以上）
            } else {
                //细粒度的网络状态（以下）
                /**
                 /** 准备开始数据连接设置。* /
                 DLE,空闲,
                 /** 搜索一个可用的访问点。* /
                 SCANNING,扫描,
                 /** 当前建立数据连接。* /
                 CONNECTING,连接,
                 /** 网络连接建立，执行身份验证。* /
                 AUTHENTICATING,验证,
                 /** 等待DHCP服务器的响应，以便分配IP地址信息。* /
                 OBTAINING_IPADDR,
                 /** IP流量应该是可用的。* /
                 CONNECTED,连接,
                 /** IP流量被暂停* /
                 SUSPENDED,暂停,
                 /** 目前正在拆除数据连接。* /
                 DISCONNECTING,断开,
                 /** IP流量不可用。* /
                 DISCONNECTED,断开连接,
                 /** 尝试连接失败。* /
                 FAILED,失败了,
                 /** 对这个网络的访问被阻塞（已阻止）* /
                 BLOCKED,阻塞,
                 /** 链接的连通性很差。* /
                 VERIFYING_POOR_LINK,
                 /** 检查网络是否是一个被捕获的门户（判断是否需要浏览器二次登录）* /
                 CAPTIVE_PORTAL_CHECK
                 */
                NetworkInfo.DetailedState state = info_.getDetailedState();
                if (NetworkInfo.DetailedState.CONNECTING == state){
                    HHDLog.v("网络状态改变（细粒度），连接中...");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.CONNECTING);
                } else if (NetworkInfo.DetailedState.AUTHENTICATING == state){
                    HHDLog.v("网络状态改变（细粒度），验证中...");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.AUTHENTICATING);
                }else if (NetworkInfo.DetailedState.OBTAINING_IPADDR == state){
                    HHDLog.v("网络状态改变（细粒度），获取IP中...");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.OBTAINING_IPADDR);
                } else if (NetworkInfo.DetailedState.FAILED == state){
                    HHDLog.v("网络状态改变（细粒度），网络失败");
                    mListener.onCurrentState(HHDWifiReceiverActionListener.State.FAILED);
                }
            }
        }
    }

    /**
     * 根据网络切换前后类型判断连接状态（避免重复广播）
     *
     * @param context   上下文
     * @param intent    意图
     */
    private void getNetworkTypeInfo(Context context, Intent intent){
        String action = intent.getAction();
        //网络连接发生改变
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            //取出intent所附带的额外数据
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e("HHDWifiStateReceiver","bundle为空不往下处理");
                return;
            }
            //网络连接的额外信息（建议使用getActiveNetworkInfo()或getAllNetworkInfo()）
            NetworkInfo networkInfo = (NetworkInfo) bundle.get(ConnectivityManager.EXTRA_NETWORK_INFO);
            //当前网络状态
            NetworkInfo.State state = networkInfo.getState();
            //网络类型
            int netInfoType = networkInfo.getType();
            if (connectState == null) {
                connectState = state;
                // 移动网络 (2G/3G/4G或wifi间切换)
                networkType = netInfoType;
            } else if (connectState == state && networkType == networkInfo.getType()) {
                HHDLog.w("相同状态不处理，网络状态（连接、断开...）="+state.name()+"，网络类型名称（WIFI、MOBILE...）"+networkInfo.getTypeName() + "，类型(int)=" + networkInfo.getType());
                return;
            }
            //网络连接相关的管理器
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                HHDLog.w("ConnectivityManager为空则返回");
                return;
            }
            //当前网络连接的额外信息
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                //当前网络类型
                int activeNetType = activeNetworkInfo.getType();
                HHDLog.i("当前网络类型名称（WIFI、MOBILE...）="+activeNetworkInfo.getTypeName()+"，类型(int)="+activeNetType);
                //当前网络类型与网络类型不同（认为是中间状态）
                if (activeNetType != netInfoType) {
                    HHDLog.i("当前与之前类型不同，处于中间状态（不处理），类型(int)当前="+activeNetType+"<=>之前="+netInfoType);
                } else {
                    connectState = state;
                    networkType = netInfoType;
                    HHDLog.i("当前与之前类型相同（重新赋值），类型(int)当前=" + activeNetType + "<=>之前=" + netInfoType + "，之前连接状态=" + state);
                    if (isRemoveDuplicates){
                        temporaryShield = true;
                    }
                }
            } else {
                connectState = state;
                networkType = netInfoType;
                HHDLog.w("当前网络连接的额外信息为空，之前类型(int)=" + netInfoType + "之前连接状态=" + state);
            }
        }


        if (false){
            //考虑采用（但滞后）
            //final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()) {
                    String name = networkInfo.getTypeName();
                    if ("WIFI".equals(name)){
                        Log.v("HHDWifiStateReceiver","WiFi网络变化（防止调用两次）");
                    } else if ("GPRS".equals(name)) {
                        Log.v("HHDWifiStateReceiver","GPRS（防止调用两次）");
                    } else if ("UMTS".equals(name)) {
                        Log.v("HHDWifiStateReceiver","UMTS（防止调用两次）");
                    }
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == mListener) {
            return;
        }
        getNetworkTypeInfo(context, intent);
        parseBroadcast(context, intent);

    }
}
