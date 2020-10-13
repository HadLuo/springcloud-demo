package com.uc.framework.thread;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * 多任务处理器实现核心
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年2月20日 新建
 */
public class MultiTaskerCore extends AbstractMultiTasker {

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> ArrayList<O> mapReduce(List<I> resources, int nThreads, TaskerCallback<I, O> callback) {
        return (ArrayList<O>) map0(resources, nThreads, callback, true).getResult();
    }

    @Override
    public <I> void mapReduceNoShuntDown(List<I> resources, int nThreads,
            TaskerCallbackNoneResult<I> callback) {
        map0(resources, nThreads, callback, false).getResult();
    }

    private <I> ResultSet map0(List<I> resources, int nThreads, Callback callback, boolean isShuntDown) {
        if (CollectionUtils.isEmpty(resources)) {
            return ResultSet.empty();
        }
        nThreads = check(resources, nThreads);
        // 按线程 数 来 分割总任务任务
        List<Task<I>> tasks = splitTask(resources, nThreads);
        // 构造nThreads大小的线程池
        newExcutorService(nThreads);
        // 添加超时 检测器。 以便 工作线程结束不了 卡死
        addTimeoutChecker();
        // 构造线程计数器
        newAwait(tasks.size());
        // 执行任务
        try {
            for (Task<I> task : tasks) {
                execute(callback, task);
            }
            await();
        } finally {
            if (isShuntDown) {
                close();
            }
        }
        // 汇总数据
        return reduce();
    }

    @Override
    public <I> void map(List<I> resources, int nThreads, TaskerCallbackNoneResult<I> callback) {
        map0(resources, nThreads, callback, true);
    }

    @Override
    public void close() {
        closeExcutorService();
        if (timeoutChecker != null) {
            timeoutChecker.shutdown();
        }
    }
}
