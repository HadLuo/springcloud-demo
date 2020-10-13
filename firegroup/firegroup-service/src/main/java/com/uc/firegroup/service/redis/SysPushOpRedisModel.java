package com.uc.firegroup.service.redis;

import com.uc.firegroup.api.Constant;
import com.uc.framework.redis.RedisHandler;

/***
 * 系统暂停,恢复 剧本 群维度推送
 * 
 * @author 皮吉
 *
 */
public class SysPushOpRedisModel {

    private static String key(String groupId, Integer playId) {
        return "push.sys.op." + groupId + "." + playId;
    }

    public static void pause(String groupId, Integer playId) {
        System.err.println("系统暂停>>groupId=" + groupId + ",playId=" + playId);
        RedisHandler.setExpire(key(groupId, playId), "0", Constant.RedisExpireTime);
    }

    public static void resume(String groupId, Integer playId) {
        System.err.println("系统暂停恢复>>groupId=" + groupId + ",playId=" + playId);
        RedisHandler.setExpire(key(groupId, playId), "1", Constant.RedisExpireTime);
    }

    public static boolean isPause(String groupId, Integer playId) {
        return "0".equals(RedisHandler.get(key(groupId, playId)));
    }
}
