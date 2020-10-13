package com.uc.firegroup.service.redis;

import com.uc.firegroup.api.Constant;
import com.uc.framework.redis.RedisHandler;

/**
 * 
 * title: 关键词一天只能推送一次 拦截
 *
 * @author HadLuo
 * @date 2020-9-17 17:56:32
 */
public class PushRepeatRedisModel {

    public static final String getKey(String groupId, Integer playId) {
        return "keywords.oneday." + groupId + "." + playId;
    }

    public static boolean isStop(String groupId, Integer playId) {
        String ret = RedisHandler.get(getKey(groupId, playId));
        if ("1".equals(ret)) {
            return true;
        }
        return false;
    }

    public static void setStop(String groupId, Integer playId) {
        RedisHandler.setExpire(getKey(groupId, playId), "1", Constant.RedisExpireTime);
    }

}
