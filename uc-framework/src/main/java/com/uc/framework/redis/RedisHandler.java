package com.uc.framework.redis;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.framework.App;
import com.uc.framework.Times;

/**
 * redis 操作
 * 
 * redisTemplate.opsForValue(); //�����ַ��� <br>
 * redisTemplate.opsForHash(); //����hash<br>
 * redisTemplate.opsForList(); //����list<br>
 * redisTemplate.opsForSet(); //����set<br>
 * redisTemplate.opsForZSet(); //��������set<br>
 * 
 * @author HadLuo
 * @date 2020-9-2 11:22:21
 */
public class RedisHandler {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * get RedisTemplate����
     * 
     * 
     * @return
     * @author HadLuo 2020-9-2 11:42:22
     */
    public static RedisTemplate<String, Object> getRedisTemplate() {
        return App.getBean(RedisHandler.class).redisTemplate;
    }

    /**
     * title: 执行lua 脚本
     *
     * @param <T>
     * @param returnType lua脚本返回值类型
     * @param lua 脚本字符串
     * @param keys 所有的 key
     * @param args 参数值
     * @return
     * @author HadLuo 2020-9-10 11:51:27
     */
    public static <T> T execLua(Class<T> returnType, String lua, List<String> keys, String... args) {
        Object[] argsA = args;
        return getRedisTemplate().execute(new DefaultRedisScript<>(lua, returnType), keys, argsA);
    }

    /**
     * set with expire time
     * 
     * 
     * @param key
     * @param value
     * @param time 单位 s
     * @author HadLuo 2020-9-2 11:42:33
     */
    public static void setExpire(final String key, final String value, final long time) {
        getRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * string set
     * 
     * 
     * @param key
     * @param value
     * @author HadLuo 2020-9-2 11:43:11
     */
    public static void set(final String key, final Object value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        if (value instanceof String) {
            getRedisTemplate().opsForValue().set(key, value);
        } else {
            getRedisTemplate().opsForValue().set(key, JSON.toJSONString(value));
        }
    }

    /**
     * string get
     * 
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:18
     * @param <T>
     */
    public static <T> T get(final String key, Class<T> clazz) {
        if (StringUtils.isEmpty(key) || clazz == null) {
            return null;
        }
        Object object = getRedisTemplate().opsForValue().get(key);
        if (object == null) {
            return null;
        }
        return JSON.parseObject((String) object, clazz);
    }

    /**
     * string set
     * 
     * 
     * @param key
     * @param value
     * @author HadLuo 2020-9-2 11:43:11
     */
    public static void set(final String key, final String value) {
        getRedisTemplate().opsForValue().set(key, value);
    }

    /**
     * string get
     * 
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:18
     */
    public static String get(final String key) {
        return (String) getRedisTemplate().opsForValue().get(key);
    }

    /**
     * del key
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:32
     */
    public static Boolean del(final String key) {
        return getRedisTemplate().delete(key);
    }
    
    /**
     * title : 指定key的过期时间，单位s
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:32
     */
    public static Boolean expire(String key ,  int second) {
        return getRedisTemplate().expire(key, second, TimeUnit.SECONDS);
    }
    
    /**
     * title : 指定key在哪个日期过期
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:32
     */
    public static Boolean expireAt(String key ,  Date date) {
        return getRedisTemplate().expireAt(key, date);
    }
    
    
    /**
     * title : 指定key在当前时间多少天后过期
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:43:32
     */
    public static Boolean expireAtDay(String key ,  int day) {
        return getRedisTemplate().expireAt(key, Times.getDayReturnDate(new Date(),day));
    }

    /**
     * incr
     * 
     * @param key
     * @author HadLuo 2020-9-2 11:43:54
     */
    public static void incr(final String key) {
        getRedisTemplate().opsForValue().increment(key);
    }

    /**
     * incr
     * 
     * @param key
     * @param delta
     * @author HadLuo 2020-9-2 11:44:02
     */
    public static void incr(final String key, long delta) {
        getRedisTemplate().opsForValue().increment(key, delta);
    }

    /**
     * hset ����
     * 
     * @param key
     * @param hashKey
     * @param hashValue
     * @author HadLuo 2020-9-2 11:44:10
     */
    public static void hset(String key, String hashKey, String hashValue) {
        getRedisTemplate().opsForHash().put(key, hashKey, hashValue);
    }

    /**
     * hget����
     * 
     * @param key
     * @param hashKey
     * @return
     * @author HadLuo 2020-9-2 11:44:24
     */
    public static String hget(String key, String hashKey) {
        return (String) getRedisTemplate().opsForHash().get(key, hashKey);
    }

    /**
     * delete by key
     * 
     * @param key
     * @param hashKeys
     * @author HadLuo 2020-9-2 11:44:36
     */
    public static void hdel(String key, Object... hashKeys) {
        getRedisTemplate().opsForHash().delete(key, hashKeys);
    }

    /**
     * exists key
     * 
     * @param key
     * @return
     * @author HadLuo 2020-9-2 11:49:45
     */
    public static boolean exists(final String key) {
        return getRedisTemplate().hasKey(key);
    }

    /**
     * 
     * title: list 右边添加元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     */
    public static void rpush(String key, Object value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        if (value instanceof String) {
            getRedisTemplate().opsForList().rightPush(key, value);
        } else {
            getRedisTemplate().opsForList().rightPush(key, JSON.toJSONString(value));
        }
    }

    /**
     * 
     * title: list 左边添加元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     */
    public static void lpush(String key, Object value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        if (value instanceof String) {
            getRedisTemplate().opsForList().leftPush(key, value);
        } else {
            getRedisTemplate().opsForList().leftPush(key, JSON.toJSONString(value));
        }
    }

    /**
     * 
     * title: list 左边弹出元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static String lpop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return (String) getRedisTemplate().opsForList().leftPop(key);
    }

    /**
     * 
     * title: list 左边弹出元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static String rpop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return (String) getRedisTemplate().opsForList().rightPop(key);
    }

    /**
     * 
     * title: list 左边弹出元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static <T> T lpop(String key, Class<T> clazz) {
        if (clazz == null || StringUtils.isEmpty(key)) {
            return null;
        }
        Object object = getRedisTemplate().opsForList().leftPop(key);
        if (object == null) {
            return null;
        }
        String json = (String) object;
        return JSON.parseObject(json, clazz);
    }

    /**
     * 
     * title: list 右边弹出元素
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static <T> T rpop(String key, Class<T> clazz) {
        if (clazz == null || StringUtils.isEmpty(key)) {
            return null;
        }
        Object object = getRedisTemplate().opsForList().rightPop(key);
        if (object == null) {
            return null;
        }
        String json = (String) object;
        return JSON.parseObject(json, clazz);
    }

    /**
     * 
     * title: list 队列大小
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static Long llen(String key) {
        if (StringUtils.isEmpty(key)) {
            return 0L;
        }
        return getRedisTemplate().opsForList().size(key);
    }

    /**
     * 
     * title: list遍历
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        if (StringUtils.isEmpty(key)) {
            Collections.emptyList();
        }
        List<Object> datas = getRedisTemplate().opsForList().range(key, start, end);
        if (CollectionUtils.isEmpty(datas)) {
            Collections.emptyList();
        }
        List<T> results = Lists.newArrayList();
        for (Object obj : datas) {
            if (obj instanceof String) {
                results.add(JSON.parseObject((String) obj, clazz));
            } else {
                results.add((T) obj);
            }
        }
        return results;
    }

    /**
     * 
     * title: list遍历
     *
     * @param key
     * @param value
     * @author HadLuo 2020-9-16 9:18:07
     * @param <T>
     */
    public static List<String> lrange(String key, int start, int end) {
        if (StringUtils.isEmpty(key)) {
            Collections.emptyList();
        }
        List<Object> datas = getRedisTemplate().opsForList().range(key, start, end);
        if (CollectionUtils.isEmpty(datas)) {
            Collections.emptyList();
        }
        List<String> results = Lists.newArrayList();
        for (Object obj : datas) {
            results.add((String) obj);
        }
        return results;
    }

    /**
     * 
     * title: zset zrange
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @author HadLuo 2020-9-16 9:49:00
     */
    public static Set<String> zrange(String key, long start, long end) {
        Set<Object> sets = getRedisTemplate().opsForZSet().range(key, start, end);
        if (CollectionUtils.isEmpty(sets)) {
            return Collections.emptySet();
        }
        Set<String> ret = new HashSet<String>();
        for (Object obj : sets) {
            ret.add((String) obj);
        }
        return ret;
    }

    /**
     * 
     * title: zset zrange
     *
     * @param key
     * @param start
     * @param end
     * @return
     * @author HadLuo 2020-9-16 9:49:00
     * @param <T>
     */
    public static <T> Set<T> zrange(String key, long start, long end, Class<T> clazz) {
        Set<Object> sets = getRedisTemplate().opsForZSet().range(key, start, end);
        if (CollectionUtils.isEmpty(sets)) {
            return Collections.emptySet();
        }
        Set<T> ret = new HashSet<T>();
        for (Object obj : sets) {
            ret.add(JSON.parseObject((String) obj, clazz));
        }
        return ret;
    }
}
