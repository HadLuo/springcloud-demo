package com.uc.framework.chat.store;

import com.uc.framework.logger.Logs;

public class PauseStore extends GetKey {

    public PauseStore(ChatStore store) {
        super(store);
        // TODO Auto-generated constructor stub
    }

    public void pause(String alias, String groupUuid, String groupWxId) {
        String k = getKey(getClass(), alias, groupUuid, groupWxId);
        getStore().set(k, "1");
        Logs.e(getClass(),
                "[pause chat store]>>key=" + k + ",groupUuid=" + groupUuid + ",groupWxId=" + groupWxId);
    }

    public void cancel(String alias, String groupUuid, String groupWxId) {
        String k = getKey(getClass(), alias, groupUuid, groupWxId);
        getStore().del(k);
        Logs.e(getClass(), "[cancel pause chat store]>>key=" + k + ",groupUuid=" + groupUuid + ",groupWxId="
                + groupWxId);
    }

    public boolean isPause(String alias, String groupUuid, String groupWxId) {
        boolean ret = "1".equals(getStore().get(getKey(getClass(), alias, groupUuid, groupWxId)));
        if (ret) {
            Logs.e(getClass(), "[chat paused]>>groupUuid=" + groupUuid + ",groupWxId=" + groupWxId);
        }
        return ret;
    }

    @Override
    public void del(String alias, String groupUuid) {

    }
}
