package com.uc.framework.chat;

public abstract class AbstractFuture implements Future {
    private String groupId;
    private String ackKey;

    public AbstractFuture(String groupId, String ackKey) {
        super();
        this.groupId = groupId;
        this.ackKey = ackKey;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAckKey() {
        return ackKey;
    }

    @Override
    public String getUuid() {
        // TODO Auto-generated method stub
        return null;
    }

}
