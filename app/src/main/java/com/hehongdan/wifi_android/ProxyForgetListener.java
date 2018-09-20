package com.hehongdan.wifi_android;

import com.hehongdan.wifi_android.WifiStateListener.State;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 类描述：代理AndroidAPI的ActionListener隐藏接口（忘记（删除）WiFi）
 *
 * @author hehongdan
 * @version v2018/8/25
 * @date 2018/8/25
 */

public class ProxyForgetListener implements InvocationHandler {
    private MyLogger HHDLog = MyLogger.HHDLog();
    /** WiFi连接监听器 */
    private WifiStateListener mWifiStateListener;

    /**
     * 设置WiFi连接监听器
     * @param wifiStateListener 赋值的监听器
     */
    public void setmWifiStateListener(WifiStateListener wifiStateListener) {
        this.mWifiStateListener = wifiStateListener;
    }

    /**
     * 在代理实例上处理方法调用并返回结果
     *
     * @param proxy         （被代理的类或接口）在其上调用方法的代理实例
     * @param method        （调用了类或接口哪个方法）对应于在代理实例上调用的接口方法的 Method 实例
     * @param args          （方法的传入的参数）包含传入代理实例上方法调用的参数值的对象数组
     * @return              （方法的返回的值）从代理实例的方法调用返回的值
     * @throws Throwable    从代理实例上的方法调用抛出的异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /*HHDLog.i("【测试】 反射，监听forget（忘记）成功" + "\n"
                *//*+ "proxy=" + proxy *//*
                + "，method=" + method
                + "，args=" + args);*/

        if (null != method) {
            //actionListener(method);
            String name = method.getName();
            if ("onSuccess".equals(name)) {
                HHDLog.i("广播，忘记WiFi成功");
                mWifiStateListener.onCurrentState(State.FORGET_SUCCESS);
            } else if ("onFailure".equals(name)) {
                HHDLog.e("广播，忘记WiFi失败");
                mWifiStateListener.onCurrentState(State.FORGET_FAILURE);
            }
        } else {
            HHDLog.e("广播，忘记WiFi失败");
            mWifiStateListener.onCurrentState(State.FORGET_FAILURE);
        }
        return null;
    }
}
