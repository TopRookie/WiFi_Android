package com.hehongdan.wifi_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

/**
 * 类描述：WiFi状态的广播接收器
 *
 * @author hehongdan
 * @version v2018/8/27
 * @date 2018/8/27
 */

public class WifiStateReceiver extends BroadcastReceiver {
    
    private MyLogger HHDLog = MyLogger.HHDLog();
    private final String TAG = "HHDWifiStateReceiver";
    /** WiFi状态监听器 */
    private final WifiStateListener mListener;
    /** 缓存网络状态（连接、连接中、断开、断开中、暂停、未知） */
    private NetworkInfo.State connectState;
    /** 缓存网络类型（WIFI、MOBILE...） */
    private int networkType;

    /**
     * 构造方法
     *
     * @param mListener 广播接收器的回调监听器
     */
    public WifiStateReceiver(WifiStateListener mListener) {
        this.mListener = mListener;
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
            mListener.onCurrentState(WifiStateListener.State.SCAN_RESULT);
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
                    mListener.onCurrentState(WifiStateListener.State.OPENED);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //wifi关闭发出的广播
                    HHDLog.v("WiFi状态改变，已经禁用（关闭）");
                    mListener.onCurrentState(WifiStateListener.State.CLOSED);
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
                mListener.onCurrentState(WifiStateListener.State.DISCONNECTED);
            }else if (NetworkInfo.State.CONNECTED.equals(info_.getState())){
                //TODO 这里会触发两次广播，改到方法（getNetworkTypeInfo()）
                //HHDLog.e("网络状态改变，已经连接="+"WiFi名称");
                //mListener.onCurrentState(WifiStateListener.State.CONNECTED);
                //粗粒度的网络状态（以上）
            } else {
                //细粒度的网络状态（以下）
                NetworkInfo.DetailedState state = info_.getDetailedState();
                if (NetworkInfo.DetailedState.CONNECTING == state){
                    HHDLog.v("网络状态改变（细粒度），连接中...");
                    mListener.onCurrentState(WifiStateListener.State.CONNECTING);
                } else if (NetworkInfo.DetailedState.AUTHENTICATING == state){
                    HHDLog.v("网络状态改变（细粒度），验证中...");
                    mListener.onCurrentState(WifiStateListener.State.AUTHENTICATING);
                }else if (NetworkInfo.DetailedState.OBTAINING_IPADDR == state){
                    HHDLog.v("网络状态改变（细粒度），获取IP中...");
                    mListener.onCurrentState(WifiStateListener.State.OBTAINING_IPADDR);
                } else if (NetworkInfo.DetailedState.FAILED == state){
                    HHDLog.v("网络状态改变（细粒度），网络失败");
                    mListener.onCurrentState(WifiStateListener.State.FAILED);
                }



                /*else if (NetworkInfo.DetailedState.SCANNING== state){
                    HHDLog.v("网络状态改变（细粒度），SCANNING 扫描中");
                    mListener.onCurrentState(WifiStateListener.State.SCAN_RESULT);
                }*/

                /*else if (NetworkInfo.DetailedState.DISCONNECTED == state){
                    HHDLog.v("网络状态改变（细粒度），DISCONNECTED 已断开");
                    mListener.onCurrentState(WifiStateListener.State.DISCONNECTED);
                }else if (NetworkInfo.DetailedState. DISCONNECTING== state){
                    HHDLog.v("网络状态改变（细粒度），DISCONNECTING 断开中...");
                }
                else if (NetworkInfo.DetailedState.BLOCKED == state){
                    HHDLog.v("网络状态改变（细粒度），BLOCKED 阻塞");
                }else if (NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK == state){
                    HHDLog.v("网络状态改变（细粒度），CAPTIVE_PORTAL_CHECK 捕获网站");
                }else if (NetworkInfo.DetailedState.CONNECTED == state){
                    HHDLog.v("网络状态改变（细粒度），CONNECTED 已连接");
                }else if (NetworkInfo.DetailedState. FAILED== state){
                    HHDLog.v("网络状态改变（细粒度），FAILED 连接失败");
                }else if (NetworkInfo.DetailedState.IDLE== state){
                    HHDLog.v("网络状态改变（细粒度），IDLE");
                }else if (NetworkInfo.DetailedState.SUSPENDED== state){
                    HHDLog.v("网络状态改变（细粒度），SUSPENDED 暂停");
                }else if (NetworkInfo.DetailedState.VERIFYING_POOR_LINK== state){
                    HHDLog.v("网络状态改变（细粒度），VERIFYING_POOR_LINK 连接差");
                }*/

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
                    HHDLog.e("这里 设置连接回调可以避免广播重复问题");
                    mListener.onCurrentState(WifiStateListener.State.CONNECTED);
                }
            } else {
                connectState = state;
                networkType = netInfoType;
                HHDLog.w("当前网络连接的额外信息为空，之前类型(int)=" + netInfoType + "之前连接状态=" + state);
            }
        }

        if (false){
            //考虑采用（与上面功能上一致）
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
