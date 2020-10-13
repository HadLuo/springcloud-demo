package com.uc.framework.redis.lock;

import java.util.Collections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.uc.framework.redis.RedisHandler;
import com.uc.framework.resources.FileLoads;

/**
 * title: lua 脚本实现 分布式锁
 *
 * @author HadLuo
 * @date 2020-9-10 10:56:40
 */
@Component
public class LuaRedisLock implements RedisLock, InitializingBean {

    private String lockScript;
    private String unLockScript;

    private static final String LockPrefix = "Lock|";

    @Override
    public void afterPropertiesSet() throws Exception {
        // 加载 lua
        this.lockScript = FileLoads.loadString("lock.lua");
        this.unLockScript = FileLoads.loadString("unlock.lua");
        if (StringUtils.isEmpty(this.lockScript)) {
            throw new RuntimeException("lock lua脚本为空");
        }
        if (StringUtils.isEmpty(this.unLockScript)) {
            throw new RuntimeException("unlock lua脚本为空");
        }
    }

    private String getKey(String key) {
        return LockPrefix + key;
    }

    @Override
    public boolean lock(String lockKey, String releaseFlag, int expireSecond) {
        if (StringUtils.isEmpty(lockKey)) {
            return true;
        }
        Long ret = RedisHandler.execLua(Long.class, lockScript, Collections.singletonList(getKey(lockKey)),
                releaseFlag, expireSecond + "");
        if (ret == 1) {
            // 拿锁成功
            return true;
        }
        return false;
    }

    @Override
    public void unlock(String key, String releaseFlag) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        Long ret = RedisHandler.execLua(Long.class, unLockScript, Collections.singletonList(getKey(key)),
                releaseFlag);
        if (ret == 1) {
            return;
        }

    }

}
