package com.wanjie.wifidemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Administrator on 2017/7/27.
 */

public class WifiReceiver extends BroadcastReceiver {

    private final WifiReceiverActionListener mListener;

    public WifiReceiver(WifiReceiverActionListener listener) {
        //通过构造方法把回调接口扔进来
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //判断Action
        String action = intent.getAction();

        if (null == mListener) {
            return;
        }

        switch (action) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                //WIFI打开还是关闭或者是位置状态
                handlerWifiState(intent);
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                //扫描结果
                handlerWifiScanResult();
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                //用于判断连接状态
                handlerWifiConnectState(intent);
                break;
        }

    }

    /**
     * 这个方法用于处理wifi的连接状态发生改变
     *
     * @param intent
     */
    private void handlerWifiConnectState(Intent intent) {

        //拿到NetworkInfo
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        //判断连接上了哈
        if (null != networkInfo && networkInfo.isConnected()) {
            //连接上了,就把wifi的信息传出去
            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                //把结果回传出去
                mListener.onWifiConnected(wifiInfo);
            }
        }

    }


    /**
     * 这个方法用于通知wifi扫描有结果
     */
    private void handlerWifiScanResult() {
        mListener.onWifiScanResultBack();
    }


    /**
     * 这个方法用于处理wifi的状态,打开,打开中..关闭,关闭中..
     *
     * @param intent
     */
    private void handlerWifiState(Intent intent) {
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        //对wifi的状态进行处理
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                //wifi已经打开..
                mListener.onWifiOpened();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                //wifi打开中..
                mListener.onWifiOpening();
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                //wifi关闭了..
                mListener.onWifiClosed();
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                //wifi关闭中..
                mListener.onWifiClosing();
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                //未知状态..
                break;
        }
    }
}
