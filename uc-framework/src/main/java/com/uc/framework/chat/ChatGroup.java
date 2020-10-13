package com.uc.framework.chat;

import java.util.List;

/***
 * title : 分组的聊天包（一个剧本一个组） 里面包含 要发送的聊天包
 * 
 * @author HadLuo
 *
 */

public class ChatGroup {
    /** 具体的聊天消息可以无序 */
    private List<Chat> chats;
    /** 业务唯一标识 ，标识这一个聊天剧本(包含多个聊天包) */
    private String groupUuid;

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

}
