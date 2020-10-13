package com.uc.framework.thread;

import java.util.ArrayList;
import java.util.List;

/***
 * 多任务工具(线程不安全， 只能放在方法中调用)
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2019年5月7日 新建
 */
public interface MultiTasker {

    /***
     * 多线程任务执行 ， 且汇总 返回了各子任务执行的结果
     * 
     * @param resources 总任务量
     * @param nThreads 多少条线程执行
     * @param callback 子任务 执行的 逻辑回调
     * @return
     * @author HadLuo 2019年5月6日 新建
     */
    public <I, O> ArrayList<O> mapReduce(List<I> resources, int nThreads, TaskerCallback<I, O> callback);

    /***
     * 多线程任务执行 ， 且汇总 返回了各子任务执行的结果
     * 
     * @param resources 总任务量
     * @param nThreads 多少条线程执行
     * @param callback 子任务 执行的 逻辑回调
     * @return
     * @author HadLuo 2019年5月6日 新建
     */
    public <I> void mapReduceNoShuntDown(List<I> resources, int nThreads,
            TaskerCallbackNoneResult<I> callback);

    /***
     * 多线程任务执行 ， 没有返回结果
     * 
     * @param resources
     * @param nThreads
     * @param callback
     * @author HadLuo 2019年5月6日 新建
     * @param <I>
     */
    public <I> void map(List<I> resources, int nThreads, TaskerCallbackNoneResult<I> callback);

    /***
     * 获取当前执行结果
     * 
     * @return
     * @author HadLuo 2019年5月7日 新建
     */
    public ResultSet getResultSet();

    public void close();
}
