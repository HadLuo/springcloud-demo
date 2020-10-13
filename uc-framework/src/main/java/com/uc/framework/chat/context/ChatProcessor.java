package com.uc.framework.chat.context;

import com.uc.framework.chat.ChatConfigure;
import com.uc.framework.chat.ChatGroup;
import com.uc.framework.chat.ChatRequest;
import com.uc.framework.chat.strategy.AckStrategy;

/***
 * 
 * title: 聊天推送 处理器
 *
 * @author HadLuo
 * @date 2020-9-26 16:39:30
 */
public interface ChatProcessor extends ChatOpration {
    /***
     * 
     * title: 接受 聊天任务
     *
     * @param param
     * @author HadLuo 2020-9-19 16:03:49
     */
    public void onAccept(ChatGroup chatGroup);

    /***
     * 
     * title: 设置控制器
     *
     * @param controller
     * @author HadLuo 2020-9-26 17:15:15
     */
    public void setController(AbstarctChatController controller);

    /***
     * 
     * title: 设置配置
     *
     * @param controller
     * @author HadLuo 2020-9-26 17:15:15
     */
    public void setConfigure(ChatConfigure configure);

    public void setRequest(ChatRequest request);

    /***
     * 
     * title: 聊天消息确认，kafka回调
     *
     * @param task 聊天任务 id
     * @author HadLuo 2020-9-26 17:15:15
     */
    public void onAck(AckStrategy ackStrategy);

    /***
     * 
     * title: 主动清楚所有的 存储信息
     *
     * @param groupUuid
     * @author HadLuo 2020-10-12 11:14:33
     */
    public void clearStore(String groupUuid);
}
