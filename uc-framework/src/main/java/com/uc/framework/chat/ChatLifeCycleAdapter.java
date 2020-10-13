package com.uc.framework.chat;

import java.util.LinkedList;

public class ChatLifeCycleAdapter extends AbstractChatLifeCycle {

    @Override
    public void onStartup(String groupUuid, LinkedList<Chat> chats) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSingleSendError(String ackKey, String groupUuid, Chat chat, String errorMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSingleSendStart(String groupUuid, Chat chat) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSingleHalfSendSuccess(String groupUuid, String ackKey, Chat chat) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSingleAckSendSuccess(String groupUuid, String ackKey, Chat chat) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause(String groupUuid, String groupWxId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume(String groupUuid, String groupWxId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSingleAckSendError(String groupUuid, String ackKey, Chat chat, String errorMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGroupFinish(String groupUuid, String groupId) {
        // TODO Auto-generated method stub

    }

}
