package com.hehongdan.wifi_android.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by MYL on 2018/8/24.
 */

public class HHDWifiStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        //扫描结果
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
            Log.w("何洪丹", "WiFi扫描结果");
            Toast.makeText(context,"WiFi扫描返回结果",Toast.LENGTH_SHORT).show();
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
                    Log.w("何洪丹", "WiFi状态改变，已经启用");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //wifi关闭发出的广播
                    Log.w("何洪丹", "WiFi状态改变，已经禁用");
                    break;

                default:
                    break;
            }
            //网络状态发生改变
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
            //网络信息
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            /**
             * CONNECTED,已经连接,
             * CONNECTING,正在连接,
             * DISCONNECTED,已经断开,
             * DISCONNECTING,正在断开,
             * SUSPENDED,暂停,
             * UNKNOWN;未知;
             */
            if (NetworkInfo.State.DISCONNECTED.equals(info)){
                Log.w("何洪丹", "网络状态改变，已经断开");
            }else if (NetworkInfo.State.CONNECTED.equals(info.getState())){
                Log.w("何洪丹", "网络状态改变，已经连接="+"WiFi名称");
                //粗粒度的网络状态（以上）
            }else {
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
                 /** IP流量被暂停/
                 SUSPENDED,暂停,
                 /** 目前正在拆除数据连接。* /
                 DISCONNECTING,断开,
                 /** IP流量不可用。* /
                 DISCONNECTED,断开连接,
                 /** 尝试连接失败。* /
                 FAILED,失败了,
                 /** 对这个网络的访问被阻塞。* /
                 BLOCKED,阻塞,
                 /** 链接的连通性很差。* /
                 VERIFYING_POOR_LINK,
                 /** 检查网络是否是一个被捕获的门户/
                 CAPTIVE_PORTAL_CHECK
                 */
                NetworkInfo.DetailedState state = info.getDetailedState();
                if (NetworkInfo.DetailedState.CONNECTING == state){
                    Log.w("何洪丹", "网络状态改变（细粒度），连接中...");
                } else if (NetworkInfo.DetailedState.AUTHENTICATING == state){
                    Log.w("何洪丹", "网络状态改变（细粒度），验证中...");
                }else if (NetworkInfo.DetailedState.OBTAINING_IPADDR == state){
                    Log.w("何洪丹", "网络状态改变（细粒度），获取IP中...");
                } else if (NetworkInfo.DetailedState.FAILED == state){
                    Log.w("何洪丹", "网络状态改变（细粒度），网络失败");
                }
            }
        }

    }
}
