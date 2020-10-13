package com.uc.framework.chat.context;

import com.uc.framework.App;
import com.uc.framework.chat.ChatGroup;
import com.uc.framework.redis.queue.DelayQueue;
import com.uc.framework.redis.queue.DelayQueueManager;

public class DefaultChatProcessor extends AbstractChatProcessor {

    private static final String KEY = "chat.queue.delay.";

    public DefaultChatProcessor() {
    }

    @Override
    public void onAccept(ChatGroup chatGroup) {
        launch(chatGroup);
    }

    @Override
    public DelayQueue getQueue() {
        // 一个alias 对应一个延时队列
        return App.getBean(DelayQueueManager.class).getQueue(KEY + getRequest().getAlias(), this);
    }

}
