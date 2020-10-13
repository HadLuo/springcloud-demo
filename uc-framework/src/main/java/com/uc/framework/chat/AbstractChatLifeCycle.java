package com.uc.framework.chat;

import java.util.LinkedList;

/***
 * 
 * title: 整个发送阶段 的生命周期
 *
 * @author HadLuo
 * @date 2020-9-27 15:15:41
 */
public abstract class AbstractChatLifeCycle {

    /***
     * 
     * title: 整个 剧本 开始发送
     *
     * @param groupWxIds
     * @param chats
     * @author HadLuo 2020-9-27 15:15:57
     */
    public abstract void onStartup(String groupUuid, LinkedList<Chat> chats);

    /***
     * 
     * title: 某个群的 所有发言人 发言完成
     *
     * @param groupUuid
     * @author HadLuo 2020-10-10 11:30:56
     */
    public abstract void onGroupFinish(String groupUuid, String groupId);

    /***
     * 
     * title: 单条消息开始推送
     *
     * @param groupUuid
     * @param chat
     * @author HadLuo 2020-10-12 10:22:38
     */
    public abstract void onSingleSendStart(String groupUuid, Chat chat);

    /***
     * 
     * title: 单条消息 发送失败
     *
     * @param groupWxIds
     * @param chats
     * @author HadLuo 2020-9-27 15:15:57
     */
    public abstract void onSingleSendError(String ackKey, String groupUuid, Chat chat, String errorMessage);

    /***
     * 
     * title: 单条半消息 发送成功(待确认成功)
     *
     * @param ackKey 发送消息返回的唯一标识
     * @param chat 消息
     * @author HadLuo 2020-9-27 15:15:57
     */
    public abstract void onSingleHalfSendSuccess(String groupUuid, String ackKey, Chat chat);

    /***
     * 
     * title: 单条消息确认 发送成功
     *
     * @param ackKey 发送消息返回的唯一标识
     * @param chat 消息
     * @author HadLuo 2020-9-27 15:15:57
     */
    public abstract void onSingleAckSendSuccess(String groupUuid, String ackKey, Chat chat);

    /***
     * 
     * title: 单条消息确认 发送 失败
     *
     * @param ackKey 发送消息返回的唯一标识
     * @param chat 消息
     * @author HadLuo 2020-9-27 15:15:57
     */
    public abstract void onSingleAckSendError(String groupUuid, String ackKey, Chat chat,
            String errorMessage);

    /***
     * 
     * title: 群剧本被暂停了
     *
     * @param groupUuid
     * @author HadLuo 2020-10-10 11:30:56
     */
    public abstract void onPause(String groupUuid, String groupWxId);

    /***
     * 
     * title: 群剧本被恢复了
     *
     * @param groupUuid
     * @author HadLuo 2020-10-10 11:30:56
     */
    public abstract void onResume(String groupUuid, String groupWxId);
}
