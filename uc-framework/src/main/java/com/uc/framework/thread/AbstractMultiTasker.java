package com.uc.framework.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.uc.framework.logger.Logs;
import com.uc.framework.thread.TimeoutChecker.TimeoutStrategy;

/***
 * 多任务处理器实现
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年2月20日 新建
 */
public abstract class AbstractMultiTasker implements MultiTasker {
    /** 最大线程数 */
    private static final int MAX_THREAD_NUM = 100;
    /** 处理的最大数据量 */
    private static final int MAX_TASK_NUM = 5000000;
    /** 默认超时时间 10分钟 */
    private long timeout = 10 * 60 * 1000;
    /** 线程池 */
    private volatile ExecutorService executorService;
    TimeoutChecker timeoutChecker;
    /** 线程任务 计数器 */
    private CountDownLatch endGate;
    /** 执行结果 */
    private volatile ResultSet resultSet = new ResultSet();

    @Override
    public ResultSet getResultSet() {
        // TODO Auto-generated method stub
        return resultSet;
    }

    int check(List<?> resources, int nThreads) {
        if (resources.size() > MAX_TASK_NUM) {
            throw new RuntimeException("数据量过大，无法处理  size:" + resources.size());
        }
        if (nThreads > MAX_THREAD_NUM) {
            return MAX_THREAD_NUM;
        }
        if (resources.size() < nThreads) {
            return 1;
        }
        return nThreads;
    }

    <I, O> void execute(final Callback callback, final Task<I> task) {
        if (task.isEmpty()) {
            return;
        }
        executorService.execute(new Runnable() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void run() {
                try {
                    if (callback instanceof TaskerCallback) {
                        // 将执行结果 放入 结果集中
                        resultSet.putResult(((TaskerCallback) callback).run(task.getDatas()));
                    } else if (callback instanceof TaskerCallbackNoneResult) {
                        // 没有执行结果
                        ((TaskerCallbackNoneResult) callback).run(task.getDatas());
                    }
                } catch (Throwable e) {
                    Logs.e(getClass(), "", e);
                    if (e instanceof InterruptedException) {
                        closeExcutorService();
                    }
                    resultSet.putThrowable(e);
                } finally {
                    countDown();
                }
            }
        });
    }

    void await() {
        if (endGate == null) {
            return;
        }
        try {
            endGate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void countDown() {
        if (endGate != null) {
            endGate.countDown();
        }
    }

    void newAwait(int size) {
        endGate = new CountDownLatch(size);
    }

    synchronized ResultSet reduce() {
        return resultSet;
    }

    void newExcutorService(int nThreads) {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(nThreads, nThreads, 1, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    void closeExcutorService() {
        try {
            if (executorService != null) {
                executorService.shutdown();
                executorService = null;
            }
        } catch (Exception e) {
        }
    }

    void addTimeoutChecker() {
        if (timeoutChecker == null) {
            timeoutChecker = new TimeoutChecker(timeout, new TimeoutStrategy() {
                @Override
                public void timeout() {
                    if (executorService != null) {
                        Logs.e(AbstractMultiTasker.class,
                                "多任务执行超时！timeout:" + timeout + ",executor=" + executorService);
                    }
                    closeExcutorService();
                }
            });
        }
    }

    public static <I> List<Task<I>> splitTask(List<I> resources, int nThreads) {
        /**
         * 下面为自动分配 分配规则：threadNum（共多少个任务）, 每个任务分配的工作量： 总工作量/threadNum ,
         * 多余的增加到最后一个任务线程
         */
        int cur = 0, totalNum = resources.size();
        int SINGLE_NUM = totalNum / nThreads;
        List<I> curTasks = null;

        if (SINGLE_NUM == 0) {
            new Task<I>(resources);
        }

        List<Task<I>> tasks = new ArrayList<Task<I>>();
        while (true) {
            if (cur >= totalNum - SINGLE_NUM) {
                // 在最后一页内 (可能是只有一页), 也可能在最后一页 没有 满 pageSize
                tasks.add(new Task<I>(resources.subList(cur, totalNum)));
                break; // 没有数据了， 不继续循环了
            }
            // 不是最后一页
            try {
                List<I> datas_ = resources.subList(cur, SINGLE_NUM + cur);
                if (datas_ != null) {
                    curTasks = new ArrayList<I>();
                    curTasks.addAll(datas_);
                }

                if (curTasks == null || curTasks.size() == 0) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
            cur += SINGLE_NUM;
            if (tasks.size() >= nThreads) {
                // 达到了最大线程 , 直接让最后一个线程 负责
                // 取 最后一个任务负责的 工作量
                tasks.get(tasks.size() - 1).append(curTasks);
            } else {
                // 没有达到最大线程 ,开新线程
                tasks.add(new Task<I>(curTasks));
            }
        }
        return tasks;
    }
}
