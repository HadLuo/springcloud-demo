package com.uc.framework.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.uc.framework.App;

/***
 * 线程池工具,优先获取spring配置的线程池
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年2月20日 新建
 */
public class ThreadPools {

    private static volatile Executor EXECUTOR = null;

    private static Executor fromSpringPools() {
        try {
            return App.getBean(ThreadPoolTaskExecutor.class);
        } catch (Throwable e) {
            return null;
        }
    }

    public synchronized static Executor getThreadPools() {
        Executor executor = fromSpringPools();
        if (executor != null) {
            return executor;
        }
        if (EXECUTOR == null) {
            // 构造一个核心为100 最大为100的线程池
            EXECUTOR = new ThreadPoolExecutor(100, 100, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1000));
        }
        return EXECUTOR;
    }
}
