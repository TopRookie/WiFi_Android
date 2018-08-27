package com.hehongdan.wifi_android.test;

import android.net.wifi.WifiInfo;

/**
 * Created by Administrator on 2017/7/27.
 */

public interface WifiReceiverActionListener {

    //wifi打开
    void onWifiOpened();

    //wifi打开中
    void onWifiOpening();

    //wifi关闭
    void onWifiClosed();

    //wifi关闭中
    void onWifiClosing();

    //有扫描结果了
    void onWifiScanResultBack();

    //连接上了
    void onWifiConnected(WifiInfo wifiInfo);
}
