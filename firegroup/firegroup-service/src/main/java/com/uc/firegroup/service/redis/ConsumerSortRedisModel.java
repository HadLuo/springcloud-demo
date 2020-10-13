package com.uc.firegroup.service.redis;

import org.springframework.util.StringUtils;
import com.uc.framework.redis.RedisHandler;

/***
 * 记录 群剧本 到哪个 发言的位置了
 * 
 * @author 皮吉
 *
 */
public class ConsumerSortRedisModel {

    private static String key(Integer playId) {
        return "ConsumerSort." + playId;
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
    public static void setSort(Integer playId, int sort) {
        RedisHandler.set(key(playId), sort + "");
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
    public static int getSort(Integer playId) {
        String sort = RedisHandler.get(key(playId));
        if (StringUtils.isEmpty(sort)) {
            return 0;
        }
        return Integer.parseInt(sort);
    }
}
