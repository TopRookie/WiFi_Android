package com.hehongdan.wifi_android;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

/**
 * 类描述：忘记WiFi的代理回调接口
 *
 * @author hehongdan
 * @version v2018/8/25
 * @date 2018/8/25
 */
@Deprecated
public class ProxyInterface implements ActionListener {
    private static final String TAG = "WifiController";
    @Override
    public void onSuccess() {
        Log.e(TAG,"忘记WiFi成功");
    }

    @Override
    public void onFailure(int reason) {
        Log.e(TAG,"忘记WiFi失败");
    }
}
