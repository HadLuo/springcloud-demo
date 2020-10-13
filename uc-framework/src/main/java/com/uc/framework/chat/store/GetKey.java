package com.uc.framework.chat.store;

public abstract class GetKey {

    private ChatStore store;

    public GetKey(ChatStore store) {
        this.store = store;
    }

    public ChatStore getStore() {
        return store;
    }

    public String getKey(Class<?> clazz, String... args) {
        String key = clazz.getName() + ".";
        if (args != null) {
            for (String arg : args) {
                key = key + arg + ".";
            }
        }
        return key;
    }

    public abstract void del(String alias, String groupUuid);

}
