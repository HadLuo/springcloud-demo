package com.uc.framework.redis.queue;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.uc.framework.Ids;

/**
 * 
 * title: 延时 任务
 *
 * @author HadLuo
 * @date 2020-9-15 10:44:40
 */
public class Task implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5743096866266963934L;

    /**
     * 具体 任务
     */
    private Object data;

    /**
     * 延迟时间
     */
    private long delay;

    private TimeUnit timeUnit;

    /***
     * 任务唯一标识 id ,用于跟踪任务， 来停止任务
     */
    private String taskId;

    Task() {
    }

    public static Task newTask(Object data, long delay) {
        Task task = new Task();
        task.setData(data);
        task.setDelay(delay);
        task.setTaskId(Ids.getId());
        task.setTimeUnit(TimeUnit.SECONDS);
        return task;
    }

    public static <T> Task newTask(Object data, long delay, String taskId) {
        Task task = new Task();
        task.setData(data);
        task.setDelay(delay);
        task.setTaskId(taskId);
        task.setTimeUnit(TimeUnit.SECONDS);
        return task;
    }

    public static <T> Task newTask(Object data, long delay, String taskId, TimeUnit timeUnit) {
        Task task = new Task();
        task.setData(data);
        task.setDelay(delay);
        task.setTaskId(taskId);
        task.setTimeUnit(timeUnit);
        return task;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}
