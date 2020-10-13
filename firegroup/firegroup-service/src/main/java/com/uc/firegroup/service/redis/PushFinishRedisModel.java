package com.uc.firegroup.service.redis;

import com.uc.framework.redis.RedisHandler;

/***
 * 
 * title: 发言人消息 是否 真正成功
 *
 * @author HadLuo
 * @date 2020-9-26 15:57:01
 */
public class PushFinishRedisModel {

    private static String key(int playId, String groupId, String nickName) {
        return "pushfinish." + playId + "." + groupId + "." + nickName;
    }

    public static void setStart(int playId, String groupId, String nickName) {
        RedisHandler.set(key(playId, groupId, nickName), "1");
    }

    public static void setFinish(int playId, String groupId, String nickName) {
        RedisHandler.set(key(playId, groupId, nickName), "2");
    }

    public static boolean isFinish(int playId, String groupId, String nickName) {
        return "2".equals(RedisHandler.get(key(playId, groupId, nickName)));
    }

}
