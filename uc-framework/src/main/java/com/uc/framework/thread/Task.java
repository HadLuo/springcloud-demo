package com.uc.framework.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Task<T> {

    private List<T> datas;

    public Task(List<T> datas) {
        super();
        this.datas = datas;
    }

    public List<T> getDatas() {
        if (datas == null) {
            return Collections.emptyList();
        }
        return datas;
    }

    public void append(List<T> datas) {
        if (datas != null) {
            getDatas().addAll(datas);
        }
    }

    public boolean isEmpty() {
        try {
            if (datas != null) {
                return new ArrayList<T>(datas).isEmpty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
