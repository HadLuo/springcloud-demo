package com.uc.firegroup.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.uc.framework.App;

/**
 * 
 * title: 程序环境变量 nacos dataId： firegroup-env.properties
 *
 * @author HadLuo
 * @date 2020-9-19 16:52:20
 */
@Controller
@RefreshScope
public class Env {

    /** 商户标识 */
    @Value(value = "${identity}")
    private String identity;
    /** 商户 id */
    @Value(value = "${merchatId}")
    private String merchatId;

    /** 程序环境 ： dev 开发 ，test测试 ， idc线上 */
    @Value(value = "${env}")
    private String env;
    /** 关键词 多少s 推送一次 , 默认1天 */
    @Value(value = "${KeyWordsPushDelay}")
    private Integer KeyWordsPushDelay = 60 * 60 * 24;

    /**
     * 
     * title: 判断 是否是 线上环境
     *
     * @return
     * @author HadLuo 2020-9-19 16:57:46
     */
    public static boolean idc() {
        String env = App.getBean(Env.class).getEnv();
        if (StringUtils.isEmpty(env)) {
            return false;
        }
        if ("idc".equalsIgnoreCase(env.trim())) {
            return true;
        }
        return false;
    }

    public static String env() {
        return App.getBean(Env.class).getEnv();
    }

    public static String identity() {
        return App.getBean(Env.class).getIdentity();
    }

    public static Integer keyWordsPushDelay() {
        return App.getBean(Env.class).getKeyWordsPushDelay();
    }

    public static String merchatId() {
        return App.getBean(Env.class).getMerchatId();
    }

    public String getIdentity() {
        return identity;
    }

    public String getMerchatId() {
        return merchatId;
    }

    public String getEnv() {
        return env;
    }

    public Integer getKeyWordsPushDelay() {
        return KeyWordsPushDelay;
    }

}
