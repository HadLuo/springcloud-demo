package com.uc.framework.chat.context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.uc.framework.chat.Chat;
import com.uc.framework.chat.ChatConfigure;
import com.uc.framework.chat.ChatGroup;
import com.uc.framework.chat.store.ChatHahStore;
import com.uc.framework.chat.store.GroupStatusStore;
import com.uc.framework.chat.store.HalfChatStore;
import com.uc.framework.chat.store.PauseStore;

public class DefaultChatController extends AbstarctChatController {
    ChatHahStore chatHahStore;
    HalfChatStore halfChatStore;
    GroupStatusStore groupStatusStore;
    PauseStore pauseStore;

    public DefaultChatController(ChatConfigure configure, String alias) {
        super(configure, alias);
        chatHahStore = new ChatHahStore(configure.getStore());
        halfChatStore = new HalfChatStore(configure.getStore());
        groupStatusStore = new GroupStatusStore(configure.getStore());
        pauseStore = new PauseStore(configure.getStore());
    }

    @Override
    public LinkedList<Chat> createSortedChat(ChatGroup chatGroup) {
        List<Chat> chats = chatGroup.getChats();
        // 初始化 sort
        Collections.sort(chats, configure.getSortStrategy());
        for (Chat chat : chats) {
            chat.setGroupUuid(chatGroup.getGroupUuid());
        }
        return new LinkedList<>(chats);
    }

    @Override
    public void storeChat(Chat chat) {
        chatHahStore.set(getAlias(), chat.getGroupUuid(), chat.getSort(), chat);
    }

    @Override
    public void storePause(String uuid, String groupWxId) {
        pauseStore.pause(getAlias(), uuid, groupWxId);
    }

    @Override
    public void storeGroupSortStatus(String groupUuid, String groupWxId, int sort) {
        groupStatusStore.put(getAlias(), groupUuid, groupWxId, sort);
    }

    @Override
    public void storeHalfChat(String ackKey, Chat chat) {
        halfChatStore.set(chat.getCurrentSendGroupWxId(), ackKey, chat);
    }

    @Override
    public int moveToNext(Chat chat) {
        return groupStatusStore.next(getAlias(), chat.getGroupUuid(), chat.getCurrentSendGroupWxId());
    }

    @Override
    public int getGroupSortStatus(String groupUuid, String groupWxId) {
        return groupStatusStore.get(getAlias(), groupUuid, groupWxId);
    }

    @Override
    public Chat getChat(String groupUuid, int sort) {
        return chatHahStore.get(getAlias(), groupUuid, sort);
    }

    @Override
    public Chat getHalfChat(String ackKey, String groupWxId) {
        return halfChatStore.get(ackKey, groupWxId);
    }

    @Override
    public boolean checkPause(ChatGroup chatGroup) {
        List<Chat> chats = chatGroup.getChats();
        if (CollectionUtils.isEmpty(chats)) {
            return false;
        }
        for (String id : chats.get(0).getNeedSendGroupWxIds()) {
            if (pauseStore.isPause(getAlias(), chatGroup.getGroupUuid(), id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void delPause(String uuid, String groupWxId) {
        pauseStore.cancel(getAlias(), uuid, groupWxId);
    }

    @Override
    public boolean getPause(String uuid, String groupWxId) {
        // TODO Auto-generated method stub
        return pauseStore.isPause(getAlias(), uuid, groupWxId);
    }

    @Override
    public void clearStore(String uuid) {
        chatHahStore.del(getAlias(), uuid);
        groupStatusStore.del(getAlias(), uuid);
    }

    @Override
    public void delHalfChat(String ackKey, String groupWxId) {
        halfChatStore.del(ackKey, groupWxId);
    }

}
