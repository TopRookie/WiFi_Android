package com.hehongdan.wifi_android;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * WiFi控制类
 *
 * 参考：
 * https://github.com/AndroidKun/WiFihotspot
 *
 * Created by Administrator on 2017/7/27.
 */

public class WifiController {
    private MyLogger HHDLog = MyLogger.HHDLog();
    private static final String TAG = "WifiController";
    /** WiFi管理类 */
    private final WifiManager mWifiManager;
    /** 上下文 */
    private final Context mContext;
    /** WiFi连接监听器 */
    private WifiStateListener mWifiStateListener;
    /** 是否需要输入密码回调接口 */
    private INeedPassword iNeedPassword;


    /**
     * 网络加密模式（枚举）
     */
    public enum SecurityMode {
        /** 无密码（开放） */
        OPEN,
        /** WEP加密 */
        WEP,
        /** WPA加密 */
        WPA,
        /** WPA2加密 */
        WPA2
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


    /**
     * WiFi控制器构造函数（私有）
     *
     * @param context 上下文
     */
    private WifiController(Context context) {
        //拿到wifi管理器
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.mContext = context;
    }

    private static WifiController sInstant = null;

    /**
     * 获取 一个单例对象
     *
     * @param context   上下文
     * @return          返回实例对象（WiFi控制器）
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
     * 根据名称、密码、加密方式生成WiFi配置信息
     *
     * @param ssid      名称
     * @param password  密码
     * @param mode      加密方式
     * @return          WiFi配置信息
     */
    public WifiConfiguration createWifiConfiguration(@NonNull String ssid, @Nullable String password, @NonNull SecurityMode mode) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        if (mode == SecurityMode.OPEN) {
            config.allowedKeyManagement.set(KeyMgmt.NONE);
        } else if (mode == SecurityMode.WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (mode == SecurityMode.WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 查找系统是否存在此WiFi配置信息，存在就返回系统的相同名称WiFi的配置信息
     *
     * @param config    新WiFi的配置信息
     * @return          （null=不存在）返回系统的相同名称WiFi（更新的）配置信息
     */
    private WifiConfiguration isExists(WifiConfiguration config) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
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
            if (existingConfig.SSID.equals(appendSlash(ssid))) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 获取WiFi扫描结果
     *
     * @call    当收到WiFi扫描结果的广播后，调用这个方法获取扫描结果
     * @return  返回WiFi扫描结果的List集合
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
    public void scanAround() {
        if (!isWifiEnabled()) {
            //TODO 优化
            //开启再扫描（提示用户）
            //wifiManager.setWifiEnabled(true);
            return;
        }
        mWifiManager.startScan();
    }

    /**
     * 当前WiFi是否可用
     *
     * @return  true=已经打开，false=没打开（打开中、关闭中、打开、关闭）
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        //判断当前WiFi的状态是关闭
        if (!isWifiEnabled()) {
            setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        //判断当前WiFi的状态是打开
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
                if (wifiConfiguration.SSID.equals(appendSlash(ssid))){
                    networkId = wifiConfiguration.networkId;
                    return networkId;
                }
            }
        }
        return networkId;
    }

    /**
     * 断开并禁用指定WiFi（异步的，可能需要注册监听）
     *
     * @param networkId WiFi网络ID
     * @return          断开结果
     */
    public boolean disconnect(int networkId) {
        //禁用指定的网络（true=禁用操作成功）
        boolean isDisable = mWifiManager.disableNetwork(networkId);
        //断开连接（true=断开操作成功）
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
     * @param ssid  WiFi名称
     * @return      带"\"的WiFi名称
     */
    public static String appendSlash(String ssid) {
        return "\"" + ssid + "\"";
    }

    /**
     * 去掉前后"\"的SSID
     *
     * @param ssid  WiFi名称
     * @return      带"\"的WiFi名称
     */
    public static String subSlash(String ssid) {
        int len = ssid.length();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, len - 1);
        }
        return null;
    }

    /**
     * 获取WiFi加密方式
     *
     * @param scanResult    WiFi扫描结果
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
     * @return 当前WiFi信息（null=出现异常）
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
     * 打开或者关闭WiFi
     * <p>
     *     当WiFi处于打开时，则关闭WiFi；
     *     当WiFi处于关闭时，则打开WiFi。
     * </p>
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
     * 查找系统是否存在此WiFi扫描信息，存在就返回系统的相同名称WiFi的networkId
     *
     * @param scanResult    WiFi扫描的结果
     * @return              （-1=不存在）返回系统的相同名称WiFi的networkId
     */
    public int getNetworkIdFromScanResult(ScanResult scanResult) {
        String ssid = String.format("\"%s\"", scanResult.SSID);
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(ssid)) {
                Log.v(TAG, ssid + "对应的网络ID=" + existingConfig.networkId);
                return existingConfig.networkId;
            }
        }
        return -1;
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
     * 根据WiFi的SSID获取系统保存的配置信息
     *
     * @param ssid  WiFi名称
     * @return      配置信息
     */
    public WifiConfiguration getConfigBySsid(String ssid) {
        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : configuredNetworks) {
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
     * 连接指定WiFi（带密码）
     *
     * @param scanResult    需要连接的WiFi扫描信息
     * @param pwd           密码
     * @param listener      连接的监听器
     */
    @Deprecated
    public void connectionWifiByPassword(@NonNull ScanResult scanResult, @Nullable String pwd, @NonNull WifiStateListener listener) {
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
     * 通过networkId连接到指定WiFi（配置过的网络可以直接获取到NetworkID，不用再输入密码）
     *
     * @param SSID      WiFi名称
     * @param networkId 网络ID（失败返回-1）
     * @param listener  连接状态监听器
     */
    @Deprecated
    public void connectionWifiByNetworkId(@NonNull String SSID, int networkId, @NonNull WifiStateListener listener) {
        // 连接的回调监听
        mWifiStateListener = listener;
        /*
         * 判断 NetworkId 是否有效
         * -1 表示配置参数不正确，我们获取不到会返回-1.
         */
        if (-1 == networkId) {
            // 连接WiFi失败
            if (null != mWifiStateListener) {
                // 失败回调
                mWifiStateListener.onCurrentState(WifiStateListener.State.FAILED);
            }
            return;
        }
        // 获取当前的网络连接
        WifiInfo wifiInfo = getCurrentInfo();

        if (null != wifiInfo) {
            // 断开当前连接（先断开当前再连接指定）
            boolean isDisconnect = disconnect(wifiInfo.getNetworkId());
            if (!isDisconnect) {
                // 断开WiFi失败
                if (null != mWifiStateListener) {
                    // 失败回调
                    mWifiStateListener.onCurrentState(WifiStateListener.State.FAILED);
                }
                return;
            }
        }

        // 连接WIFI
        boolean isEnable = mWifiManager.enableNetwork(networkId, true);
        if (!isEnable) {
            // 连接WiFi失败
            if (null != mWifiStateListener) {
                // 失败回调
                mWifiStateListener.onCurrentState(WifiStateListener.State.FAILED);
            }
        }
    }

    /**
     * 根据ID忘记（移除）WiFi
     *
     * @param networkId WiFi网络ID
     * @param action  忘记成功、失败的代理监听器（ActionListener）
     *
     * @see ProxyForgetListener 只有本APP输入密码的WiFi才能成功忘记
     * <p> 参考文章
     *                          https://www.jianshu.com/p/9b5ecfb4ca63
     *                          https://blog.csdn.net/leslietuang/article/details/51203692
     * </p>
     */
    public void forget(int networkId, @NonNull ProxyForgetListener action, @NonNull WifiStateListener state) {
        if (-1 == networkId) {
            return;
        }
        action.setmWifiStateListener(state);

        try {
            //返回此 Object 的运行时类
            Class<? extends WifiManager> mWifiManagerClazz = mWifiManager.getClass();
            //Class<?> mWifiManagerClazz = Class.forName("android.net.wifi.WifiManager");//等同上一句
            //HHDLog.e("【测试】 反射的类名称（WifiManager）=" + mWifiManagerClazz.getName());
            //返回与带有给定字符串名的类或接口相关联的 Class 对象（参数：所需类的完全限定名）
            Class<?> mActionListenerClazz = Class.forName("android.net.wifi.WifiManager$ActionListener");
            //HHDLog.e("【测试】 反射的内部接口名称（ActionListener）=" + mActionListenerClazz.getName());
            //InterfaceProxy_ mInterfaceProxy = new InterfaceProxy_();
            //返回一个指定接口的代理类实例（参数：定义代理类的类加载器；代理类要实现的接口列表；指派方法调用的调用处理程序）
            Object actionListenerObject = Proxy.newProxyInstance(WifiController.class.getClassLoader(), new Class[]{mActionListenerClazz}, action);
            //返回一个 Method 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明方法（参数：方法名；参数数组）
            Method mForget = mWifiManagerClazz.getDeclaredMethod("forget", int.class, mActionListenerClazz);
            //Method mForget = mWifiManager.getDeclaredMethod("forget", new Class[]{int.class, mActionListener});//等同上一句
            HHDLog.e("【测试】 反射创建方法名称（forget）=" + mForget);
            mForget.invoke(mWifiManager, new Object[]{networkId, actionListenerObject});
            //mForget.invoke(mWifiManager.newInstance(), new Object[]{networkId, actionListenerObject});//等同上一句
            HHDLog.e("【测试】 反射调用方法名称（forget）参数=" + networkId + "，" + actionListenerObject);
        } catch (ClassNotFoundException e) {
            HHDLog.e("【测试】 反射出错（无法定位该类）=" + e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            HHDLog.e("【测试】 反射出错（找不到匹配的方法）=" + e);
            e.printStackTrace();
        } catch (SecurityException e) {
            HHDLog.e("【测试】 反射出错（存在安全侵犯）=" + e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            HHDLog.e("【测试】 反射出错（方法无法访问）=" + e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            HHDLog.e("【测试】 反射出错（参数不正确）=" + e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            HHDLog.e("【测试】 反射出错（构造出错）=" + e);
            e.printStackTrace();
        } /*catch (InstantiationException e) {
            HHDLog.e("【测试】 反射出错（创建实例出错）=" + e);
            e.printStackTrace();
        }*/

    }

    /**
     * 根据ID忘记（移除）WiFi
     *
     * @param networkId WiFi网络ID
     * @param listener  忘记成功、失败的代理监听器（ActionListener）
     */
    @Deprecated
    public void forget(int networkId, ProxyInterface listener) {
        if (-1 == networkId){return;}

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
            forget.invoke(mWifiManager, networkId, listener);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "e == " + e);
        }
        //如果是直接放在系统中编译的话，那么直接是调用wifiManager里的forget方法即可
    }

}
