package com.hehongdan.wifi_android.test;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.hehongdan.wifi_android.InterfaceProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class WifiController {
    private static final String TAG = "WifiController";
    private final WifiManager mWifiManager;
    private final Context mContext;
    private OnWifiConnectListener mOnWifiConnectListener;

    private WifiController(Context context) {
        //拿到wifi管理器
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.mContext = context;
    }

    private static WifiController sInstant = null;

    /**
     * 获取 一个单例对象
     *
     * @param context 上下文
     * @return 返回实例对象
     */
    public static WifiController getInstant(Context context) {
        if (sInstant == null) {
            synchronized (WifiController.class) {
                if (sInstant == null) {
                    sInstant = new WifiController(context);
                }
            }
        }
        return sInstant;
    }

    /**
     * 是否需要输入密码
     */
    public interface INeedPassword {
        /**
         * 是否需要输入密码
         *
         * @param need  false=不需要
         */
        void isNeed(boolean need);
    }
    /** 是否需要输入密码回调接口 */
    private INeedPassword iNeedPassword;
    /**
     * 连接WiFi
     *
     * @param scanResult    指定扫描结果
     * @param iNeedPassword 需要密码回调接口
     */
    public void connect(final ScanResult scanResult, @NonNull INeedPassword iNeedPassword){
        this.iNeedPassword = iNeedPassword;
        final WifiConfiguration wifiConfiguration = isExsits(scanResult.SSID);
        //是否可以执行连接
        boolean isEnableNetwork = false;
        if (null == wifiConfiguration) {
            // 加密方式
            SecurityMode securityMode = getSecurityMode(scanResult);
            if (SecurityMode.WPA == securityMode || SecurityMode.WPA2 == securityMode || SecurityMode.WEP == securityMode) {
                iNeedPassword.isNeed(true);
            }
            if (SecurityMode.OPEN == securityMode) {
                isEnableNetwork = connectNoPassword(scanResult);
            }
        } else {
            //已经连接过
            isEnableNetwork = connectByExist(scanResult);
        }
        Log.e("是否执行连接=", "" + isEnableNetwork);
    }
    /**
     * 连接WiFi（美易来）
     *
     * @param scanResult    指定扫描结果
     * @param password      WiFi密码
     */
    public void connectWifi(final ScanResult scanResult, @Nullable String password){
        final WifiConfiguration wifiConfiguration = isExsits(scanResult.SSID);
        //是否可以执行连接
        boolean isEnableNetwork = false;
        if (null == wifiConfiguration) {
            // 加密方式
            SecurityMode securityMode = getSecurityMode(scanResult);
            if (SecurityMode.WPA == securityMode || SecurityMode.WPA2 == securityMode || SecurityMode.WEP == securityMode) {
                connectByPassword(scanResult, password);
            }
            if (SecurityMode.OPEN == securityMode) {
                isEnableNetwork = connectNoPassword(scanResult);
            }
        } else {
            //已经连接过
            isEnableNetwork = connectByExist(scanResult);
        }
        Log.e("是否执行连接=", "" + isEnableNetwork);
    }
    /**
     * 通过网络ID连接系统存在的WiFi（已经连接过）
     *
     * @param scanResult    WiFi扫描结果
     * @return              是否执行连接
     */
    public boolean connectByExist(final ScanResult scanResult) {
        //WiFi名称
        String ssid = scanResult.SSID;
        //连接指定Id的网络，并使其他网络都禁用（参数：指定网络的Id，是否禁用其他网络；返回：true，让WifiManager执行连接命令）
        return mWifiManager.enableNetwork(getNetworkId(ssid), true);
    }

    /**
     * 通过网络ID连接系统存在的WiFi（已经连接过）
     *
     * @param scanResult    WiFi扫描结果
     * @return              是否执行连接
     */
    public boolean connectNoPassword(final ScanResult scanResult) {
        //WiFi名称
        String ssid = scanResult.SSID;
        // 加密方式
        SecurityMode securityMode = getSecurityMode(scanResult);
        //生成配置文件
        WifiConfiguration addConfig = createWifiConfiguration(ssid, null, securityMode);
        //重新生成配置（判断是否存在）
        WifiConfiguration updateConfig = isExists(addConfig);
        int networkId;
        //有更新
        if (null != updateConfig){
            //更新一个网络连接（参数：网络配置信息；返回：大于0，则表示操作成功）
            networkId = mWifiManager.updateNetwork(updateConfig);
        } else {
            networkId = mWifiManager.addNetwork(addConfig);
        }

        if (-1 == networkId){
            // 提示连接WIFI失败
            /*if (null != mOnWifiConnectListener) {
                mOnWifiConnectListener = null;//TODO 回调失败
            }*/
            return false;
        }

        //FIXME 断开当前连接，并判断断开结果（需要权限）
        if (false){
            boolean isDisconnect = disconnectCurrent();
            if (!isDisconnect) {
                // 断开当前网络失败

                // 提示连接WIFI失败
            /*if (null != mOnWifiConnectListener) {
                mOnWifiConnectListener = null;//TODO 回调失败
            }*/
                return false;
            }
        }

        //连接指定Id的网络，并使其他网络都禁用（参数：指定网络的Id，是否禁用其他网络；返回：true，让WifiManager执行连接命令）
        boolean isEnable = mWifiManager.enableNetwork(networkId, true);
        //0代表连接上
        //mWifiManager.getConfiguredNetworks().get(networkId).status;
        if (!isEnable){
            //WifiManager没执行连接命令

            // 提示连接WIFI失败
            /*if (null != mOnWifiConnectListener) {
                mOnWifiConnectListener = null;//TODO 回调失败
            }*/
        }
        return isEnable;
    }

    /**
     * 连接已经存在的WiFi（带密码）
     *
     * @param scanResult    指定扫描结果
     * @param password      WiFi密码
     * @return              是否执行连接
     */
    public boolean connectByPassword(ScanResult scanResult, @NonNull String password) {
        //WiFi名称
        String ssid = scanResult.SSID;
        // 加密方式
        SecurityMode securityMode = getSecurityMode(scanResult);
        // WiFi网络配置信息
        WifiConfiguration configuration = createWifiConfiguration(ssid, password, securityMode);
        //新网络ID
        int networkId = mWifiManager.addNetwork(configuration);
        //连接指定Id的网络，并使其他网络都禁用（参数：指定网络的Id，是否禁用其他网络；返回：true，让WifiManager执行连接命令）
        return mWifiManager.enableNetwork(networkId, true);
    }

    /**
     * 生成新的配置信息 用于连接Wifi
     *
     * @param SSID     WIFI名字
     * @param password WIFI密码
     * @param mode     WIFI加密类型
     * @return 配置
     */
    public WifiConfiguration createWifiConfiguration(@NonNull String SSID, @Nullable String password, @NonNull SecurityMode mode) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if (mode == SecurityMode.OPEN) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (mode == SecurityMode.WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (mode == SecurityMode.WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 判断配置在系统中是否存在
     *
     * @param config 新的配置
     * @return 配置存在就更新配置，把新的配置返回，配置不存在就返回null
     */
    private WifiConfiguration isExists(WifiConfiguration config) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            Log.i(TAG, "系统保存配置SSID=" + existingConfig.SSID + "，网络ID=" + existingConfig.networkId);
            if (existingConfig.SSID.equals(config.SSID)) {
                config.networkId = existingConfig.networkId;
                return config;
            }
        }
        return null;
    }

    /**
     * 根据SSID判断并返回系统操作的WiFi配置
     *
     * @param ssid  WiFi名称
     * @return      系统操作的配置
     */
    public WifiConfiguration isExsits(String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(appendSsid(ssid))) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * @return 返回扫描的wifi结果, 是一个list集合
     * @call 当收到扫描结果的广播以后就可以调用这个方法去获取扫描结果
     */
    public List<ScanResult> getWifiScanResult() {
        return mWifiManager.getScanResults();
    }


    /**
     * 获取当前的SSID（WiFi名称）
     *
     * @return  返回SSID（WiFi名称）
     */
    public String getCurrentSsid(){
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    /**
     * 扫描周围的WiFi
     */
    public void scanWifiAround() {
        if (!isWifiEnabled()) {
            //TODO 优化
            //开启再扫描（提示用户）
            //wifiManager.setWifiEnabled(true);
            return;
        }
        mWifiManager.startScan();
    }

    /**
     * @return true表示wifi已经打开, false表示wifi没有打开, 状态为:
     * 打开中,或者关闭,或者关闭中...
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        //判断当前wifi的状态,是关闭还是打开
        if (!isWifiEnabled()) {
            setWifiEnabled(true);

        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        //判断当前wifi的状态,是关闭还是打开
        if (isWifiEnabled()) {
            setWifiEnabled(false);
        }
    }

    /**
     * 设置WiFi是否可用
     *
     * @param enabled   true 打开WiFi，false 关闭WiFi
     */
    public void setWifiEnabled(boolean enabled){
        mWifiManager.setWifiEnabled(enabled);
    }

    /**
     * 根据SSID获取配置ID
     *
     * @param ssid  WiFi名称
     * @return      返回对应的网络ID（-1没在系统保存中找到）
     */
    public int getNetworkId(final String ssid){
        //获取配置列表
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        //网络ID
        int networkId = -1;
        //配置列表不为空
        if (null != wifiConfigurations){
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                //拿到指定的WiFi配置
                if (wifiConfiguration.SSID.equals(appendSsid(ssid))){
                    networkId = wifiConfiguration.networkId;
                    return networkId;
                }
            }
        }
        return networkId;
    }

    /**
     * 根据网络Id断开WiFi
     *
     * @param netId netId
     * @return 是否断开
     */
    public boolean disconnect(int netId) {
        boolean isDisable = mWifiManager.disableNetwork(netId);
        boolean isDisconnect = mWifiManager.disconnect();
        return isDisable && isDisconnect;
    }

    /**
     * 断开当前WiFi
     *
     * @return 是否断开
     */
    public boolean disconnectCurrent() {
        // 获取当前的网络连接
        WifiInfo wifiInfo = getCurrentInfo();
        if (null != wifiInfo) {
            // 断开当前连接
            return disconnect(wifiInfo.getNetworkId());
        }
        return false;
    }

    /**
     * 拼接带"\"SSID
     *
     * @param ssid
     * @return
     */
    public static String appendSsid(String ssid) {
        return "\"" + ssid + "\"";
    }

    /**
     * 获取WIFI的加密方式
     *
     * @param scanResult    WIFI信息
     * @return              加密方式（null=不确定）
     */
    public SecurityMode getSecurityMode(@NonNull ScanResult scanResult) {
        String capabilities = scanResult.capabilities;
        if (TextUtils.isEmpty(capabilities)) {
            return null;
        }
        //不区分大小写//capabilities.equalsIgnoreCase();
        //判断是否包含
        if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return SecurityMode.WPA;
        } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
            return SecurityMode.WEP;
        } else if (capabilities.contains("WPA2") || capabilities.contains("wpa2")) {
            return SecurityMode.WPA2;
        } else {
            // 没有加密
            return SecurityMode.OPEN;
        }
    }

    /**
     * 获取当前WiFi信息
     *
     * @return 当前WiFi信息
     */
    public WifiInfo getCurrentInfo() {
        try {
            return mWifiManager.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }












    /**
     * 这个方法用于打开或关闭wifi
     * 当wifi打开的时候,那么就会关闭wifi
     * 当wifi关闭的时候,那么就会打开wifi
     */
    public void openOrCloseWifi() {
        //判断当前wifi的状态,是关闭还是打开
        if (this.isWifiEnabled()) {
            setWifiEnabled(false);
        } else {
            setWifiEnabled(true);
        }
    }

    /**
     * 获取NetworkId
     *
     * @param scanResult 扫描到的WIFI信息
     * @return 如果有配置信息则返回配置的networkId 如果没有配置过则返回-1
     */
    public int getNetworkIdFromConfig(ScanResult scanResult) {
        String SSID = String.format("\"%s\"", scanResult.SSID);
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(SSID)) {
                return existingConfig.networkId;
            }
        }

        return -1;
    }




    /**
     * 这个枚举用于表示网络加密模式
     */
    public enum SecurityMode {
        OPEN, WEP, WPA, WPA2
    }




    /**
     * 获取WiFi（许可）加密方式（wifi隐藏ssid还可以获取加密方式）
     * <p>
     *     (1).WPA-PSK/WPA2-PSK(目前最安全家用加密)
     *     (2).WPA/WPA2(较不安全)
     *     (3).WEP(安全较差)
     *     (4).EAP(迄今最安全的)
     * </p>
     *
     * @param wifiConfiguration WiFi配置信息
     * @return                  加密方式
     */
    public int getAllowByConfig(WifiConfiguration wifiConfiguration){
        if (wifiConfiguration.allowedKeyManagement.get(KeyMgmt.WPA_PSK)){
            return KeyMgmt.WPA_PSK;
        }
        if (wifiConfiguration.allowedKeyManagement.get(KeyMgmt.WPA_EAP)){
            return KeyMgmt.WPA_EAP;
        }
        return (wifiConfiguration.wepKeys[0] != null)? KeyMgmt.IEEE8021X : KeyMgmt.NONE;
    }



    /**
     * @param ssid 可以理解为是wifi的名字
     * @return 反回的是wifi配置对象
     */
    public WifiConfiguration getConfigurationBySSID(String ssid) {

        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : configuredNetworks) {
            Log.i(TAG, "根据SSID获取系统保存配置=" + ssid);
            if (existingConfig.SSID.equals(ssid)) {
                return existingConfig;
            }
        }

        return null;
    }

    /**
     * 弃用：连接已经存在的WiFi（带密码）
     *
     * @param config    WiFi网络配置信息
     */
    @Deprecated
    public void connect(WifiConfiguration config) {
        int networkId = mWifiManager.addNetwork(config);
        mWifiManager.enableNetwork(networkId, true);
    }

    /**
     * 通过密码连接到WIFI
     *
     * @param scanResult 要连接的WIFI
     * @param pwd        密码
     * @param listener   连接的监听
     */
    @Deprecated
    public void connectionWifiByPassword(@NonNull ScanResult scanResult, @Nullable String pwd, @NonNull OnWifiConnectListener listener) {
        // SSID
        String SSID = scanResult.SSID;
        // 加密方式
        SecurityMode securityMode = getSecurityMode(scanResult);

        // 生成配置文件
        WifiConfiguration addConfig = createWifiConfiguration(SSID, pwd, securityMode);
        int netId;
        // 判断当前配置是否存在
        WifiConfiguration updateConfig = isExists(addConfig);
        if (null != updateConfig) {
            // 更新配置
            netId = mWifiManager.updateNetwork(updateConfig);
        } else {
            // 添加配置
            netId = mWifiManager.addNetwork(addConfig);
        }
        // 通过NetworkID连接到WIFI
        connectionWifiByNetworkId(SSID, netId, listener);
    }

    /**
     * 通过NetworkId连接到WIFI （配置过的网络可以直接获取到NetworkID，从而不用再输入密码）
     *
     * @param SSID      WIFI名字
     * @param networkId NetworkId
     * @param listener  连接的监听
     */
    @Deprecated
    public void connectionWifiByNetworkId(@NonNull String SSID, int networkId, @NonNull OnWifiConnectListener listener) {
        // 连接的回调监听
        mOnWifiConnectListener = listener;
        // 连接开始的回调
        mOnWifiConnectListener.onStart(SSID);
        /*
         * 判断 NetworkId 是否有效
         * -1 表示配置参数不正确，我们获取不到会返回-1.
         */
        if (-1 == networkId) {
            // 连接WIFI失败
            if (null != mOnWifiConnectListener) {
                // 配置错误
                mOnWifiConnectListener.onFailure(SSID);
                // 连接完成
                mOnWifiConnectListener.onFinish();
                mOnWifiConnectListener = null;
            }
            return;
        }
        // 获取当前的网络连接
        WifiInfo wifiInfo = getCurrentInfo();

        if (null != wifiInfo) {
            // 断开当前连接
            boolean isDisconnect = disconnect(wifiInfo.getNetworkId());
            if (!isDisconnect) {
                // 断开当前网络失败
                if (null != mOnWifiConnectListener) {
                    // 断开当前网络失败
                    mOnWifiConnectListener.onFailure(SSID);
                    // 连接完成
                    mOnWifiConnectListener.onFinish();
                    mOnWifiConnectListener = null;
                }
                return;
            }
        }

        // 连接WIFI
        boolean isEnable = mWifiManager.enableNetwork(networkId, true);
        if (!isEnable) {
            // 连接失败
            if (null != mOnWifiConnectListener) {
                // 连接失败
                mOnWifiConnectListener.onFailure(SSID);
                // 连接完成
                mOnWifiConnectListener.onFinish();
                mOnWifiConnectListener = null;
            }
        }
    }








    public void forget(int netId, InterfaceProxy listener) {

        //有部分码友说这使用删除的方式有时候会出现问题，后来去翻看了一下android设置里的源码。
        //是需是直接掉调用forget方法来删除指写的wifi配置的

        //但是这个方法是藏起来的方法，我们可以看到前面会有一个@hide方法

        /**
         * Delete the network in the supplicant config.
         *
         * This function is used instead of a sequence of removeNetwork()
         * and saveConfiguration().
         *
         * @param config the set of variables that describe the configuration,
         *            contained in a {@link WifiConfiguration} object.
         * @param listener for callbacks on success or failure. Can be null.
         * @throws IllegalStateException if the WifiManager instance needs to be
         * initialized again
         * @hide
         */
//        public void forget(int netId, ActionListener listener) {
//            if (netId < 0) throw new IllegalArgumentException("Network id cannot be negative");
//            validateChannel();
//            sAsyncChannel.sendMessage(FORGET_NETWORK, netId, putListener(listener));
//        }


        //那问题又来了，那个ActionListener前面又有一个@hide
        //哈哈哈！

        /**
         * Interface for callback invocation on an application action
         * @hide
         */
//        public interface ActionListener {
        /** The operation succeeded */
//            public void onSuccess();
        /**
         * The operation failed
         * @param reason The reason for failure could be one of
         * {@link #ERROR}, {@link #IN_PROGRESS} or {@link #BUSY}
         */
//            public void onFailure(int reason);
//        }


        //所以这里面我们就要这样子做了
        //这部分代码是通过反射的方法去获取到ActionListener这个类的字节码对象
        Class<?> actionListenerClazz = null;
        try {
            actionListenerClazz = Class.forName("android.net.wifi.WifiManager$ActionListener");
            Log.d(TAG, "name == " + actionListenerClazz.getName());
            Method[] declaredMethods = actionListenerClazz.getDeclaredMethods();
            Log.d(TAG, "method Size == " + declaredMethods.length);
            //这些输出，只是为了验证我们拿到的是对的哦！
            for (int i = 0; i < declaredMethods.length; i++) {
                Log.d(TAG, "mohtod Name == " + declaredMethods[i].getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (actionListenerClazz == null) {
            throw new RuntimeException("fail to get ActionListener...");
        }

        Class<? extends WifiManager> wifiClazz = mWifiManager.getClass();
        try {
            Method forget = wifiClazz.getDeclaredMethod("forget", int.class, actionListenerClazz);
            Log.d(TAG, "method name == " + forget);
            //执行方法
            forget.invoke(mWifiManager, netId, listener);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "e == " + e);
        }

        //如果是直接放在系统中编译的话，那么直接是调用wifiManager里的forget方法即可

    }



    public interface OnWifiConnectListener{
        void onStart(String SSID);
        void onFinish();
        void onFailure(String SSID);
    }
}
