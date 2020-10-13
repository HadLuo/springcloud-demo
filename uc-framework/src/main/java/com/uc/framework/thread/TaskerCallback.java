package com.uc.framework.thread;

import java.util.List;

public interface TaskerCallback<I, O> extends Callback {
    public List<O> run(List<I> curDatas);
}
