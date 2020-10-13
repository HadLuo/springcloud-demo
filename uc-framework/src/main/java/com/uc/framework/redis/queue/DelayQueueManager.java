package com.uc.framework.redis.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Preconditions;

/***
 * 
 * title: 延时队列管理器
 *
 * @author HadLuo
 * @date 2020-9-26 10:11:40
 */
@Component
public class DelayQueueManager implements DisposableBean {

    private final Map<String, DelayQueue> queues = new ConcurrentHashMap<>();
    @Autowired
    private RedissonClient redissonClient;

    /***
     * 
     * title: 获取 延时队列 ， 有缓存起来 ,可多次getQueue
     *
     * @param key redis队列的 key
     * @param listener 有延时消息到了回调
     * @return
     * @author HadLuo 2020-9-26 10:20:08
     */
    public synchronized DelayQueue getQueue(String key, MessageListener listener) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(listener);
        DelayQueue queue = queues.get(key);
        if (queue == null) {
            queue = new RedissonDelayQueue(redissonClient, key, listener);
            queues.putIfAbsent(key, queue);
        }
        return queue;
    }

    @Override
    public void destroy() throws Exception {
        for (String key : queues.keySet()) {
            try {
                queues.get(key).destory();
            } catch (Exception e) {
            }
        }
        queues.clear();
    }

}
