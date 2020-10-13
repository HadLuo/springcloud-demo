package com.uc.framework.redis;

import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.uc.framework.logger.Logs;

/***
 * 相当于Diamond专门用来作动态配置，使用redis实现
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2018年9月19日 新建
 */
public class RedisProperties {

    /***
     * 设置 值到 redis
     * 
     * @param key
     * @param value
     * @author HadLuo 2019年3月15日 新建
     */
    public static void set(String key, String value) {
        RedisHandler.set(key, value);
    }

    /***
     * 设置值到redis
     * 
     * @param key
     * @param value
     * @author HadLuo 2019年3月15日 新建
     */
    public static void set(String key, Integer value) {
        if (value == null) {
            return;
        }
        RedisHandler.set(key, value.toString());
    }

    /***
     * 设置值到redis
     * 
     * @param key
     * @param value
     * @author HadLuo 2019年3月15日 新建
     */
    public static void set(String key, Long value) {
        if (value == null) {
            return;
        }
        RedisHandler.set(key, value.toString());
    }

    /***
     * 设置值到redis
     * 
     * @param key
     * @param value
     * @author HadLuo 2019年3月15日 新建
     */
    public static void set(String key, Double value) {
        if (value == null) {
            return;
        }
        RedisHandler.set(key, value.toString());
    }

    /***
     * 设置值到redis
     * 
     * @param key
     * @param value
     * @author HadLuo 2019年3月15日 新建
     */
    public static void setObject(String key, Object object) {
        if (object == null) {
            return;
        }
        RedisHandler.set(key, JSON.toJSONString(object));
    }

    /***
     * 从redis取值
     * 
     * @param key
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static String get(String key) {
        return (String) RedisHandler.get(key);
    }

    /**
     * 从redis取值 ，不存在 返回默认值
     * 
     * @param key
     * @param defaultValue
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static String getString(String key, String defaultValue) {
        String val;
        try {
            val = (String) RedisHandler.get(key);
        } catch (Exception e) {
            Logs.e(RedisProperties.class, "redis get error>>key=" + key, e);
            return defaultValue;
        }
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    /**
     * 从redis取值 ，不存在 返回默认值
     * 
     * @param key
     * @param defaultValue
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static int getInt(String key, int defaultValue) {
        String val;
        try {
            val = (String) RedisHandler.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        }
        return Integer.parseInt(val.trim());
    }

    /***
     * 从redis取值 ，不存在 返回默认值
     * 
     * @param key
     * @param defaultValue
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static long getLong(String key, long defaultValue) {
        String val = (String) RedisHandler.get(key);
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        }
        return Long.parseLong(val);
    }

    /***
     * 从redis取值 ，不存在 返回默认值
     * 
     * @param key
     * @param defaultValue
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static double getDouble(String key, double defaultValue) {
        String val = (String) RedisHandler.get(key);
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        }
        return Double.parseDouble(val.trim());
    }

    /***
     * 从redis取值 ，不存在 返回默认值
     * 
     * @param key
     * @param clazz
     * @param defaultValue
     * @return
     * @author HadLuo 2019年3月15日 新建
     */
    public static <T> T getObject(String key, Class<T> clazz, T defaultValue) {
        String val = (String) RedisHandler.get(key);
        if (StringUtils.isEmpty(val)) {
            return defaultValue;
        }
        return JSON.parseObject(val, clazz);
    }
}
