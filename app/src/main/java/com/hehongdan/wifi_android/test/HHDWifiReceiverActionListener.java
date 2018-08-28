package com.hehongdan.wifi_android.test;

/**
 * 类描述：
 *
 * @author hehongdan
 * @version v2018/8/27
 * @date 2018/8/27
 */

public interface HHDWifiReceiverActionListener {

    /**
     * WiFi的状态
     */
    public static enum State {
        /** 未知 */
        UNKNOWN,
        /** 关闭 */
        CLOSED,
        /** 打开 */
        OPENED,
        /** 已经连接 */
        CONNECTED,
        /** 当前建立数据连接。*/
        CONNECTING,
        /** 断开中 */
        DISCONNECTING,
        /** 已经断开 */
        DISCONNECTED,
        /** IP流量被暂停 */
        SUSPENDED,
        /** 尝试连接失败 */
        FAILED,
        /** 已经扫描 */
        SCAN_RESULT,
        /** 已经扫描 */
        WIFI_STATE_ENABLED,

        /** 准备开始数据连接设置 */
         DLE,
         /** 搜索一个可用的访问点 */
         SCANNING,
         /** 网络连接建立，执行身份验证 */
         AUTHENTICATING,
         /** 等待DHCP服务器的响应，以便分配IP地址信息 */
         OBTAINING_IPADDR,
         /** 对这个网络的访问被阻塞 */
         BLOCKED,
         /** 链接的连通性很差 */
         VERIFYING_POOR_LINK,
         /** 检查网络是否是一个被捕获的门户 */
         CAPTIVE_PORTAL_CHECK;

        private State() {
        }
    }

    /**
     * WiFi当前状态
     *
     * @param state
     */
    void onCurrentState(State state);





    /**
     * 已经关闭
     */
    //void onClosed();

    /**
     * 已经打开
     */
    //void onOpened();



    /**
     * 已经切换（连接一致）
     */
    //void onSwitched();


    //********************************
    /**
     * 连接中
     *
     * @param wifiInfo
     */
    //void onConnecting(WifiInfo wifiInfo);



    /**
     * 关闭中
     */
    //void onCloseing();

    /**
     * 已经打开
     */
    //void onOpening();

    /**
     * 已经扫描
     */
    //void onScaning();

    /**
     * 已经切换（连接一致）
     */
    //void onSwitching();
    //********************************

    /**
     * 验证中
     */
    //void onAuthenticating();

    /**
     * 获取IP中
     */
   //void onObtainingIpaddr();





}
