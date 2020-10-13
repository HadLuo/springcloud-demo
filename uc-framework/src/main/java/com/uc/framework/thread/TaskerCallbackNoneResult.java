package com.uc.framework.thread;

import java.util.List;

/***
 * 子线程 自行的任务回调
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年2月20日 新建
 */
public interface TaskerCallbackNoneResult<I> extends Callback {
    public void run(List<I> curDatas);
}
