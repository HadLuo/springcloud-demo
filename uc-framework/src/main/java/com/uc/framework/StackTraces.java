package com.uc.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import org.springframework.util.StringUtils;

/***
 * 堆栈工具
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2019年3月23日 新建
 */
public class StackTraces {

    /**
     * 解决反射invoke method时 吞并的真实异常
     * 
     * @param wrapped
     * @return
     * @author HadLuo 2020年4月20日 新建
     */
    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /***
     * 将异常所有堆栈信息 转换成 String
     * 
     * @param e
     * @return
     * @author HadLuo 2019年3月23日 新建
     */
    public static String convertString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        if (!StringUtils.isEmpty(s)) {
            if (s.length() > 10000) {
                return s.substring(0, 9999);
            }
        }
        return s;
    }

    public static String convertString(Throwable e, int len) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        try {
            if (!StringUtils.isEmpty(s)) {
                if (s.length() > len) {
                    return s.substring(0, len - 1);
                }
            }
        } catch (Exception e2) {
            // TODO: handle exception
        }
        return s;
    }

    /***
     * 获取调用 者 的 类名和方法名
     * 
     * @return
     * @author HadLuo 2019年6月28日 新建
     */
    public static String getCallerInfo(String... filterMethodNames) {
        StackTraceElement[] stacks = (new Throwable()).getStackTrace();
        for (StackTraceElement s : stacks) {
            if (s.getMethodName().trim().contains("getCallerInfo")) {
                // 方法自己，过滤
                continue;
            }
            if (filterMethodNames != null) {
                boolean isFilter = false;
                for (String filter : filterMethodNames) {
                    if (s.getMethodName().trim().contains(filter)) {
                        isFilter = true;
                        break;
                    }
                }
                if (isFilter) {
                    continue;
                }
            }
            if (!StringUtils.isEmpty(s.getClassName()) && !StringUtils.isEmpty(s.getMethodName())) {
                return s.getClassName() + "." + s.getMethodName();
            }
        }
        return "";
    }
}
