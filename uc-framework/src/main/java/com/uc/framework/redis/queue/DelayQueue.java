package com.uc.framework.redis.queue;

import java.util.concurrent.TimeUnit;

/**
 * 
 * title: 延时队列
 *
 * @author HadLuo
 * @date 2020-9-15 10:54:17
 */
public interface DelayQueue {
    /**
     * 
     * title: 放任务
     *
     * @param <T>
     * @param task
     * @author HadLuo 2020-9-15 10:48:01
     */
    public <T> void put(Task task);

    /**
     * 
     * title: 放任务
     *
     * @param <T>
     * @param data
     * @param delay 多少秒 后触发
     * @return 返回 唯一跟踪id
     * @author HadLuo 2020-9-26 10:03:22
     */
    public <T> String put(Object data, long delay);

    /**
     * 
     * title: 放任务
     *
     * @param <T>
     * @param data
     * @param delay 多少秒 后触发
     * @return 返回 唯一跟踪id
     * @author HadLuo 2020-9-26 10:03:22
     */
    public <T> String put(Object data, long delay, TimeUnit timeUnit);

    /**
     * 
     * title: 销毁队列
     *
     * @author HadLuo 2020-9-15 10:48:10
     */
    public void destory();

}
