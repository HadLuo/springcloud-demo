package com.uc.framework.chat.store;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.uc.framework.chat.Chat;
import com.uc.framework.logger.Logs;

/***
 * title :半消息的存储
 * 
 * @author 皮吉
 *
 */
public class HalfChatStore extends GetKey {

    public HalfChatStore(ChatStore store) {
        super(store);
        // TODO Auto-generated constructor stub
    }

    public void set(String groupWxId, String ackKey, Chat chat) {
        String k = getKey(getClass(), groupWxId, ackKey);
        getStore().set(k, JSON.toJSONString(chat));
        getStore().expire(k, 30);
        Logs.e(getClass(), "[half chat store]>>key=" + k + ",ackKey=" + ackKey + ",groupWxId=" + groupWxId
                + ",chat=" + JSON.toJSONString(chat));
    }

    public Chat get(String ackKey, String groupWxId) {
        String k = getKey(getClass(), groupWxId, ackKey);
        String str = getStore().get(k);
        Logs.e(getClass(), "[get half chat]>>k=" + k + ",str=" + str);
        if (StringUtils.isEmpty(k)) {
            return null;
        }
        return JSON.parseObject(str, Chat.class);
    }

    @Override
    public void del(String ackKey, String groupWxId) {
        String k = getKey(getClass(), groupWxId, ackKey);
        getStore().del(k);
        Logs.e(getClass(), "[HalfChatStore]>>k=" + k);
    }

}
