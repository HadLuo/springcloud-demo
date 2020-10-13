package com.uc.framework.chat;

public abstract class ChatRequest {
    private String alias;

    public ChatRequest(String alias) {
        super();
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
