package com.uc.framework.thread;

import java.util.Timer;
import java.util.TimerTask;

/***
 * 异步任务工具，使用条件<br>
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2018年10月26日 新建
 */
public class AsyncTask {

    /***
          * 异步执行单个任务
     * @param task
     * @author HadLuo  2020年2月20日 新建
     */
    public static void execute(Runnable task) {
        if (task == null) {
            return;
        }
        ThreadPools.getThreadPools().execute(task);

    }

    /***
         * 延迟执行 任务
     * 
     * @param task
     * @param delay 毫秒
     * @author HadLuo 2018年10月29日 新建
     */
    public static void execute(final Runnable task, long delay) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // 交给 spring线程池执行
                    ThreadPools.getThreadPools().execute(task);
                } finally {
                    timer.cancel();
                }
            }
        }, delay);
    }

    /***
     * 构造多线程任务 工具
     * 
     * @return
     * @author HadLuo 2019年5月7日 新建
     */
    public static MultiTasker newMultiTasker() {
        return new MultiTaskerCore();
    }
}
