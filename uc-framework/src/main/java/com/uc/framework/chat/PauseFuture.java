package com.uc.framework.chat;

public class PauseFuture extends AbstractFuture {

    private String uuid;

    public PauseFuture(String uuid, String groupId) {
        super(groupId, null);
        this.uuid = uuid;
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
