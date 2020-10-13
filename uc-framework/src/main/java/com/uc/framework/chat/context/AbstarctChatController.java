package com.uc.framework.chat.context;

import java.util.LinkedList;

import com.uc.framework.chat.Chat;
import com.uc.framework.chat.ChatConfigure;
import com.uc.framework.chat.ChatGroup;

public abstract class AbstarctChatController implements Store {
    ChatConfigure configure;
    String alias;

    public AbstarctChatController(ChatConfigure configure, String alias) {
        super();
        this.configure = configure;
        this.alias = alias;
    }

    /**
     * 
     * title: 创建有序的 聊天发送包
     *
     * @param playId
     * @return <所有要发送的群id， 发言人聊天包>
     * @author HadLuo 2020-9-26 17:25:52
     */
    public abstract LinkedList<Chat> createSortedChat(ChatGroup chatGroup);

    /***
     * 
     * title: 检测暂停
     *
     * @param chatGroup
     * @return
     * @author HadLuo 2020-10-10 14:01:10
     */
    public abstract boolean checkPause(ChatGroup chatGroup);

    public String getAlias() {
        return alias;
    }

}
