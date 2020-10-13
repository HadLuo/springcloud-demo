package com.uc.firegroup.service.redis;

import org.springframework.util.StringUtils;

import com.uc.firegroup.api.Constant;
import com.uc.framework.redis.RedisHandler;

/***
 * 记录 群剧本 到哪个 发言的位置了
 * 
 * @author 皮吉
 *
 */
public class SpeakedSortRedisModel {

    private static String key(String groupId, Integer playId) {
        return "push.speak." + groupId + "." + playId;
    }

    /**
     * 
     * title: 第几号已经发言了
     *
     * @param groupId
     * @param playId
     * @param robotWxId
     * @author HadLuo 2020-9-23 11:02:57
     */
    public static void speak(String groupId, Integer playId, int curerntSort) {
        RedisHandler.setExpire(key(groupId, playId), curerntSort + "", Constant.RedisExpireTime);
    }

    /***
     * 
     * title: 获取当前 发言人 顺序
     *
     * @param groupId
     * @param playId
     * @param robotWxId
     * @return
     * @author HadLuo 2020-9-23 11:02:50
     */
    public static int getSort(String groupId, Integer playId) {
        String sort = RedisHandler.get(key(groupId, playId));
        if (StringUtils.isEmpty(sort)) {
            return -1;
        }
        return Integer.parseInt(sort);
    }
}
