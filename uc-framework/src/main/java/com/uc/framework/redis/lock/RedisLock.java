package com.uc.framework.redis.lock;

/***
 * title: redis 分布式锁
 *
 * @author HadLuo
 * @date 2020-9-10 10:49:43
 */
public interface RedisLock {

    /**
     * 
     * title: 锁方法,锁成功会返回true
     *
     * @param key redis key
     * @param releaseFlag 释放锁 的标识（必须唯一）
     * @param expireSecond 锁的时间 ， 单位s
     * @return true-返回拿锁成功 ， false-拿锁失败
     * @author HadLuo 2020-9-11 10:16:58
     */
    public boolean lock(String key, String releaseFlag, int expireSecond);

    /**
     * 
     * title: 解锁
     *
     * @param key
     * @param releaseFlag
     * @author HadLuo 2020-9-11 10:18:35
     */
    public void unlock(String key, String releaseFlag);

}
