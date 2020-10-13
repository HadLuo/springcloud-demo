package com.uc.firegroup.service.redis;

import org.springframework.util.StringUtils;

import com.uc.framework.redis.RedisHandler;

/***
 * 
 * title: 群 维度 发送 计数器
 *
 * @author HadLuo
 * @date 2020-9-25 9:19:15
 */
public class GroupPushCounter {

    private static String key(int playId, String groupId) {
        return "grouppush.count." + playId + "." + groupId;
    }

    public static int get(int playId, String groupId) {
        String count = RedisHandler.get(key(playId, groupId));
        if (StringUtils.isEmpty(count)) {
            return 0;
        }
        return Integer.parseInt(count);
    }

    public static void incr(int playId, String groupId) {
        int c = get(playId, groupId);
        c = c + 1;
        RedisHandler.set(key(playId, groupId), c);
    }
}
