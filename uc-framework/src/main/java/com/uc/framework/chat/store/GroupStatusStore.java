package com.uc.framework.chat.store;

import org.apache.commons.lang.StringUtils;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.RedisHandler;

/***
 * title : 每个群的发送 状态的存储
 * 
 * @author 皮吉
 *
 */
public class GroupStatusStore extends GetKey {

    public GroupStatusStore(ChatStore store) {
        super(store);
        // TODO Auto-generated constructor stub
    }

    public void put(String alias, String groupUuid, String groupWxId, int sort) {
        String k = getKey(getClass(), alias, groupUuid);
        getStore().hput(k, groupWxId, sort + "");
        Logs.e(getClass(), "[chat group status store]>>key=" + k + ",groupUuid=" + groupUuid + ",groupWxId="
                + groupWxId + ",sort=" + sort);
    }

    public int get(String alias, String groupUuid, String groupWxId) {
        String k = getKey(getClass(), alias, groupUuid);
        String str = getStore().hget(k, groupWxId);
        if (StringUtils.isEmpty(str)) {
            return 0;
        }
        Logs.e(getClass(), "[get chat group status]>>k=" + k + ",str=" + str);
        return Integer.parseInt(str);
    }

    public int next(String alias, String groupUuid, String groupWxId) {
        int cur = get(alias, groupUuid, groupWxId);
        int nextSort = cur + 1;
        // 把 要发的 群 顺序 设置为下一个
        put(alias, groupUuid, groupWxId, nextSort);
        return cur;
    }

    @Override
    public void del(String alias, String groupUuid) {
        String k = getKey(getClass(), alias, groupUuid);
        RedisHandler.del(k);
        Logs.e(getClass(), "[GroupStatusStore del]>>key=" + k);
    }

}
