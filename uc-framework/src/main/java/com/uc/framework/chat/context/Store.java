package com.uc.framework.chat.context;

import com.uc.framework.chat.Chat;

public interface Store {

    /***
     * 
     * title: 聊天包 的临时存储池
     *
     * @param chat
     * @author HadLuo 2020-10-10 13:24:02
     */
    public void storeChat(Chat chat);

    /***
     * 
     * title: 获取聊天消息
     *
     * @param groupUuid
     * @param sort
     * @return
     * @author HadLuo 2020-10-10 13:36:30
     */
    public Chat getChat(String groupUuid, int sort);

    /***
     * 
     * title: 存储 记录每个群发送 到哪个发言人了
     *
     * @param groupUuid
     * @param groupWxId
     * @param sort
     * @author HadLuo 2020-10-10 13:28:13
     */
    public void storeGroupSortStatus(String groupUuid, String groupWxId, int sort);

    public int getGroupSortStatus(String groupUuid, String groupWxId);

    /***
     * 
     * title: 将 这个群的发言人 设置为下一个待发送
     *
     * @param chat
     * @author HadLuo 2020-10-10 13:33:20
     */
    public int moveToNext(Chat chat);

    /***
     * 
     * title: 存储半消息
     *
     * @param ackKey
     * @param chat
     * @author HadLuo 2020-10-10 13:31:09
     */
    public void storeHalfChat(String ackKey, Chat chat);

    /***
     * 
     * title: 获取半消息
     *
     * @param ackKey
     * @param groupWxId
     * @return
     * @author HadLuo 2020-10-10 13:46:41
     */
    public Chat getHalfChat(String ackKey, String groupWxId);
    
    /***
     * 
     * title: 删除半消息 
     *
     * @param ackKey
     * @param groupWxId
     * @author HadLuo 2020-10-12 13:39:40
     */
    public void delHalfChat(String ackKey, String groupWxId);

    /***
     * 
     * title: 暂停这个聊天
     *
     * @param alias
     * @param chat
     * @author HadLuo 2020-10-10 13:18:26
     */
    public void storePause(String uuid, String groupWxId);

    public boolean getPause(String uuid, String groupWxId);

    /***
     * 
     * title: 删除 暂停这个聊天
     *
     * @param alias
     * @param chat
     * @author HadLuo 2020-10-10 13:18:26
     */
    public void delPause(String uuid, String groupWxId);
    
    /***
     * 
     * title: 清楚所有redis key
     *
     * @param uuid
     * @author HadLuo 2020-10-12 9:23:34
     */
    public void clearStore(String uuid);

}
