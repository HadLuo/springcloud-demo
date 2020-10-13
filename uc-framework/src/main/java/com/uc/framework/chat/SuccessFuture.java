package com.uc.framework.chat;

public class SuccessFuture extends AbstractFuture {
    public SuccessFuture(String ackKey, String groupId) {
        super(groupId, ackKey);
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }
}
