package com.uc.framework.redis.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.uc.framework.Ids;
import com.uc.framework.logger.Logs;

/**
 * 
 * title: redisson 实现的 队列
 *
 * @author HadLuo
 * @date 2020-9-15 10:57:05
 */
public class RedissonDelayQueue implements DelayQueue, Runnable {
    private RBlockingQueue<Task> blockingQueue;
    private RDelayedQueue<Task> delayedQueue;
    private MessageListener listener;
    private volatile boolean stop;

    private final AtomicInteger source = new AtomicInteger(1);
    private static final String ThreadName = "RedissonQueueLoop-Thread-";
    private static final String ThreadNameExecutors = "RedissonQueueLoop-HadndlerThread-";
    private volatile Thread thread;

    private ExecutorService executorService;

    public RedissonDelayQueue(RedissonClient redissonClient, String redisQueueKey, MessageListener listener) {
        Preconditions.checkNotNull(redissonClient);
        if (StringUtils.isEmpty(redisQueueKey)) {
            throw new IllegalArgumentException();
        }
        this.blockingQueue = redissonClient.getBlockingQueue(redisQueueKey);
        this.delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        this.listener = listener;
        stop = true;
        initLoopThread();
        int num = 1;
        executorService = new ThreadPoolExecutor(num, num, 1, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10000), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(Thread.currentThread().getThreadGroup(), r,
                                ThreadNameExecutors + source.getAndIncrement());
                    }
                });
    }

    private synchronized void initLoopThread() {
        if (thread == null) {
            thread = new Thread(Thread.currentThread().getThreadGroup(), this,
                    ThreadName + source.getAndIncrement());
            if (stop) {
                thread.start();
            }
        }
    }

    @Override
    public void run() {
        Logs.console().i(getClass(), "RedissonQueue start ！");
        while (stop) {
            try {
                final Task task = blockingQueue.take();
                if (task == null) {
                    Logs.e(getClass(), "RedissonQueue receive null body");
                }
                Logs.e(getClass(), "RedissonQueue take>>" + JSON.toJSONString(task));
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onMessage(task);
                        }
                    }
                });

            } catch (InterruptedException e) {
                // 打断 异常
                stop = false;
                return;
            } catch (Exception e) {
                Logs.e(getClass(), "RedissonQueue unkonw exception", e);
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (Exception ex) {
                }
            }

        }

    }

    @Override
    public <T> void put(Task task) {
        if (task != null && task.getDelay() > 0) {
            delayedQueue.offer(task, task.getDelay(), task.getTimeUnit());
            Logs.e(getClass(), "RedissonQueue put>>" + JSON.toJSONString(task));
        }
    }

    @Override
    public void destory() {
        try {
            stop = false;
            if (executorService != null) {
                executorService.shutdown();
            }
            if (delayedQueue != null) {
                delayedQueue.destroy();
            }
            thread.interrupt();
        } catch (Exception e) {
        }
    }

    @Override
    public <T> String put(Object data, long delay) {
        String traceId = Ids.getId();
        put(Task.newTask(data, delay, traceId));
        return traceId;
    }

    @Override
    public <T> String put(Object data, long delay, TimeUnit timeUnit) {
        String traceId = Ids.getId();
        put(Task.newTask(data, delay, traceId, timeUnit));
        return traceId;
    }

}
