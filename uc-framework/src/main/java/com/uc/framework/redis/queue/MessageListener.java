package com.uc.framework.redis.queue;

/***
 * 
 * title: 消息监听器
 *
 * @author HadLuo
 * @date 2020-9-15 10:53:56
 */
public interface MessageListener {

    /***
     * 
     * title: 收到延时队列消息
     *
     * @param task
     * @author HadLuo 2020-9-26 10:09:49
     */
    public void onMessage(Task task);

}
