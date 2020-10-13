package com.uc.framework.thread;

import java.util.Timer;
import java.util.TimerTask;

/***
 * 超时检测器
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2018年6月4日 新建
 */
public final class TimeoutChecker extends TimerTask {

    public static interface TimeoutStrategy {
        public void timeout();
    }

    private final Timer timer;

    private final TimeoutStrategy timeoutStrategy;

    private volatile boolean isRunning;

    public TimeoutChecker(long timeout, TimeoutStrategy timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
        shutdown();
        isRunning = true;
        this.timer = new Timer();
        this.timer.schedule(this, timeout);
    }

    @Override
    public void run() {
        try {
            if (timeoutStrategy != null) {
                timeoutStrategy.timeout();
            }
        } finally {
            shutdown();
        }
    }

    /***
     * 停止检测器
     */
    public void shutdown() {
        try {
            if (isRunning) {
                cancel();
            }
            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
        }
        isRunning = false;
    }

}