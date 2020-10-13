package com.uc.framework.chat.store;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.uc.framework.chat.Chat;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.RedisHandler;

/***
 * title: 发言人聊天包存储
 * 
 * @author 皮吉
 *
 */
public class ChatHahStore extends GetKey {

    public ChatHahStore(ChatStore store) {
        super(store);
    }

    public void set(String alias, String groupUuid, int sort, Chat chat) {
        String k = getKey(getClass(), alias, groupUuid);
        getStore().hput(k, sort + "", JSON.toJSONString(chat));
        Logs.e(getClass(), "[chat store]>>key=" + k + ",groupUuid=" + groupUuid + ",sort=" + sort + ",chat="
                + JSON.toJSONString(chat));
    }

    public Chat get(String alias, String groupUuid, int sort) {
        String str = getStore().hget(getKey(getClass(), alias, groupUuid), sort + "");
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return JSON.parseObject(str, Chat.class);
    }

    @Override
    public void del(String alias, String groupUuid) {
        String k = getKey(getClass(), alias, groupUuid);
        RedisHandler.del(k);
        Logs.e(getClass(), "[ChatHahStore del]>>key=" + k);
    }

}
