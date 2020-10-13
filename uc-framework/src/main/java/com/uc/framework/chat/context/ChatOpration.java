package com.uc.framework.chat.context;

public interface ChatOpration {

    /***
     * 
     * title: 取消暂停 , 继续推送
     *
     * @param chatGroup
     * @return
     * @author HadLuo 2020-10-10 14:01:10
     */
    public abstract void cancelPause(String groupUuid, String groupWxId);

    /***
     * 
     * title: 启动暂停
     *
     * @param chatGroup
     * @author HadLuo 2020-10-10 14:26:06
     */
    public abstract void startPause(String groupUuid, String groupWxId);

}
