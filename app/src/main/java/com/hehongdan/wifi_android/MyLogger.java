package com.hehongdan.wifi_android;

import android.util.Log;

import java.util.Hashtable;

/**
 * 打印日志
 *
 * The class for print log
 * @author kesenhoo
 *
 *
 * logger工具类，单例设计模式
 *
 * 定义MyLogger过程：
 * private MyLogger HHDLog = MyLogger.HHDLog();//全局定义
 * HHDLog.e("括号中间的内容");//测试代码处编写
 *
 * 实际显示效果：
 * [美易来]: @HHD@ [ main: MainActivity.java:88 onCreate ] - 括号中间的内容
 *
 * Log.e(TAG,"级别5，错误信息");//Error:FF5555//Assert:FF0000
 * Log.w(TAG,"级别4，警告信息");//Warning:BBBB55
 * Log.i(TAG,"级别3，一般信息");//Info:55BB55
 * Log.d(TAG,"级别2，调试信息");//Debug:6666FF
 * Log.v(TAG,"级别1，无用信息");//Verbose:BBBBBB
 *
 */
public class MyLogger {
    /** 标记是否打印日志 */
    private final static boolean                logFlag         = true;
    /** 日志的标记（一般为类名） */
    public final static String                  tag             = "[HHD]";
    /** 日志的级别 */
    private final static int                    logLevel        = Log.VERBOSE;
    /** 存放不同用户Log实例的容器 */
    private static Hashtable<String, MyLogger>  sLoggerTable    = new Hashtable<String, MyLogger>();
    /** 日志构造方法参数（标记哪个用户的Log） */
    private String							    mClassName;
    /** 声明一个HHDLog对象 */
    private static MyLogger                     Hlog;
    private static MyLogger                     Llog;
    /** 标记一个HHDLog对象 */
    private static final String                 LDY             = "@LDY@ ";
    private static final String                 HHD             = "@HHD@ ";
    /** 拼接符号（方法名与内容之间） */
    private static final String                 APPEND_SYMBOL   = " - ";

    /**
     * 私有的构造方法
     *
     * @param name  用户标记
     */
    private MyLogger(String name) {
        mClassName = name;
    }

    /**
     * 单例模式创建Log
     *
     * @param className 日志类名
     * @return          日志对象
     */
    @SuppressWarnings("unused")
    private static MyLogger getLogger(String className) {
        MyLogger classLogger = (MyLogger) sLoggerTable.get(className);
        if (classLogger == null) {
            classLogger = new MyLogger(className);
            sLoggerTable.put(className, classLogger);
        }
        return classLogger;
    }

    /**
     * 标记第一个用户（HHD）
     * Purpose:Mark user one
     * 给外部提供一个静态公开方法获取对象实例
     *
     * @return  HHDLog对象
     */
    public static MyLogger HHDLog() {
        if (Hlog == null) {
            Hlog = new MyLogger(HHD);
        }
        return Hlog;
    }

    /**
     * 标记第二个用户（）
     * Purpose:Mark user two
     *
     * @return  LDYLog对象
     */
    public static MyLogger LDYLog() {
        if (Llog == null) {
            Llog = new MyLogger(LDY);
        }
        return Llog;
    }

    /**
     * 获取当前（堆栈跟踪元素）的方法名
     * Get The Current Function Name
     *
     * @return  方法名（包括：@HHD@、线程名、类名、行数、方法名）
     */
    private String getFunctionName() {
        //堆栈跟踪元素数组（当前线程）
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            //堆栈跟踪元素方法是（本机方法）
            if (st.isNativeMethod()) {
                //结束单次循环
                continue;
            }
            //堆栈跟踪元素类名是（该线程的名称）
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            //堆栈跟踪元素类名是（本Log的名称）
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            //@HHD@ [ 线程:main  文件:类名.java  行数:100  方法:方法名 ]
            return mClassName + "[ 线程:" + Thread.currentThread().getName() + "  文件:" + st.getFileName() + "  行数:" + st.getLineNumber() + "  方法:" + st.getMethodName() + " ]";
        }
        return null;
    }



    /**
     * Verbose级别的日志
     * The Log Level:V
     *
     * @param str   打印的内容
     */
    public void v(Object str) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(tag, name + APPEND_SYMBOL + str);
                } else {
                    Log.v(tag, str.toString());
                }
            }
        }
    }

    /**
     * Debug级别的日志
     * The Log Level:d
     *
     * @param str   打印的内容
     */
    public void d(Object str) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(tag, name + APPEND_SYMBOL + str);
                } else {
                    Log.d(tag, str.toString());
                }
            }
        }
    }

    /**
     * Info级别的日志
     * The Log Level:i
     *
     * @param str   打印的内容
     */
    public void i(Object str) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(tag, name + APPEND_SYMBOL + str);
                } else {
                    Log.i(tag, str.toString());
                }
            }
        }
    }

    /**
     * Warn级别的日志
     * The Log Level:w
     *
     * @param str   打印的内容
     */
    public void w(Object str) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(tag, name + APPEND_SYMBOL + str);
                } else {
                    Log.w(tag, str.toString());
                }
            }
        }
    }

    /**
     * * Error级别的日志
     * The Log Level:e
     *
     * @param str   打印的内容
     */
    public void e(Object str) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(tag, name + APPEND_SYMBOL + str);
                } else {
                    Log.e(tag, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param ex
     */
    public void e(Exception ex) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                Log.e(tag, "error", ex);
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public void e(String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] " + log + "\n", tr);
        }
    }
}