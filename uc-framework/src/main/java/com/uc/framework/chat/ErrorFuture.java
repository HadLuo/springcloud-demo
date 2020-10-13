package com.uc.framework.chat;

public class ErrorFuture extends AbstractFuture {

    private String errorMessage;

    public ErrorFuture(String groupId, String ackKey, String errorMessage) {
        super(groupId, ackKey);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
