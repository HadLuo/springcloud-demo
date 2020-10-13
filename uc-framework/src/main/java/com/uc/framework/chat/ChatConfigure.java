package com.uc.framework.chat;

import java.util.Comparator;
import com.uc.framework.chat.store.ChatStore;
import com.uc.framework.chat.store.RedisChatStore;
import com.uc.framework.chat.strategy.ChatSortStrategy;
import com.uc.framework.chat.strategy.SendChatStrategy;

public class ChatConfigure {
    /** 聊天包的排序策略 */
    private Comparator<Chat> sortStrategy;
    /** 发送聊天剧本的生命周期 */
    private AbstractChatLifeCycle lifeCycle;
    /** 真正发送聊天包策略 */
    private SendChatStrategy sendChatStrategy;
    /**消息存储池*/
    private ChatStore store;

    /***
     * 
     * title: 設置排序规则
     *
     * @param sortStrategy
     * @return
     * @author HadLuo 2020-9-27 11:18:31
     */
    public ChatConfigure setSortStrategy(Comparator<Chat> sortStrategy) {
        this.sortStrategy = sortStrategy;
        return this;
    }
    
    /***
     * 
     * title: 設消息存储
     *
     * @param sortStrategy
     * @return
     * @author HadLuo 2020-9-27 11:18:31
     */
    public ChatConfigure setStore(ChatStore store) {
    	this.store = store;
    	return this;
    }

    /***
     * 
     * title: 设置 发送聊天剧本的生命周期
     *
     * @param lifeCycle
     * @return
     * @author HadLuo 2020-9-27 15:26:16
     */
    public ChatConfigure setLifeCycle(AbstractChatLifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
        return this;
    }


    /**
     * 
     * title: 设置 真正发送聊天包策略
     *
     * @param sendChatStrategy
     * @author HadLuo 2020-9-27 15:26:07
     */
    public ChatConfigure setSendChatStrategy(SendChatStrategy sendChatStrategy) {
        this.sendChatStrategy = sendChatStrategy;
        return this;
    }

    public AbstractChatLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public SendChatStrategy getSendChatStrategy() {
        return sendChatStrategy;
    }

    public Comparator<Chat> getSortStrategy() {
        if (null == sortStrategy) {
            sortStrategy = new ChatSortStrategy();
        }
        return sortStrategy;
    }
    public ChatStore getStore() {
    	if (null == store) {
    		store = new RedisChatStore();
        }
		return store;
	}
}
