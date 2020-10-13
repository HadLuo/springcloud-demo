package com.uc.framework.logger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;

/**
 * title ：日志组件
 * 
 * @author HadLuo
 * @date 2020-9-2 15:58:04
 */
public class Logs {

    private static final Console console = new Console();

    /** idc 生产 dev 开发 test测试 */
    private static String env;

    public static void registerEnv(String env) {
        if (StringUtils.isEmpty(env)) {
            throw new RuntimeException("环境变量错误");
        }
        if (!env.equalsIgnoreCase("test") && !env.equalsIgnoreCase("dev") && !env.equalsIgnoreCase("idc")) {
            throw new RuntimeException("环境变量错误，只能是 test,dev,idc");
        }
        Logs.env = env;
    }

    public static String envAlias() {
        if (isDev()) {
            return "开发环境";
        }
        if (isIdc()) {
            return "生产环境";
        }
        if (isTest()) {
            return "测试环境";
        }
        return null;
    }

    /**
     * 
     * title: true-开发环境
     *
     * @return
     * @author HadLuo 2020-9-22 17:01:17
     */
    public static boolean isDev() {
        if (env.equalsIgnoreCase("dev")) {
            return true;
        }
        return false;
    }

    /**
     * 
     * title: true-生产环境
     *
     * @return
     * @author HadLuo 2020-9-22 17:01:08
     */
    public static boolean isIdc() {
        if (env.equalsIgnoreCase("idc")) {
            return true;
        }
        return false;
    }

    /**
     * 
     * title: true-测试环境
     *
     * @return
     * @author HadLuo 2020-9-22 17:00:58
     */
    public static boolean isTest() {
        if (env.equalsIgnoreCase("test")) {
            return true;
        }
        return false;
    }

    /***
     * 
     * title: 不打印 到生产环境， 只打印到测试和开发
     *
     * @param msg
     * @author HadLuo 2020-9-22 16:13:31
     */
    public static void debug(String msg) {
        if (!isIdc()) {
            System.err.println(msg);
        }
    }

    /**
     * 
     * title: 输出到 es
     *
     * @param clazz
     * @param msg
     * @param e
     * @author HadLuo 2020-9-12 15:21:12
     */
    public static void e(Class<?> clazz, String msg) {
        LoggerFactory.getLogger(clazz).info(msg);
//        LoggerFactory.getLogger(clazz).error(msg);
    }

    /**
     * 
     * title: 不会 输出到es ，只会输出到 console
     *
     * @return
     * @author HadLuo 2020-9-16 14:31:02
     */
    public static Console console() {
        return console;
    }

    /**
     * 
     * title: 输出到 es
     *
     * @param clazz
     * @param msg
     * @param e
     * @author HadLuo 2020-9-12 15:21:12
     */
    public static void e(Class<?> clazz, String msg, Throwable e) {
        LoggerFactory.getLogger(clazz).info(msg, e);
    }

    /**
     * 
     * title: 只打印到控制台 ， 不输出到es
     *
     * @author HadLuo
     * @date 2020-9-12 15:20:52
     */
    public static class Console {

        public void i(Class<?> clazz, String msg) {
            LoggerFactory.getLogger(clazz).info(msg);
        }

        public void i(Class<?> clazz, String msg, Throwable e) {
            LoggerFactory.getLogger(clazz).info(msg, e);
        }

    }

    /***
     * 
     * title: 后台 admin 操作 日志
     *
     * @param opration
     * @param request
     * @param response
     * @author HadLuo 2020-9-22 11:27:45
     */
    public static void admin(String opration, Object request, Object response) {
        if (StringUtils.isEmpty(opration)) {
            return;
        }
        String req = "", res = "", person = "";
        try {
            req = JSON.toJSONString(request);
        } catch (Exception e) {
        }
        try {
            res = JSON.toJSONString(response);
        } catch (Exception e) {
        }
        try {
            User user = UserThreadLocal.get();
            person = JSON.toJSONString(user);
        } catch (Exception e) {
        }
        Logs.e(Logs.class, "[admin:" + opration + "]>>user=" + person + ",req=" + req + ",res=" + res);
    }

    // public static void w(Class<?> clazz, String msg) {
    // LoggerFactory.getLogger(clazz).warn(msg);
    // }
    //
    // public static void w(Class<?> clazz, String msg, Throwable e) {
    // LoggerFactory.getLogger(clazz).warn(msg, e);
    // }

}
