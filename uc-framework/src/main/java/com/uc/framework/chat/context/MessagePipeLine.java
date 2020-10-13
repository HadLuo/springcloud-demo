package com.uc.framework.chat.context;

import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.uc.framework.chat.Chat;
import com.uc.framework.redis.queue.Task;

/***
 * title : 消息 管道
 * 
 * @author HadLuo
 *
 */
public class MessagePipeLine {

    AbstractChatProcessor processor;

    public MessagePipeLine(AbstractChatProcessor processor) {
        this.processor = processor;
    }

    /***
     * title : 将聊天包放入 管道 发送到redis延时队列里面
     * 
     * @param chat
     * @return
     */
    public MessagePipeLine add(Chat chat) {
        if (chat == null) {
            return this;
        }
        Set<String> ids = chat.getNeedSendGroupWxIds();
        if (CollectionUtils.isEmpty(ids)) {
            return this;
        }
        // 发送第一条消息 , 每个群发送一遍
        for (String groupWxId : ids) {
            add(chat, groupWxId);
        }
        return this;
    }

    /***
     * title : 将聊天包放入 管道 发送到redis延时队列里面
     * 
     * @param chat
     * @return
     */
    public MessagePipeLine add(Chat chat, String groupWxId) {
        if (chat == null || StringUtils.isEmpty(groupWxId)) {
            return this;
        }
        // 设置当前发送的 wxId
        chat.setCurrentSendGroupWxId(groupWxId);
        // 放入延时队列
        processor.getQueue().put(Task.newTask(chat, chat.getDelay()));
        return this;
    }

}
