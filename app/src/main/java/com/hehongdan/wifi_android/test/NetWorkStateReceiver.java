package com.hehongdan.wifi_android.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Android：检测网络状态&监听网络变化
 * https://www.jianshu.com/p/983889116526
 *
 * Created by Carson_Ho on 16/10/31.
 */
public class NetWorkStateReceiver extends BroadcastReceiver {

    private NetworkInfo.State cacheState;
    private int netType;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e("何洪丹","bundle为空不往下处理");
                return;
            }
            NetworkInfo netInfo = (NetworkInfo) bundle.get(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = netInfo.getState();
            int netInfoType = netInfo.getType();
            if (cacheState == null) {
                cacheState = state;
                // 移动网络 (2G/3G/4G 间切换)  or  wifi
                netType = netInfoType;
            } else if (cacheState == state && netType == netInfo.getType()) {
                Log.i("何洪丹", "相同状态广播则返回="+state.name()+"，对比类型"+netInfo.getTypeName() + "，类型(int)=" + netInfo.getType());
                return;
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                Log.i("何洪丹", "ConnectivityManager为空则返回");
                return;
            }
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                int activeNetType = activeNetInfo.getType();
                Log.i("何洪丹", "类型名称="+activeNetInfo.getTypeName()+"，类型(int)="+activeNetInfo.getType());
                // 类型不同  认为是中间状态 不处理
                if (activeNetType != netInfoType) {
                    Log.i("何洪丹", "类型不同，处于中间状态（不处理），对比类型="+activeNetType+"<=>"+netInfoType);
                } else {
                    cacheState = state;
                    netType = netInfoType;
                    Log.i("何洪丹", "类型相同，对比类型="+activeNetType+"<=>"+netInfoType);
                    //Log.i("MyReceiver", "当前操作   state : " + state.name() + " -- " + netInfo.getTypeName() + " : " + netInfo.getType());
                }
            } else {
                Log.i("何洪丹", "activeNetInfo为空");
                cacheState = state;
                netType = netInfoType;
                //Log.i("MyReceiver", "当前操作   state : " + state.name() + " -- " + netInfo.getTypeName() + " : " + netInfo.getType());
            }
        }

        

    
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        System.out.println("网络状态发生变化");
        //检测API是不是小于21，因为到了API21之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
            }
        }else {
            //这里的就不写了，前面有写，大同小异
            System.out.println("API level 大于21");
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //Log.e("何洪丹",networkInfo.getTypeName()+"网络变化（防止调用两次）");

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            for (int i=0; i < networks.length; i++){
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }
            Toast.makeText(context, sb.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}
