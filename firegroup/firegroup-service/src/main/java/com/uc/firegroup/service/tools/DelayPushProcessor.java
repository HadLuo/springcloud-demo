//package com.uc.firegroup.service.tools;
//
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import com.uc.framework.redis.queue.MessageListener;
//import com.uc.framework.redis.queue.Queue;
//import com.uc.framework.redis.queue.RedissonQueue;
//import com.uc.framework.redis.queue.Task;
//
///***
// * 
// * title: 延迟消息 推送处理器
// *
// * @author HadLuo
// * @date 2020-9-15 10:37:22
// */
//@Component
//public class DelayPushProcessor implements DisposableBean, InitializingBean, MessageListener {
//
//    private static final String KEY = "queue.send.delay.msg";
//    private Queue queue;
//
//    private volatile MessageListener messageListener;
//    @Autowired
//    private RedissonClient redissonClient;
//
//    @Override
//    public void destroy() throws Exception {
//        if (queue != null) {
//            queue.destory();
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.queue = new RedissonQueue(redissonClient, KEY, this);
//    }
//
//    public void setMessageListener(MessageListener messageListener) {
//        if (this.messageListener == null) {
//            this.messageListener = messageListener;
//        }
//    }
//
//    @Override
//    public void onMessage(Task task) {
//        if (messageListener != null) {
//            messageListener.onMessage(task);
//        }
//    }
//
//    public void put(Task task) {
//        queue.put(task);
//    }
//
//}
