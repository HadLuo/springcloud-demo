package com.uc.framework.chat.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.base.Preconditions;
import com.uc.framework.chat.ChatRequest;
import com.uc.framework.natives.Classes;

public class ChatProcessorContext {

    private static volatile Map<String, ChatProcessor> processors = new ConcurrentHashMap<>();

    /***
     * title:获取创建好的ChatProcessor
     * 
     * @param alias
     * @return
     */
    public static ChatProcessor from(String alias) {
        ChatProcessor processor = processors.get(alias);
        Preconditions.checkNotNull(processor, alias + "别名的ChatProcessor不存在，请先调createProcessor创建");
        return processor;
    }

    /**
     * 
     * title: 构造 聊天 消息处理器
     * 
     * @param alias 别名， 后面kafka消息可以根据 alias获取 ChatProcessor
     * @param request
     * @return
     * @author HadLuo 2020-9-26 17:03:40
     */
    public static ChatProcessor createProcessor(ChatRequest request) {
        String alias = request.getAlias();
        Preconditions.checkNotNull(alias);
        if (processors.containsKey(alias)) {
            throw new RuntimeException(request + "别名的ChatProcessor已经存在，请更换");
        }
        processors.put(alias, Classes.newInstance(DefaultChatProcessor.class));
        ChatProcessor processor = processors.get(alias);
        // 注入request
        processor.setRequest(request);
        return processor;
    }

}
