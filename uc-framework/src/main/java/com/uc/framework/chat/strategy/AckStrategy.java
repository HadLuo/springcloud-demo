package com.uc.framework.chat.strategy;

import com.uc.framework.chat.Future;

public interface AckStrategy {

    /***
     * title : kafka消息确认
     * 
     * @author HadLuo
     * @param future
     */
    public Future get();

}
