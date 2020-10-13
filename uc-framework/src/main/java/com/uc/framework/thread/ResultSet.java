package com.uc.framework.thread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/***
 * 执行结果集
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2019年5月7日 新建
 */
public class ResultSet implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4461263584556490718L;
    /** 错误信息 */
    private final ArrayList<Throwable> throwables = new ArrayList<Throwable>();
    private final ArrayList<Object> results = new ArrayList<>();

    public synchronized ArrayList<?> getResult() {
        return results;
    }

    public static ResultSet empty() {
        return new ResultSet();
    }

    synchronized void putResult(List<?> result) {
        if (result != null) {
            for (Object object : result) {
                results.add(object);
            }
        }
    }

    synchronized void putThrowable(Throwable e) {
        throwables.add(e);
    }

    public void clear() {
        if (results != null) {
            results.clear();
        }
        if (throwables != null) {
            throwables.clear();
        }
    }
}
